package com.android.weatherapp

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import org.junit.Rule
import org.junit.Test

class MainActivityTest {
    @get:Rule
    val composeRule = createComposeRule()
    private val viewModel = MainViewModel()

    @Test
    fun print_myScreen() {
        composeRule.setContent {
            LiveDataComponent(viewModel = viewModel)
        }
        composeRule.onRoot(useUnmergedTree = true).printToLog("FindMyIPTestTag")
    }
}