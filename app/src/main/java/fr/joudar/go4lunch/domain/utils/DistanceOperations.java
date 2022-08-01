package fr.joudar.go4lunch.domain.utils;

import android.annotation.SuppressLint;
import android.location.Location;
import com.google.android.gms.maps.model.LatLng;

public class DistanceOperations {

  // Returns the meter distance between two LatLng locations
  public static Float getLatLngDistance(LatLng start, LatLng end) {

    Location startLoc = new Location("Location Provider");
    startLoc.setLatitude(start.latitude);
    startLoc.setLongitude(start.longitude);

    Location endLoc = new Location("Location Provider");
    endLoc.setLatitude(end.latitude);
    endLoc.setLongitude(end.longitude);

    return startLoc.distanceTo(endLoc);
  }


  public static String convertToDisplayableDistance(Float distanceInMeter){
    if(distanceInMeter == null) return "0.0m";
    if(distanceInMeter < 100) return Math.round(distanceInMeter) + " m";
    if(distanceInMeter < 1000) return Math.round(distanceInMeter) + "m";
    if((distanceInMeter % 1000) < 100 ) return Math.round(distanceInMeter / 1000) + " km";
    return String.format("%.1fkm", distanceInMeter / 1000);
  }

}
