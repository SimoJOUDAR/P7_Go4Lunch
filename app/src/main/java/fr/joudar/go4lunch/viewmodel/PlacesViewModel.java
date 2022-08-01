package fr.joudar.go4lunch.viewmodel;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.ViewModel;
import com.google.android.gms.maps.model.LatLng;
import java.time.LocalDateTime;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.lifecycle.HiltViewModel;
import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.utils.Callback;
import fr.joudar.go4lunch.domain.utils.DistanceOperations;
import fr.joudar.go4lunch.repositories.FirebaseServicesRepository;
import fr.joudar.go4lunch.repositories.NearbysearchRepository;

@HiltViewModel
public class PlacesViewModel extends ViewModel {

    private final NearbysearchRepository nearbysearchRepository;
    private LatLng lastLocation;
    private static Place[] lastResults;
    private LocalDateTime timeOfLastRequest;;
    private final FirebaseServicesRepository firebaseServicesRepository;


    @Inject
    public PlacesViewModel(NearbysearchRepository nearbysearchRepository, FirebaseServicesRepository firebaseServicesRepository) {
        this.nearbysearchRepository = nearbysearchRepository;
        this.firebaseServicesRepository = firebaseServicesRepository;
    }

    public void getNearbyRestaurant(LatLng currentLocation, Callback<Place[]> callback) {
        if (isCacheUpToDate(currentLocation)) {
            callback.onSuccess(lastResults);
        }
        else {
            nearbysearchRepository.getNearbyRestaurant(currentLocation,
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
    private boolean isCacheUpToDate(LatLng currentLocation) {
        return lastLocation != null
                && DistanceOperations.getLatLngDistance(lastLocation, currentLocation) < 50
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
}
