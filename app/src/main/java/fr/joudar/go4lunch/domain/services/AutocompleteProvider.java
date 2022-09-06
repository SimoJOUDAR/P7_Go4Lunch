package fr.joudar.go4lunch.domain.services;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import fr.joudar.go4lunch.domain.models.Autocomplete;
import fr.joudar.go4lunch.domain.utils.Callback;

public interface AutocompleteProvider {

    void getAutocompletes(@NonNull String input, @NonNull Location location, @NonNull String searchRadius, boolean filtered, Callback<Autocomplete[]> callback);

}
