package fr.joudar.go4lunch.domain.services;

import com.google.android.gms.maps.model.LatLng;
import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.utils.Callback;

public interface NearbysearchProvider {

    void getPlaces(LatLng location, Callback<Place[]> callback);

}
