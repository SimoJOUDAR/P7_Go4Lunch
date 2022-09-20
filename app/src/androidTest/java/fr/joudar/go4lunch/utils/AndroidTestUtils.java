package fr.joudar.go4lunch.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;

import javax.inject.Inject;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import fr.joudar.go4lunch.BuildConfig;
import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.utils.Callback;
import fr.joudar.go4lunch.repositories.FirebaseServicesRepository;
import fr.joudar.go4lunch.ui.activities.AuthenticationActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@HiltAndroidTest
public abstract class AndroidTestUtils {

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public ActivityScenarioRule<AuthenticationActivity> activityScenarioRule =
            new ActivityScenarioRule<>(AuthenticationActivity.class);

    @Rule
    public GrantPermissionRule runtimePermissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Inject public FirebaseServicesRepository firebaseRepository;

    protected final Context context = ApplicationProvider.getApplicationContext();

    public UiDevice uiDevice;

    @Before
    public void init() {
        if (firebaseRepository == null) {
            hiltRule.inject();
        }
        if (uiDevice == null) {
            uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        }
    }

    public void waitForUIUpdate(){
        uiDevice.waitForWindowUpdate(BuildConfig.APPLICATION_ID, 5000);
    }

    public void loginUser() throws UiObjectNotFoundException {
        waitForUIUpdate();
        onView(ViewMatchers.withId(R.id.google_login_btn)).perform(click());
        waitForUIUpdate();
        if (uiDevice.findObject(new UiSelector().text("joudar mohamed")).exists()) {
            uiDevice.findObject(new UiSelector().text("joudar mohamed")).click();
        }
        waitForUIUpdate();
    }

    public void initChosenRestaurant() {
        final User currentUser = firebaseRepository.getCurrentUser();
        waitForUIUpdate();
        try {
            if (currentUser.getChosenRestaurantId().isEmpty()) {
                currentUser.setChosenRestaurantId("ChIJcdAb-E3IlkcRFuzy9nI8oBg");
                currentUser.setChosenRestaurantName("Au Crocodile");
            }

        } catch (NullPointerException e) {
            waitForUIUpdate();
        }
    }

    public List<User> getCurrentUserWorkmates() throws InterruptedException {
        final Object threadsLocks = new Object();
        final List<User> currentUserWorkmates = new ArrayList<>();

        firebaseRepository.getColleagues(
                new Callback<User[]>() {
                    @Override
                    public void onSuccess(User[] users) {
                        Collections.addAll(currentUserWorkmates, users);
                        synchronized (threadsLocks) {
                            threadsLocks.notify(); // unblock the thread
                        }
                    }

                    @Override
                    public void onFailure() {}
                });

        synchronized (threadsLocks) {
            threadsLocks.wait(5000); // block the tread until the workmates data have been fetched
        }
        return currentUserWorkmates;
    }

    public ViewAssertion recyclerViewItemCount(int itemCount) {
        return new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                Log.d("Workmates_tests", "recyclerViewWithItemCount");
                if (noViewFoundException != null)
                    throw noViewFoundException;
                final RecyclerView.Adapter adapter = ((RecyclerView) view).getAdapter();
                Assert.assertEquals(adapter.getItemCount(), itemCount);
            }
        };
    }
}