package fr.joudar.go4lunch.domain.core;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import fr.joudar.go4lunch.BuildConfig;
import fr.joudar.go4lunch.domain.dto.MapApiAutocompleteResponse;
import fr.joudar.go4lunch.domain.dto.MapApiNearbysearchResponse;
import fr.joudar.go4lunch.domain.dto.MapApiPlaceDetailsResponse;
import fr.joudar.go4lunch.domain.models.Autocomplete;
import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.services.AutocompleteProvider;
import fr.joudar.go4lunch.domain.services.HttpQueryProvider;
import fr.joudar.go4lunch.domain.services.NearbysearchProvider;
import fr.joudar.go4lunch.domain.services.PlaceDetailsProvider;
import fr.joudar.go4lunch.domain.utils.Callback;
import fr.joudar.go4lunch.domain.utils.Token;
import retrofit2.Call;
import retrofit2.Response;

public class GoogleApiHandler implements NearbysearchProvider, AutocompleteProvider, PlaceDetailsProvider {

    public static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/";
    public static final String BUSINESS_STATUS = "restaurant";
    private final HttpQueryProvider httpQueryProvider;
    private Map<String, String> nearbyQueryParameters;
    private Map<String, String> autocompleteQueryParameters;
    private Map<String, String> placeDetailsQueryParameters;
    private final Token token;
    public static int searchRadius = 0;

    // Hilt injected the PlaceHttpClient arg dependency we need here

    @Inject
    public GoogleApiHandler(HttpQueryProvider httpQueryProvider, Token token) {
        this.httpQueryProvider = httpQueryProvider;
        this.token = token;
    }

    /***********************************************************************************************
     ** Nearbysearch
     **********************************************************************************************/

//    String businessStatus, LatLng location,
//    String lang, int radius

    @Override
    public void getPlaces(Location location, String radius, Callback<Place[]> callback) {
        setNearbyQueryParameters(location, radius);
        httpQueryProvider.placesQuery(nearbyQueryParameters).enqueue(catchHttpPlacesQueryResults(callback));
    }

    private void setNearbyQueryParameters(Location location, String radius) {
        nearbyQueryParameters = new HashMap<>();
        nearbyQueryParameters.put("type", BUSINESS_STATUS);
        nearbyQueryParameters.put("location", location.getLatitude() + "%2C-" + location.getLongitude());
        nearbyQueryParameters.put("language", getSystemLanguage());
        nearbyQueryParameters.put("radius", radius);
        nearbyQueryParameters.put("key", BuildConfig.MAPS_API_KEY);
    }


    private retrofit2.Callback<MapApiNearbysearchResponse> catchHttpPlacesQueryResults(Callback<Place[]> callback) {
        return new retrofit2.Callback<MapApiNearbysearchResponse>() {

            @Override
            public void onResponse(Call<MapApiNearbysearchResponse> call, Response<MapApiNearbysearchResponse> response) {
                final MapApiNearbysearchResponse body = response.body();
                if (body != null)
                    callback.onSuccess(body.getPlaces());
                else
                    callback.onFailure();
            }

            @Override
            public void onFailure(Call<MapApiNearbysearchResponse> call, Throwable t) {
                callback.onFailure();
            }
        };
    }

    /***********************************************************************************************
     ** Autocomplete
     **********************************************************************************************/


    @Override
    public void getAutocompletes(String input, Location location, String searchRadius, boolean isFiltered, Callback<Autocomplete[]> callback) {
        setAutocompleteQueryParameters(input, location, searchRadius);
        httpQueryProvider.getAutocompletes(autocompleteQueryParameters).enqueue(catchHttpAutocompleteQueryResults(isFiltered, callback));

    }

    private void setAutocompleteQueryParameters(String input, Location location, String searchRadius) {
        autocompleteQueryParameters = new HashMap<>();
        autocompleteQueryParameters.put("input", input);
        autocompleteQueryParameters.put("types", "establishment");
        autocompleteQueryParameters.put("language", getSystemLanguage());
        autocompleteQueryParameters.put("origin", location.getLatitude() + "%2C-" + location.getLongitude());
        autocompleteQueryParameters.put("location", location.getLatitude() + "%2C-" + location.getLongitude());
        autocompleteQueryParameters.put("radius", searchRadius);
        autocompleteQueryParameters.put("key", BuildConfig.MAPS_API_KEY);
        autocompleteQueryParameters.put("sessiontoken", token.getToken());
    }

    private retrofit2.Callback<MapApiAutocompleteResponse> catchHttpAutocompleteQueryResults(
            boolean isFiltered, Callback<Autocomplete[]> callback) {
        return new retrofit2.Callback<MapApiAutocompleteResponse>() {
            @Override
            public void onResponse(Call<MapApiAutocompleteResponse> call, Response<MapApiAutocompleteResponse> response) {
                final MapApiAutocompleteResponse body = response.body();
                if (body != null)
                    callback.onSuccess(body.getAutocomplete(isFiltered));
                else
                    callback.onFailure();
            }

            @Override
            public void onFailure(Call<MapApiAutocompleteResponse> call, Throwable t) {
                callback.onFailure();
            }
        };
    }

    /***********************************************************************************************
     ** PlaceDetails
     **********************************************************************************************/

    @Override
    public void getPlaceDetails(String placeId, Callback<Place> callback) {
        setPlaceDetailsQueryParameters(placeId);
        httpQueryProvider.getPlaceDetails(placeDetailsQueryParameters).enqueue(catchHttpPlaceDetailsQueryResults(callback));
    }

    private void setPlaceDetailsQueryParameters(String placeId) {
        String fields = "place_id,name,formatted_address,formatted_phone_number,website,rating,opening_hours,photos";
        placeDetailsQueryParameters = new HashMap<>();
        placeDetailsQueryParameters.put("place_id", placeId);
        placeDetailsQueryParameters.put("language", getSystemLanguage());
        placeDetailsQueryParameters.put("fields", fields);
        placeDetailsQueryParameters.put("sessiontoken", token.getToken());
        placeDetailsQueryParameters.put("key", BuildConfig.MAPS_API_KEY);
    }

    private retrofit2.Callback<MapApiPlaceDetailsResponse> catchHttpPlaceDetailsQueryResults(Callback<Place> callback) {
        return new retrofit2.Callback<MapApiPlaceDetailsResponse>() {
            @Override
            public void onResponse(Call<MapApiPlaceDetailsResponse> call, Response<MapApiPlaceDetailsResponse> response) {
                final MapApiPlaceDetailsResponse body = response.body();
                if (body != null) {
                    final Place resultPlace = body.getPlaceDetails();
                    if (resultPlace != null) {
                        callback.onSuccess(resultPlace);
                    }
                    else
                        callback.onFailure();
                }
                else
                    callback.onFailure();
            }

            @Override
            public void onFailure(Call<MapApiPlaceDetailsResponse> call, Throwable t) {
                callback.onFailure();
            }
        };
    }

    /***********************************************************************************************
     ** Utils
     **********************************************************************************************/
    public static String getSystemLanguage() {
        String lang;
        if (Locale.getDefault().getLanguage().equals("fr"))
            lang = "fr";
        else
            lang = "en";
        return lang;
    }
}
