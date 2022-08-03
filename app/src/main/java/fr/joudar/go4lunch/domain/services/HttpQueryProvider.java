package fr.joudar.go4lunch.domain.services;

import java.util.Map;

import fr.joudar.go4lunch.domain.dto.MapApiAutocompleteResponse;
import fr.joudar.go4lunch.domain.dto.MapApiNearbysearchResponse;
import fr.joudar.go4lunch.domain.dto.MapApiPlaceDetailsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface HttpQueryProvider {
    @GET("nearbysearch/json")
    Call<MapApiNearbysearchResponse> placesQuery(@QueryMap Map<String, String> params);

    @GET("autocomplete/json")
    Call<MapApiAutocompleteResponse> getAutocompletes(@QueryMap Map<String, String> params);

    @GET("details/json")
    Call<MapApiPlaceDetailsResponse> getPlaceDetails(@QueryMap Map<String, String> params);

}
