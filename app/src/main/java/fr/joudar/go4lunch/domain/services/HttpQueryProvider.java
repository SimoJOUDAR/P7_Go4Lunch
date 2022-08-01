package fr.joudar.go4lunch.domain.services;

import java.util.Map;

import fr.joudar.go4lunch.domain.dto.MapApiNearbysearchResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface HttpQueryProvider {
    @GET("nearbysearch/json")
    Call<MapApiNearbysearchResponse> placesQuery(@QueryMap Map<String, String> params);

}
