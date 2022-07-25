package fr.joudar.go4lunch.domain.services;

import android.location.Location;

public interface CurrentLocationProvider {
    void getCurrentCoordinates(OnCoordinatesResultListener resultListener);

    interface OnCoordinatesResultListener {
        void onResult(Location location);
    }
}