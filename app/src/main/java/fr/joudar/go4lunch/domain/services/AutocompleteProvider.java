package fr.joudar.go4lunch.domain.services;

import com.google.android.gms.maps.model.LatLng;

import fr.joudar.go4lunch.domain.models.Autocomplete;
import fr.joudar.go4lunch.domain.utils.Callback;

public interface AutocompleteProvider {

    void getAutocompletes(String input, LatLng location, boolean filtered, Callback<Autocomplete[]> callback);

}
