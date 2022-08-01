package fr.joudar.go4lunch.domain.core;

import static fr.joudar.go4lunch.viewmodel.HomepageViewModel.getRadius;
import static fr.joudar.go4lunch.viewmodel.HomepageViewModel.getSystemLanguage;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import fr.joudar.go4lunch.BuildConfig;
import fr.joudar.go4lunch.domain.dto.MapApiNearbysearchResponse;
import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.services.HttpQueryProvider;
import fr.joudar.go4lunch.domain.services.NearbysearchProvider;
import fr.joudar.go4lunch.domain.utils.Callback;
import retrofit2.Call;
import retrofit2.Response;

public class GoogleApiHandler implements NearbysearchProvider {

    public static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/";
    public static final String BUSINESS_STATUS = "restaurant";
    private final HttpQueryProvider mHttpQueryProvider;
    private Map<String, String> queryParameters;

    // Hilt injected the PlaceHttpClient arg dependency we need here

    @Inject
    public GoogleApiHandler(HttpQueryProvider httpQueryProvider) {
        this.mHttpQueryProvider = httpQueryProvider;
    }

    /***********************************************************************************************
     ** Nearbysearch
     **********************************************************************************************/

//    String businessStatus, LatLng location,
//    String lang, int radius

    @Override
    public void setQueryParameters(LatLng location) {
        queryParameters = new HashMap<>();
        queryParameters.put("type", BUSINESS_STATUS);
        queryParameters.put("location", location.latitude + "," + location.longitude);
        queryParameters.put("language", getSystemLanguage());
        queryParameters.put("radius", getRadius());
        queryParameters.put("key", BuildConfig.MAPS_API_KEY);
    }

    @Override
    public void getPlaces(Callback<Place[]> callback) {
        mHttpQueryProvider.placesQuery(queryParameters).enqueue(catchHttpPlacesQueryResults(callback));
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

}
