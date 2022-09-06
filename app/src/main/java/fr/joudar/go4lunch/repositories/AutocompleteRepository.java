package fr.joudar.go4lunch.repositories;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;

import dagger.hilt.android.scopes.ViewModelScoped;
import fr.joudar.go4lunch.domain.models.Autocomplete;
import fr.joudar.go4lunch.domain.services.AutocompleteProvider;
import fr.joudar.go4lunch.domain.utils.Callback;

@ViewModelScoped
public class AutocompleteRepository {

    private final AutocompleteProvider autocompleteProvider;

    @Inject
    public AutocompleteRepository(AutocompleteProvider autocompleteProvider) {
        this.autocompleteProvider = autocompleteProvider;
    }

    public void getAutocompletes(@NonNull String input, @NonNull Location location, @NonNull String searchRadius, boolean isFiltered, Callback<Autocomplete[]> callback) {
        autocompleteProvider.getAutocompletes(input, location, searchRadius, isFiltered, callback
//                new Callback<Autocomplete[]>() {   // Pass the callback straight away with no additional treatment
//                    @Override
//                    public void onSuccess(Autocomplete[] results) {
//                        callback.onSuccess(results);
//                    }
//
//                    @Override
//                    public void onFailure() {
//                        callback.onFailure();
//                    }
//                }
        );
    }
}
