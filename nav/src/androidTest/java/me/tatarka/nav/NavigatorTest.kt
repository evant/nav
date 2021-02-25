package me.tatarka.nav

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

enum class Page { ONE, TWO }

@RunWith(AndroidJUnit4::class)
class NavigatorTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun saves_back_stack_state() {
        val pages = MutableStateFlow(listOf(Page.ONE))

        composeTestRule.setContent {
            Navigator(
                pages = pages.collectAsState().value,
                onPopPage = { pages.value = pages.value.dropLast(1) }
            ) {
                when (pages.value.last()) {
                    Page.ONE -> {
                        var count by rememberSaveable { mutableStateOf(0) }
                        Column {
                            BasicText(
                                modifier = Modifier.clickable(onClick = { count++ }),
                                text = "Page 1"
                            )
                            BasicText(modifier = Modifier.testTag("count"), text = count.toString())
                        }

                    }
                    Page.TWO -> {
                        BasicText(text = "Page 2")
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Page 1").performClick()

        pages.value = listOf(Page.ONE, Page.TWO)
        composeTestRule.waitForIdle()
        pages.value = listOf(Page.ONE)

        composeTestRule.onNodeWithTag("count").assertTextEquals("1")
    }

    @Test
    fun moves_back_stack_state_on_pop() {
        val pages = MutableStateFlow(listOf(Page.ONE, Page.TWO))

        composeTestRule.setContent {
            Navigator(
                pages = pages.collectAsState().value,
                onPopPage = { pages.value = pages.value.dropLast(1) }
            ) {
                when (pages.value.last()) {
                    Page.ONE -> {
                        BasicText(text = "Page 1")
                    }
                    Page.TWO -> {
                        var count by rememberSaveable { mutableStateOf(0) }
                        Column {
                            BasicText(
                                modifier = Modifier.clickable(onClick = { count++ }),
                                text = "Page 2"
                            )
                            BasicText(modifier = Modifier.testTag("count"), text = count.toString())
                        }
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Page 2").performClick()

        pages.value = listOf(Page.ONE)
        composeTestRule.waitForIdle()
        pages.value = listOf(Page.ONE, Page.TWO)

        composeTestRule.onNodeWithTag("count").assertTextEquals("0")
    }
}