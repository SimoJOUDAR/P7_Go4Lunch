package fr.joudar.go4lunch.viewmodel;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import fr.joudar.go4lunch.domain.core.notification.NotificationWorker;
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

    public void updateAllCurrentUserData(User user) {
        firebaseServicesRepository.updateAllCurrentUserData(user);
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

    public String getWorkplaceName() {
        return firebaseServicesRepository.getWorkplaceName();
    }

    public String getWorkplaceAddress() {
        return firebaseServicesRepository.getWorkplaceAddress();
    }

    public void setUsername(String username) {
        firebaseServicesRepository.setUsername(username);
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
    public void getAutocompletes(@NonNull String input, @NonNull Location location, @NonNull String searchRadius, boolean isFiltered, Callback<Autocomplete[]> callback) {
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

    /***********************************************************************************************
     ** Notification
     **********************************************************************************************/

    public void scheduleNotificationJob(Context context, @Nullable Calendar dueDate) {
        final String JOB_TAG = "GO4LUNCH_NOTIFICATION_WORKER";
        Calendar currentDate = Calendar.getInstance();
        long timeDiff =  dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        final PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(NotificationWorker.class,24, TimeUnit.HOURS)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .addTag(JOB_TAG)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(JOB_TAG, ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest);
    }
}
