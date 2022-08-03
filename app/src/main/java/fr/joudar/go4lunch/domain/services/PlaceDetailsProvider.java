package fr.joudar.go4lunch.domain.services;

import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.utils.Callback;

public interface PlaceDetailsProvider {

    void getPlaceDetails(String placeId, Callback<Place> callback);
}
