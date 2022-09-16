package fr.joudar.go4lunch.repositories;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.services.NearbysearchProvider;
import fr.joudar.go4lunch.domain.utils.Callback;

@ParametersAreNonnullByDefault
//@ViewModelScoped  //TODO: make it ViewModelScoped ?
public class NearbysearchRepository {

    private final NearbysearchProvider nearbysearchProvider;

    @Inject
    public NearbysearchRepository(NearbysearchProvider nearbysearchProvider) {
        this.nearbysearchProvider = nearbysearchProvider;
    }

    public void getNearbyRestaurant(Location location, String radius, Callback<Place[]> callback) {
        nearbysearchProvider.getPlaces(location, radius, callback);
    }
}
