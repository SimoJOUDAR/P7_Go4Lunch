package fr.joudar.go4lunch.repositories;

import com.google.android.gms.maps.model.LatLng;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;
import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.services.NearbysearchProvider;
import fr.joudar.go4lunch.domain.utils.Callback;

@ParametersAreNonnullByDefault
public class NearbysearchRepository {

    private final NearbysearchProvider nearbysearchProvider;

    @Inject
    public NearbysearchRepository(NearbysearchProvider nearbysearchProvider) {
        this.nearbysearchProvider = nearbysearchProvider;
    }

    public void getNearbyRestaurant(LatLng location, Callback<Place[]> callback) {
        nearbysearchProvider.setQueryParameters(location);
        nearbysearchProvider.getPlaces(
                new Callback<Place[]>() {
                    @Override
                    public void onSuccess(Place[] places) {
                        callback.onSuccess(places);
                    }

                    @Override
                    public void onFailure() {

                    }
                }
        );
    }
}
