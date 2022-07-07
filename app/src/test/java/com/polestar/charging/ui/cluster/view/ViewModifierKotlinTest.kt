package com.polestar.charging.ui.cluster.view

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

//https://developer.android.com/kotlin/coroutines/test
//advanceUntilIdle
@ExperimentalCoroutinesApi
class ViewModifierKotlinTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun taskOnly1() = runTest(testDispatcher) {
        var result = ""

        val task = MockRunTask("task1", 1000) {
            result = it
        }

        ViewModifierKotlin(testScope).appendTaskToTail(task)
        advanceUntilIdle()
        Assert.assertEquals("task1", result)
    }

    @Test
    fun taskOnly2() = runTest(testDispatcher) {
        var result = ""

        ViewModifierKotlin(testScope).let {
            it.appendTaskToTail(MockRunTask("task1", 0) {
                result += it
            })
            it.appendTaskToTail(MockRunTask("task2", 0) {
                result += it
            })
        }

        advanceUntilIdle()
        Assert.assertEquals("task1task2", result)
    }

    @Test
    fun taskOnly3() = runTest(testDispatcher) {
        var result = ""

        ViewModifierKotlin(testScope).let {
            it.appendTaskToTail(MockRunTask("task1", 100) {
                result += it
            })
            it.appendTaskToTail(MockRunTask("task2", 100) {
                result += it
            })
            it.appendTaskToTail(MockRunTask("task3", 100) {
                result += it
            })
        }

        advanceUntilIdle()
        Assert.assertEquals("task1task3", result)
    }


    class MockRunTask(
        val name: String,
        val durationMs: Long,
        val callback: ((name: String) -> Unit)
    ) : Runnable, ViewModifierKotlin.WorkTask {
        override fun run() {
            callback.invoke(name)
        }

        override suspend fun work(function: () -> Unit) {
            delay(durationMs)
            run()
            function.invoke()
        }
    }

}