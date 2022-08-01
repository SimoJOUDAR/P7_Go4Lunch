package fr.joudar.go4lunch.domain.core;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;

import javax.inject.Inject;

import fr.joudar.go4lunch.domain.services.CurrentLocationProvider;

public class CurrentLocationHandler implements CurrentLocationProvider {

    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final LocationPermissionHandler locationPermissionHandler;
    private Location lastRecordedLocation;
    private OnCoordinatesResultListener onResultListener;

    @Inject
    public CurrentLocationHandler(
            Activity activity, LocationPermissionHandler locationPermissionHandler) {
        this.locationPermissionHandler = locationPermissionHandler;
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        initLocationProvider();
    }

    /**
     * This method make sure that the a location has been requested at least once before requesting
     * the last know location, otherwise the last know location could always return null until a
     * client connect and request the current location.
     */
    @SuppressLint("MissingPermission")
    private void initLocationProvider() {
        final LocationRequest locationRequest =
                LocationRequest.create()
                        .setInterval(100)
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (locationPermissionHandler.hasPermission()) {
            fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            lastRecordedLocation = locationResult.getLastLocation();
                            if (onResultListener != null) {
                                onResultListener.onResult(lastRecordedLocation);
                            }
                            fusedLocationProviderClient.removeLocationUpdates(this);
                        }
                    },
                    Looper.getMainLooper());
        } else locationPermissionHandler.requestPermission(this::initLocationProvider);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void getCurrentCoordinates(OnCoordinatesResultListener resultListener) {
        Log.d("FusedLocationAdapter", "getCurrentCoordinates");
        if (locationPermissionHandler.hasPermission()) {
            Log.d("FusedLocationAdapter", "----HAS PRM----");
            fusedLocationProviderClient
                    .getLastLocation()
                    .addOnCompleteListener(getOnCompleteResultListener(resultListener));
        } else locationPermissionHandler.requestPermission(() -> getCurrentCoordinates(resultListener));
    }

    private OnCompleteListener<Location> getOnCompleteResultListener(OnCoordinatesResultListener resultListener) {
        return task -> {
            final Location location = task.getResult();
            if (task.isSuccessful() && location != null) {
                resultListener.onResult(location);
                lastRecordedLocation = location;
            } else if (lastRecordedLocation != null) {
                resultListener.onResult(lastRecordedLocation);
            } else onResultListener = resultListener;
        };
    }

    // TODO: Test - Location permission before entrance
    @Override
    public void hasLocationPermission(){
        if (!locationPermissionHandler.hasPermission()) {
            initLocationProvider();
        }
    }
}
