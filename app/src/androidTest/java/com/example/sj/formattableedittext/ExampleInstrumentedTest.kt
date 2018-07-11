package com.example.sj.formattableedittext

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.TypeTextAction
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.KeyEvent
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    var mActivityTestRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    // todo vorobei type into and check if strings are the same as expected

    @Test fun addText(){
        type("Hi, @Dm")
        clickFirstItemAtList()
        type("! How are you? @Alexa")
        clickFirstItemAtList()
        type("mentioned you!")
//        checkText("Hi,@Dmitryw! How are you? @Alexandr Telegin mentioned you!")
        sleep(5)
    }

    @Test fun removeText(){
        type("Hi, @Dm")
        clickFirstItemAtList()
        type("! How are you?")
        onView(withId(R.id.editText)).perform(replaceText(""))
        sleep(5)
    }

    @Test fun removeLastFromName(){
        type("Hi @Dm")
        clickFirstItemAtList()
        type(" ")
        removeLastSymbols(3)
        sleep(3)
    }

    /**
     * Hi, @Dm
     * clear text
     */
    @Test fun openList() {
        type("Hi, ")
        listHidden()
        type("@")
        listDisplayed()
        type("D")
        listDisplayed()
        type("m")
        listDisplayed()

        listHidden()

        type("@")
        listDisplayed()
        type("D")
        listDisplayed()
        type("m")
        listDisplayed()
    }

    /**
     * '@' at first of string
     * select friend
     */
    @Test fun firstAt() {
        clearInput()
        type("@")
        listDisplayed()
        type("alexa")
        listDisplayed()
        clickFirstItemAtList()
        listHidden()
        checkText("@Alexandr Telegin ")

        removeLastSymbols(1)
        listHidden()

        removeLastSymbols(1)
        listHidden()

        clickFirstItemAtList()
        listHidden()
        checkText("@Alexandr Telegin ")
    }

    @Test fun sendMail() {
        val first = "Hi, this is my mail: mymail@"
        val second = "gmail.com"
        type(first)
        listHidden()
        type(second)
        checkText("$first$second")
    }

    @Test fun removeMention() {
        type("Hi, @x")
        listDisplayed()
        removeLastSymbols(2)
        checkText("Hi, ")
        listHidden()
    }

    /**
     * Remove space between 2 mentions
     */
    @Test fun mentionTwoAndEditMiddle() {
        type("Hi, @Alexa")
        listDisplayed()
        clickFirstItemAtList()
        checkText("Hi, @Alexandr Telegin ")
        listHidden()

        type(", @Too")
        listDisplayed()
        clickFirstItemAtList()
        checkText("Hi, @Alexandr Telegin, @Tookey Williams ")
        listHidden()
    }

    @Test fun editBetweenTwoMentions() {
        type("Hi, @Alexa")
        clickFirstItemAtList()
        checkText("Hi, @Alexandr Telegin ")
        type(", @Too")
        clickFirstItemAtList()
        checkText("Hi, @Alexandr Telegin, @Tookey Williams ")

        moveLeft(17)
        type(" and ")
        listHidden()
        moveLeft(10)
        removeLastSymbols()
    }


    fun listDisplayed() = onView(withId(R.id.tagFriendsRecycler)).check(matches(isDisplayed()))

    fun listHidden() = onView(withId(R.id.tagFriendsRecycler)).check(matches(not(isDisplayed())))

    fun removeLastSymbols(n: Int = 1) {
        (1..n).forEach { onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_DEL)) }
    }

    fun moveLeft(n: Int) {
        (1..n).forEach { onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_SYSTEM_NAVIGATION_LEFT)) }
    }

    fun clickFirstItemAtList() {
        onView(withId(R.id.tagFriendsRecycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition<FriendsAdapter.ItemViewHolder>(0, click()))
    }

    fun type(text: String) {
        onView(withId(R.id.editText)).perform(closeSoftKeyboard())
        onView(withId(R.id.editText)).perform(click())
        onView(withId(R.id.editText)).perform(TypeTextAction(text)).perform(closeSoftKeyboard())
    }

    fun ty(text: String) {
        onView(withId(R.id.editText)).perform(TypeTextAction(text))
    }

    fun checkText(text: String) {
        onView(withId(R.id.editText)).check(matches(withText(text)))
    }

    fun clearInput() {
        onView(withId(R.id.editText))
                .perform(closeSoftKeyboard())
                .perform(replaceText(""))
    }

    fun sleep(seconds: Int) {
        try {
            Thread.sleep(seconds * 1000L)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }
}
