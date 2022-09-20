package fr.joudar.go4lunch.utils;

import android.location.Location;

import java.util.Arrays;
import java.util.Map;

import fr.joudar.go4lunch.domain.models.Autocomplete;
import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.utils.Callback;

public interface TestUtils {

    /***********************************************************************************************
     ** Dummy User
     **********************************************************************************************/
    User dummyUser = new User(
            "id",
            "username",
            "user@email.com",
            "avatar.url/photo",
            "workplaceId",
            "workplaceName",
            "workplaceAddress",
            "chosenRestaurantId",
            "chosenRestaurantName",
            "chosenRestaurantAddress",
            Arrays.asList("likedRestaurant1", "likedRestaurant2", "likedRestaurant3")
            );


    Callback<User[]> dummyUsersCallback = new Callback<User[]>() {
        @Override
        public void onSuccess(User[] results) {}

        @Override
        public void onFailure() {}
    };

    Callback<Map<String, Integer>> colleaguesDistributionCallback = new Callback<Map<String, Integer>>() {
        @Override
        public void onSuccess(Map<String, Integer> results) {}

        @Override
        public void onFailure() {}
    };

    // For Autocomplete test
    String dummyAutocompleteInput = "dummy_input";
    Location dummyLocation = new Location("dummyProvider");
    String dummySearchRadius = "dummySearchRadius";
    Boolean dummyIsFiltered = true;
    Boolean dummyIsNotFiltered = false;
    Callback<Autocomplete[]> dummyAutoCompleteCallback = new Callback<Autocomplete[]>() {
        @Override
        public void onSuccess(Autocomplete[] results) {}

        @Override
        public void onFailure() {}
    };

    Callback<Place[]> dummyNearbysearchCallback = new Callback<Place[]>() {
        @Override
        public void onSuccess(Place[] results) {}

        @Override
        public void onFailure() {}
    };

    String dummyPlaceId = "dummyPlaceId";
    Callback<Place> dummyPlaceDetailsCallback = new Callback<Place>() {
        @Override
        public void onSuccess(Place results) {}

        @Override
        public void onFailure() {}
    };
}
