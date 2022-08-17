package fr.joudar.go4lunch.domain.utils;

import android.annotation.SuppressLint;
import android.location.Location;
import com.google.android.gms.maps.model.LatLng;

public class DistanceOperations {

  // TODO : Useless ? Remove?
  // Returns the meter distance between two LatLng locations
  public static Float getLatLngDistance(Location start, Location end) {

    Location startLoc = new Location("Location Provider");
    startLoc.setLatitude(start.getLatitude());
    startLoc.setLongitude(start.getLongitude());

    Location endLoc = new Location("Location Provider");
    endLoc.setLatitude(end.getLatitude());
    endLoc.setLongitude(end.getLongitude());

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
