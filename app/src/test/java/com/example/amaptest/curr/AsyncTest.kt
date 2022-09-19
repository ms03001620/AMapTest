package test.package1

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

@OptIn(ExperimentalCoroutinesApi::class)
class AsyncTest {
    private val coroutineDispatcher = TestCoroutineScheduler()//StandardTestDispatcher()

    @Test
    fun syncLoad() {
        val list = listOf(1, 2, 3, 4, 5)
        runTest(coroutineDispatcher) {
            val time = measureTimeMillis {
                list.map {
                    Pair(it, syncTask(it, coroutineDispatcher))
                }.forEach {
                    println("i:${it.first}, v:${it.second}")
                }
            }
            coroutineDispatcher.advanceUntilIdle()

            println("time:$time")
        }
    }


    @Test
    fun asyncLoad() {
        val list = listOf(1, 2, 3, 4, 5)
        runTest {
            val time = measureTimeMillis {
                list.map {
                    async {
                        Pair(it, syncTask(it))
                    }
                }.awaitAll().toMap().forEach {
                    println("i:${it.key}, v:${it.value}")
                }
            }
            println("time:$time")
        }
    }


    suspend fun syncTask(index: Int, d: CoroutineContext= Dispatchers.IO) = withContext(d) {
        delay(10000)
        return@withContext "a$index"
    }
}