package com.example.amaptest.video

import android.media.MediaDataSource
import android.util.Log //  请确保在 Android 项目中使用，或替换为其他日志库
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.min

/**
 * 可选的监听器，用于在从数据包头部解析出新的宽高信息时通知应用程序。
 * MediaExtractor 通常从 SPS 获取初始宽高，这个监听器主要用于应用层面的感知。
 */
interface DimensionListener {
    fun onNewDimensions(width: Int, height: Int)
}

class StreamingMediaDataSource(
    private val maxBufferSizeBytes: Long = DEFAULT_BUFFER_SIZE_BYTES // 缓冲区的最大字节容量
) : MediaDataSource() {

    companion object {
        const val DEFAULT_BUFFER_SIZE_BYTES = 10 * 1024 * 1024L // 默认 10 MB
        private const val HEADER_SIZE_BYTES = 8 // 假设头部大小: 4 字节宽度, 4 字节高度
        private const val TAG = "StreamingMediaDS"
        private val POISON_PILL_PACKET = ByteArray(0) // 用于唤醒阻塞在队列上的读取操作
    }

    // 存储H.265 body（负载）的队列
    private val packetQueue = LinkedBlockingQueue<ByteArray>()

    // Semaphore 用于控制缓冲队列中数据包的总字节大小
    private val bufferCapacitySemaphore = Semaphore(maxBufferSizeBytes.toInt(), true) // true for fairness

    @Volatile
    private var isClosed = false
    private var totalBytesEffectivelyServed: Long = 0L // MediaExtractor 应该请求的下一个字节的偏移量

    var dimensionListener: DimensionListener? = null

    // 锁，用于保护 isClosed, totalBytesEffectivelyServed, currentPacket, currentPacketOffset
    private val stateLock = ReentrantLock()

    // 当前正在被 readAt 方法处理的数据包及其偏移量
    private var currentPacket: ByteArray? = null
    private var currentPacketOffset: Int = 0

    /**
     * 由外部调用者调用，用于放入带有头部（包含宽、高）和H.265负载的数据包。
     * 此方法会解析头部，提取宽高（通过DimensionListener回调），然后将body部分放入缓冲队列。
     */
    fun putData(packetWithHeader: ByteArray) {
        if (stateLock.withLock { isClosed }) {
            Log.w(TAG, "DataSource is closed. Ignoring putData.")
            return
        }

        if (packetWithHeader.size < HEADER_SIZE_BYTES) {
            Log.e(TAG, "Malformed packet: too short for header. Size: ${packetWithHeader.size}")
            return
        }

        val headerBuffer = ByteBuffer.wrap(packetWithHeader, 0, HEADER_SIZE_BYTES)
        val width = headerBuffer.int
        val height = headerBuffer.int

        // 在锁外调用 listener，避免长时间持有锁
        try {
            dimensionListener?.onNewDimensions(width, height)
        } catch (e: Exception) {
            Log.e(TAG, "DimensionListener threw an exception", e)
        }


        val bodyLength = packetWithHeader.size - HEADER_SIZE_BYTES
        if (bodyLength <= 0) {
            // Log.w(TAG, "Packet with header but no H.265 body.")
            // 即使body为空，也可能需要唤醒等待的readAt以使其检查关闭状态
            // 但如果body为空，我们不应该获取信号量许可或将其放入队列
            // 不过，一个空的 poison pill 可能需要特殊处理
            return
        }

        val body = ByteArray(bodyLength)
        System.arraycopy(packetWithHeader, HEADER_SIZE_BYTES, body, 0, bodyLength)

        try {
            // 尝试获取信号量许可，表示有空间缓冲这个body。如果缓冲区满，会等待一段时间。
            if (!bufferCapacitySemaphore.tryAcquire(body.size, 5, TimeUnit.SECONDS)) {
                Log.e(TAG, "Buffer full for 5s, dropping packet of size ${body.size}. Available permits: ${bufferCapacitySemaphore.availablePermits()}")
                return // 丢弃数据包
            }
            // 成功获取许可，将body放入队列
            packetQueue.put(body)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            Log.e(TAG, "putData interrupted while acquiring semaphore or putting to queue.", e)
            // 如果在获取信号量后但在放入队列前中断，需要释放已获取的许可
            if(bufferCapacitySemaphore.availablePermits() <= maxBufferSizeBytes - body.size) { // A heuristic check
                bufferCapacitySemaphore.release(body.size)
            }
            stateLock.withLock { isClosed = true } // 发生中断，可能无法继续，关闭数据源
        } catch (e: Exception) {
            Log.e(TAG, "Error in putData", e)
            // 确保在发生其他异常时也释放许可（如果已获取）
            bufferCapacitySemaphore.release(body.size)
        }
    }

    @Throws(IOException::class)
    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        if (size <= 0) return 0

        // 检查关闭状态和 position 合法性 (在锁内)
        stateLock.withLock {
            // Requirement 3: 不支持跳跃观看，position 必须是连续的
            if (position != totalBytesEffectivelyServed) {
                Log.e(TAG, "Seek attempt or position mismatch. Expected: $totalBytesEffectivelyServed, Got: $position")
                throw IOException("Seek attempt or position mismatch for live stream. Expected $totalBytesEffectivelyServed, got $position.")
            }
            // 如果已关闭，并且没有待处理的数据包或当前包已读完，则返回EOF
            if (isClosed && packetQueue.isEmpty() && (currentPacket == null || currentPacketOffset >= currentPacket!!.size)) {
                Log.d(TAG, "readAt: EOF indicated (closed, queue empty, current packet exhausted).")
                return -1
            }
        } // 释放 stateLock，允许 putData 继续工作

        var bytesReadThisCall = 0
        var currentOutputOffset = offset

        while (bytesReadThisCall < size) {
            val packetToProcess: ByteArray? // 当前要从中读取数据的包
            val offsetInPacket: Int   // 在 packetToProcess 中的起始读取位置

            // ----- 临界区开始: 访问和修改 currentPacket, currentPacketOffset -----
            stateLock.withLock {
                if (currentPacket == null || currentPacketOffset >= currentPacket!!.size) {
                    // 当前包已耗尽或初始状态，尝试从队列获取新包
                    currentPacket?.let { prevPacket ->
                        if (prevPacket.isNotEmpty()) { // 不为空包（例如POISON_PILL）
                            bufferCapacitySemaphore.release(prevPacket.size) // 释放前一个完整消耗掉的包的许可
                            // Log.d(TAG, "Released ${prevPacket.size} permits. Available: ${bufferCapacitySemaphore.availablePermits()}")
                        }
                    }
                    currentPacket = null // 清除旧包引用

                    try {
                        // 等待一段时间获取新包，允许检查 isClosed 状态
                        currentPacket = packetQueue.poll(100, TimeUnit.MILLISECONDS)
                        currentPacketOffset = 0
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                        Log.w(TAG, "readAt interrupted while polling packet queue.")
                        throw IOException("Read interrupted", e) // 向上抛出，MediaExtractor会处理
                    }

                    // 在获取新包后，再次检查关闭状态
                    if (isClosed && currentPacket == POISON_PILL_PACKET) currentPacket = null // 忽略毒丸
                    if (isClosed && packetQueue.isEmpty() && currentPacket == null) {
                        // 确实没有更多数据了
                    }
                }
                packetToProcess = currentPacket
                offsetInPacket = currentPacketOffset
            }
            // ----- 临界区结束 -----


            if (packetToProcess == null || packetToProcess === POISON_PILL_PACKET) {
                // 队列为空（poll超时返回null）或者拿到的是毒丸（在close时放入以唤醒等待）
                // 检查是否真的已关闭且无数据
                val trulyClosed = stateLock.withLock { isClosed && packetQueue.isEmpty() }
                if (trulyClosed) {
                    return if (bytesReadThisCall > 0) bytesReadThisCall else -1 // 如果已读取一些数据则返回，否则EOF
                }
                return if (bytesReadThisCall > 0) bytesReadThisCall else 0 // 未关闭，但暂时无数据，返回0让MediaExtractor重试
            }

            val bytesToCopyFromPacket = min(packetToProcess.size - offsetInPacket, size - bytesReadThisCall)

            if (bytesToCopyFromPacket <= 0) {
                // 这通常意味着 currentPacket 刚刚被消耗完，循环将在下一次迭代中获取新包
                // 或者 packetToProcess 为空（已被上面的null检查覆盖）
                // 为防止死循环（理论上不应发生），如果读不到数据就退出
                if (bytesReadThisCall > 0) return bytesReadThisCall else continue
            }

            System.arraycopy(packetToProcess, offsetInPacket, buffer, currentOutputOffset, bytesToCopyFromPacket)

            // ----- 临界区开始: 更新 currentPacketOffset 和 totalBytesEffectivelyServed -----
            stateLock.withLock {
                currentPacketOffset += bytesToCopyFromPacket
                totalBytesEffectivelyServed += bytesToCopyFromPacket
            }
            // ----- 临界区结束 -----

            currentOutputOffset += bytesToCopyFromPacket
            bytesReadThisCall += bytesToCopyFromPacket

            if (bytesReadThisCall == size) {
                break // 已满足本次读取请求的大小
            }
        }
        return bytesReadThisCall
    }

    @Throws(IOException::class)
    override fun getSize(): Long {
        // Requirement 4: 对于实时流，大小是未知的。
        return -1L
    }

    @Throws(IOException::class)
    override fun close() {
        Log.d(TAG, "close() called.")
        stateLock.withLock {
            if (isClosed) return
            isClosed = true

            // 释放当前正在处理的数据包（如果存在且未完全处理）的许可
            currentPacket?.let { packet ->
                if (packet.isNotEmpty()) { // 确保不是毒丸
                    // 如果currentPacketOffset < packet.size，说明这个包未消耗完
                    // 但我们关闭时，这个包占用的许可也需要释放
                    // 更准确地说，是packet.size的许可，因为它是作为一个整体被acquire的
                    bufferCapacitySemaphore.release(packet.size)
                    // Log.d(TAG, "Released ${packet.size} permits for currentPacket during close. Available: ${bufferCapacitySemaphore.availablePermits()}")
                }
            }
            currentPacket = null
            currentPacketOffset = 0

            // 清空队列，并释放所有排队数据包的许可
            val packetsToClear = mutableListOf<ByteArray>()
            packetQueue.drainTo(packetsToClear) // 原子地移除所有元素

            var releasedBytesInQueue = 0
            for (packetInQueue in packetsToClear) {
                if (packetInQueue.isNotEmpty()) { // 确保不是毒丸
                    releasedBytesInQueue += packetInQueue.size
                }
            }
            if (releasedBytesInQueue > 0) {
                bufferCapacitySemaphore.release(releasedBytesInQueue)
                // Log.d(TAG, "Released $releasedBytesInQueue permits from queue during close. Available: ${bufferCapacitySemaphore.availablePermits()}")
            }
            packetQueue.clear() // 确保队列为空

            // 放入一个“毒丸”数据包来唤醒任何可能仍在 packetQueue.poll() 中阻塞的 readAt 调用线程
            // 这样它可以检查 isClosed 状态并正确退出。
            // 注意：如果 poll 超时时间很短，这个可能不是必须的，但作为一种健壮的措施可以考虑。
            // 但由于我们使用了 tryAcquire，putData 不会无限阻塞，所以毒丸的主要目的是唤醒readAt。
            packetQueue.offer(POISON_PILL_PACKET) // offer 不会阻塞
        }
        Log.i(TAG, "$TAG closed. Final semaphore permits: ${bufferCapacitySemaphore.availablePermits()}")
    }
}