package fr.joudar.go4lunch.viewmodel;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import java.time.LocalDateTime;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import fr.joudar.go4lunch.domain.models.Autocomplete;
import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.utils.Callback;
import fr.joudar.go4lunch.repositories.AutocompleteRepository;
import fr.joudar.go4lunch.repositories.FirebaseServicesRepository;
import fr.joudar.go4lunch.repositories.NearbysearchRepository;
import fr.joudar.go4lunch.repositories.PlaceDetailsRepository;

@HiltViewModel
public class HomepageViewModel extends ViewModel {

    private final FirebaseServicesRepository firebaseServicesRepository;
    private final NearbysearchRepository nearbysearchRepository;
    private final AutocompleteRepository autocompleteRepository;
    private final PlaceDetailsRepository placeDetailsRepository;

    private Location lastLocation;
    private static Place[] lastResults;
    private LocalDateTime timeOfLastRequest;

    //TODO: To define search radius in SettingsFragment and store it in SharedPreferences

    @Inject
    public HomepageViewModel(FirebaseServicesRepository firebaseServicesRepository,
                             NearbysearchRepository nearbysearchRepository,
                             AutocompleteRepository autocompleteRepository,
                             PlaceDetailsRepository placeDetailsRepository) {
        this.firebaseServicesRepository = firebaseServicesRepository;
        this.nearbysearchRepository = nearbysearchRepository;
        this.autocompleteRepository = autocompleteRepository;
        this.placeDetailsRepository = placeDetailsRepository;
    }

    /***********************************************************************************************
     ** Firebase
     **********************************************************************************************/

    public void initListener(FirebaseAuth.AuthStateListener authStateListener){
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    public User getCurrentUser() {
        return firebaseServicesRepository.getCurrentUser();
    }

    public MutableLiveData<User> getLiveCurrentUser() {
        return firebaseServicesRepository.getLiveCurrentUser();
    }

    public void getColleagues(Callback<User[]> callback) {
        firebaseServicesRepository.getColleagues(callback);
    }

    public void getColleaguesByRestaurant(String restaurantId, Callback<User[]> callback) {
        firebaseServicesRepository.getColleaguesByRestaurant(restaurantId, callback);
    }

    public void updateAllCurrentUserData() {
        firebaseServicesRepository.updateAllCurrentUserData();
    }

    public void resetChosenRestaurant(){
        firebaseServicesRepository.resetChosenRestaurant();
    }

    public void updateCurrentUserData(String dataType, Object data){
        firebaseServicesRepository.updateCurrentUserData(dataType, data);
    }

    public boolean isCurrentUserNew() {
        return firebaseServicesRepository.isCurrentUserNew();
    }

    public void logout() {
        firebaseServicesRepository.logout();
    }

    public void deleteCurrentUserAccount(Callback<Boolean> callback){
        firebaseServicesRepository.deleteCurrentUserAccount(callback);
    }

    public String getWorkplaceId(){
        return firebaseServicesRepository.getWorkplaceId();
    }


    /***********************************************************************************************
     ** NearbySearch
     **********************************************************************************************/

    public void getNearbyRestaurant(Location currentLocation, String radius, Callback<Place[]> callback) {
        if (isCacheUpToDate(currentLocation)) {
            callback.onSuccess(lastResults);
        }
        else {
            nearbysearchRepository.getNearbyRestaurant(currentLocation, radius,
                    new Callback<Place[]>() {
                        @Override
                        public void onSuccess(Place[] places) {
                            lastResults = places;
                            lastLocation = currentLocation;
                            timeOfLastRequest = LocalDateTime.now();
                            callback.onSuccess(places);
                        }

                        @Override
                        public void onFailure() {
                            callback.onFailure();
                        }
                    });
        }
    }

    // Returns true if cache has been update less than 5 min ago and less than 50 meters location distance
    private boolean isCacheUpToDate(Location currentLocation) {
        return lastLocation != null
                && lastLocation.distanceTo(currentLocation) < 50
                && timeOfLastRequest.plusMinutes(5).isAfter(LocalDateTime.now());
    }

    public void getColleaguesDistributionOverRestaurants(Callback<Map<String, Integer>> callback) {
        Log.d("PlacesViewModel", "getColleaguesDistributionOverRestaurants");
        firebaseServicesRepository.getColleaguesDistributionOverRestaurants(callback);
    }

    @VisibleForTesting
    public static Place[] getLastRequestResult() {
        return lastResults;
    }

    /***********************************************************************************************
     ** Autocomplete
     **********************************************************************************************/

    //Fetches the Autocomplete results and passes them to the callback.
    // The boolean isFiltered is "true" for Restaurants autocomplete, "false" for Workplace autocomplete.
    public void getAutocompletes(String input, Location location, String searchRadius, boolean isFiltered, Callback<Autocomplete[]> callback) {
        autocompleteRepository.getAutocompletes(input, location, searchRadius, isFiltered, new Callback<Autocomplete[]>() {
            @Override
            public void onSuccess(Autocomplete[] results) {
                callback.onSuccess(results);
            }

            @Override
            public void onFailure() {
                callback.onFailure();
            }
        });
    }

    /***********************************************************************************************
     ** PlaceDetails
     **********************************************************************************************/

    public void getPlaceDetails(String placeId, Callback<Place> callback) {
        placeDetailsRepository.getPlaceDetails(placeId, callback);
    }
}
