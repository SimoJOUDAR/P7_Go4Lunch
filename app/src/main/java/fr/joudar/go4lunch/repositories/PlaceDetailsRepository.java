package fr.joudar.go4lunch.repositories;

import javax.inject.Inject;

import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.services.PlaceDetailsProvider;
import fr.joudar.go4lunch.domain.utils.Callback;

public class PlaceDetailsRepository {

    private final PlaceDetailsProvider placeDetailsProvider;

    @Inject
    public PlaceDetailsRepository(PlaceDetailsProvider placeDetailsProvider) {
        this.placeDetailsProvider = placeDetailsProvider;
    }

    public void getPlaceDetails(String placeId, Callback<Place> callback) {
        placeDetailsProvider.getPlaceDetails(placeId, callback);
    }
}
