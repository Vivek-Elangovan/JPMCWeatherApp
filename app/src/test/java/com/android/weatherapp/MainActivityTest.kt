package com.android.weatherapp

import android.content.Context
import junit.framework.TestCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.mock

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(JUnit4::class)
class MainActivityTest : TestCase() {

    private lateinit var viewModel: MainViewModel
    private val activity = MainActivity()
    private lateinit var mockContext: Context

    @Before
    fun setup() {
        mockContext = mock(Context::class.java)
        viewModel = MainViewModel()
    }

    @Test
    fun `OnLaunch execute init`() {
        activity.init()
    }

    @Test
    fun check_isDeviceOnline() {
        assertTrue(activity.isOnline(mockContext))
    }
}