package com.google.developer.taskmaker;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.ComponentNameMatchers.hasShortClassName;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Vipin K R on 13-05-2017.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoUiIntentTest {
    /* Instantiate an IntentsTestRule object. */
    @Rule
    public IntentsTestRule<MainActivity> mIntentsRule =new IntentsTestRule<>(MainActivity.class);

    //Write a UI test to validate that clicking the floating action button results in displaying the AddTaskActivity

    @Test
    public void verifyAddTaskActivityLaunch() {
        // Type text and then press the button.
        onView(withId(R.id.fab)).perform(click());
        intended(hasComponent(hasShortClassName(".AddTaskActivity")));
    }

}
