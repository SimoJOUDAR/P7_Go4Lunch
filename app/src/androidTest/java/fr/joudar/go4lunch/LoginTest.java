package fr.joudar.go4lunch;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.uiautomator.UiObjectNotFoundException;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import dagger.hilt.android.testing.HiltAndroidTest;
import fr.joudar.go4lunch.utils.AndroidTestUtils;

@HiltAndroidTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LoginTest extends AndroidTestUtils {


    @Test
    public void a_should_login_with_google() throws UiObjectNotFoundException {
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            AuthUI.getInstance().signOut(ApplicationProvider.getApplicationContext());
        }
        loginUser();
        waitForUIUpdate();
        onView(withId(R.id.activity_homepage)).check(matches(isDisplayed()));
        waitForUIUpdate();
        AuthUI.getInstance().signOut(ApplicationProvider.getApplicationContext());
    }

    @Test
    public void b_should_display_login_error_message() {
        waitForUIUpdate();
        pressBack(); // cancel the login process
        onView(withText(ApplicationProvider.getApplicationContext()
                .getResources()
                .getString(R.string.login_failed_unknown_error))).check(matches(isDisplayed()));
    }

    @Test
    public void c_should_retry_to_login() {
        waitForUIUpdate();
        onView(withId(R.id.login_view_layout)).check(matches(isDisplayed()));
        pressBack();
        onView(withId(R.id.activity_authentication_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.retry_login_btn)).perform(click());
        onView(withId(R.id.login_view_layout)).check(matches(isDisplayed()));
    }
}