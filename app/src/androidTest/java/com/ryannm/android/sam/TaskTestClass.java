package com.ryannm.android.sam;

import android.support.test.espresso.FailureHandler;
import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class TaskTestClass {

    @Rule
    public ActivityTestRule<SinglePaneActivity> mainActivityTestRule = new ActivityTestRule<>(SinglePaneActivity.class);

    @Test
    public void anything () {
        onView(withId(R.id.fab))
                .perform(ViewActions.click());
        onView(withId(R.id.task_name))
                .perform(ViewActions.typeText("xyz"))
                .withFailureHandler(new FailureHandler() {
                    @Override
                    public void handle(Throwable error, Matcher<View> viewMatcher) {
                        error.printStackTrace();
                    }
                })
                .check(matches(not(isDisplayed())));
    }

}
