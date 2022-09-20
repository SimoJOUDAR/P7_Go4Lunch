package fr.joudar.go4lunch;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.EditText;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.UriMatchers;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dagger.hilt.android.testing.HiltAndroidTest;
import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.services.FirebaseServicesProvider;
import fr.joudar.go4lunch.utils.AndroidTestUtils;
import fr.joudar.go4lunch.viewmodel.HomepageViewModel;

@HiltAndroidTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NavigationTest extends AndroidTestUtils {

    @Before
    public void setUp() throws Exception {
        if(firebaseRepository.getCurrentUser() == null){
            loginUser();
        }
    }

    /***********************************************************************************************
     ** DrawerNav : Display ChosenRestaurant and Logout
     **********************************************************************************************/

    @Test
    public void A_should_display_the_chosen_restaurant() {
        initChosenRestaurant();
        onView(withId(R.id.activity_homepage)).perform(DrawerActions.open());
        final String chosenRestaurantName = firebaseRepository.getCurrentUser().getChosenRestaurantName();
        onView(withId(R.id.drawer_nav)).perform(NavigationViewActions.navigateTo(R.id.your_lunch));
        waitForUIUpdate();
//        onView(withId(R.id.activity_homepage)).check(matches(isDisplayed()));
        onView(withId(R.id.restaurant_details_layout)).check(matches(isDisplayed()));
        waitForUIUpdate();
        onView(withId(R.id.restaurant_name)).check(matches(withText(containsString(chosenRestaurantName))));
    }

    @Test
    public void B_should_logout() {
        onView(withId(R.id.activity_homepage)).perform(DrawerActions.open());
        onView(withId(R.id.drawer_nav)).perform(NavigationViewActions.navigateTo(R.id.logout));
        waitForUIUpdate();
        Assert.assertNull(FirebaseAuth.getInstance().getCurrentUser());
        onView(withId(R.id.login_view_layout)).check(matches(isDisplayed()));
    }

    /***********************************************************************************************
     ** MapFragment and RestaurantDetailsFragment
     **********************************************************************************************/

    @Test
    public void C_should_select_a_restaurant_from_the_map_and_show_its_details() throws UiObjectNotFoundException {
        waitForUIUpdate();
        waitForUIUpdate();
        waitForUIUpdate();

        // Randomly selecting a place
        final Place fetchedRestaurant = HomepageViewModel.getLastRequestResult()[0];

        // Clearing LikedRestaurantsIdList and ChosenRestaurant
        waitForUIUpdate();
        firebaseRepository.setLikedRestaurantsIdList(new ArrayList<>());
        firebaseRepository.updateCurrentUserData(FirebaseServicesProvider.LIKED_RESTAURANTS_ID_LIST, new ArrayList<>());
        waitForUIUpdate();
        firebaseRepository.resetChosenRestaurant();
        waitForUIUpdate();

        // We locate the Marker of the selected place
        final UiObject restaurantMarker = uiDevice.findObject(new UiSelector().descriptionContains(fetchedRestaurant.getName()));
//        final UiObject mapViewContainer = uiDevice.findObject(new UiSelector().resourceId(BuildConfig.APPLICATION_ID + ":id/map_fragment_container"));
//        mapViewContainer.pinchOut(50, 100);  // to zoom in before selecting the right marker
//        mapViewContainer.click(); // Change the focus to make the info window disappear.
        restaurantMarker.click(); // Click on the right Marker
        uiDevice.click(uiDevice.getDisplayWidth() / 2, restaurantMarker.getBounds().top - 20); // Click on the info window
        waitForUIUpdate();
        waitForUIUpdate();
        waitForUIUpdate();
        onView(withId(R.id.restaurant_details_layout)).check(matches(isDisplayed()));  // Make sure that RestaurantDetailsFragment is now displayed
        onView(withId(R.id.restaurant_name)).check(matches(withText(containsString(fetchedRestaurant.getName())))); // Make sure that RestaurantDetailsFragment shows the right restaurant

        // Choosing a restaurant
        assertTrue(firebaseRepository.getCurrentUser().getChosenRestaurantId().isEmpty());
        onView(withId(R.id.btn_select_favorite_restaurant)).perform(click());
        assertEquals(firebaseRepository.getCurrentUser().getChosenRestaurantId(), fetchedRestaurant.getId());
        // Undo the choice
        onView(withId(R.id.btn_select_favorite_restaurant)).perform(click());
        assertTrue(firebaseRepository.getCurrentUser().getChosenRestaurantId().isEmpty());
        waitForUIUpdate();

        // Liking a restaurant
        onView(withId(R.id.likeButton)).perform(click());
        waitForUIUpdate();
        assertTrue(firebaseRepository.getCurrentUser().getLikedRestaurantsIdList().contains(fetchedRestaurant.getId()));
        // Unlike
        onView(withId(R.id.likeButton)).perform(click());
        waitForUIUpdate();
        assertTrue(firebaseRepository.getCurrentUser().getLikedRestaurantsIdList().isEmpty());

        // Calling the restaurant
        Intents.init();
        intending(hasData(UriMatchers.hasScheme("tel")))
                .respondWith(
                        new Instrumentation.ActivityResult(
                                Activity.RESULT_OK, new Intent(Intent.ACTION_VIEW, Uri.parse("tel:"))));
        onView(withId(R.id.callButton)).perform(click());
        intended(hasData(UriMatchers.hasScheme("tel")));
        waitForUIUpdate();

        // Launching the restaurant website
        intending(hasData(UriMatchers.hasScheme("https")))
                .respondWith(
                        new Instrumentation.ActivityResult(
                                Activity.RESULT_OK, new Intent(Intent.ACTION_VIEW, Uri.parse("http:"))));
        onView(withId(R.id.websiteButton)).perform(click());
        intended(hasData(UriMatchers.hasScheme("https")));

        Intents.release();
    }

    /***********************************************************************************************
     ** RestaurantListFragment
     **********************************************************************************************/

    @Test
    public void D_should_click_item_from_restaurants_list_and_open_restaurant_details() {
        waitForUIUpdate();
        onView(withId(R.id.restaurantsListFragment)).perform(click());
        waitForUIUpdate();
        onView(withId(R.id.restaurants_list_fragment_layout)).check(matches(isDisplayed()));
        waitForUIUpdate();
        onView(withId(R.id.recyclerview)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        waitForUIUpdate();
        onView(withId(R.id.restaurant_details_layout)).check(matches(isDisplayed()));
    }

    /***********************************************************************************************
     ** Search field
     **********************************************************************************************/

    @Test
    public void E_should_search_a_restaurant() {
        waitForUIUpdate();
        onView(withId(R.id.search)).perform(click());
        waitForUIUpdate();
        onView(isAssignableFrom(EditText.class)).perform(typeTextIntoFocusedView("Croco"));
        waitForUIUpdate();
        onView(withId(R.id.autocomplete_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        waitForUIUpdate();
        onView(withId(R.id.restaurant_details_layout)).check(matches(isDisplayed()));
    }

    /***********************************************************************************************
     ** ColleaguesListFragment
     **********************************************************************************************/

    @Test
    public void F_should_contains_colleagues() throws InterruptedException {
        waitForUIUpdate();
        onView(withId(R.id.colleaguesListFragment)).perform(click());
        waitForUIUpdate();
        final List<User> currentUserWorkmates = getCurrentUserWorkmates();
        waitForUIUpdate();
        onView(withId(R.id.recyclerview)).check(recyclerViewItemCount(currentUserWorkmates.size()));
        waitForUIUpdate();
    }

    @Test
    public void G_should_display_colleague_chosen_restaurant() throws InterruptedException {
        onView(withId(R.id.colleaguesListFragment)).perform(click());
        waitForUIUpdate();
        final List<User> currentUserWorkmates = getCurrentUserWorkmates();
        onView(withId(R.id.recyclerview)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        waitForUIUpdate();
        onView(withId(R.id.restaurant_details_layout)).check(matches(isDisplayed()));
        waitForUIUpdate();
        final User randomWorkmate = currentUserWorkmates.get(0);
        waitForUIUpdate();
        onView(withId(R.id.restaurant_name)).check(matches(withText(containsString(randomWorkmate.getChosenRestaurantName()))));
    }
}
