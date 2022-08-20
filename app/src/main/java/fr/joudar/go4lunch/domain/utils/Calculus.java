package fr.joudar.go4lunch.domain.utils;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class Calculus {

    public static int ratingStarsCalculator(double rating) {
        double value = (rating/5)*3;
        if (value > 2.5)
            return 3;
        else if (value > 1.5)
            return 2;
        else if (value > 0.5)
            return 1;
        else
            return 0;
    }

    public static Location LatLongToLocationConverter(LatLng latLng) {
        Location location = new Location("Location");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }

    public static float distanceBetween(LatLng place1, LatLng place2) {
        return LatLongToLocationConverter(place1).distanceTo(LatLongToLocationConverter(place2));
    }

    public static float distanceBetween(Location place1, LatLng place2) {
        return place1.distanceTo(LatLongToLocationConverter(place2));
    }
}
