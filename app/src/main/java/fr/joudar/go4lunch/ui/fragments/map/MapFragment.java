package fr.joudar.go4lunch.ui.fragments.map;


import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory;
import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.services.CurrentLocationProvider;
import fr.joudar.go4lunch.domain.utils.Callback;
import fr.joudar.go4lunch.ui.activities.HomepageActivity;
import fr.joudar.go4lunch.viewmodel.HomepageViewModel;

@AndroidEntryPoint
public class MapFragment extends Fragment {

    private final String TAG = "MapFragment";
    private GoogleMap map;
    @Inject public CurrentLocationProvider currentLocationProvider;
    private HomepageViewModel homepageViewModel;
    private Map<String, Integer> distributionHashMap = new HashMap<>();
    boolean firstInit = true;

    public MapFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View mapView = inflater.inflate(R.layout.fragment_map, container, false);
        initViewModel(container);
        return mapView;
    }

    // Init the PlacesViewModel
    private void initViewModel(View fragmentContainer) {
        Log.d(TAG, "initViewModel");
        final NavController navController = Navigation.findNavController(fragmentContainer);
        final NavBackStackEntry backStackEntry = navController.getBackStackEntry(R.id.nav_graph);
        ViewModelProvider viewModelProvider = new ViewModelProvider(
                backStackEntry,
                HiltViewModelFactory.createInternal(getActivity(), backStackEntry, null, null));
        homepageViewModel = viewModelProvider.get(HomepageViewModel.class);
        homepageViewModel.getLiveCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                if (firstInit) {
                    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this::onMapResults);
                    firstInit = false;
                }
                else
                    getColleaguesDistributionOverRestaurants();
            }
        });
    }

    // Init the map, updateUI & onClickListeners()
    private void onMapResults(GoogleMap map) {
        Log.d(TAG, "onMapResults");
        this.map = map;
        getColleaguesDistributionOverRestaurants();
        map.setOnInfoWindowClickListener(this::displayPlaceDetails);
    }

    // a HashMap of colleagues distribution overs nearby restaurant
    private void getColleaguesDistributionOverRestaurants() {
        Log.d(TAG, "getColleaguesDistributionOverRestaurants");
        if (homepageViewModel.isWorkplaceIdSet()) {
            homepageViewModel.getColleaguesDistributionOverRestaurants(new Callback<Map<String, Integer>>() {
                @Override
                public void onSuccess(Map<String, Integer> results) {
                    distributionHashMap = results;
                    currentLocationProvider.getCurrentCoordinates(MapFragment.this::updateMapUI);
                }

                @Override
                public void onFailure() {
                    distributionHashMap = null;
                    showErrorMessage();
                }
            });
        }
    }

    // Updates the map with buttons, camera coordinates & showNearbyRestaurants().
    @SuppressLint("MissingPermission")
    private void updateMapUI(Location currentLocation) {
        Log.d(TAG, "updateMapUI");
        if (currentLocation != null) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 14));
            showNearbyPlaces(currentLocation, getSearchRadius());
        } else {
            map.getUiSettings().setMyLocationButtonEnabled(false);
            showErrorMessage();
        }
    }

    //Shows nearby places and markers
    private void showNearbyPlaces(Location currentLocation, String radius) {
        Log.d(TAG, "showNearbyPlaces");
        homepageViewModel.getNearbyRestaurant(currentLocation, radius, new Callback<Place[]>() {
            @Override
            public void onSuccess(Place[] results) {
                if (results != null)
                    showRestaurantsMarkers(results);
            }

            @Override
            public void onFailure() {

            }
        });
    }

    // Shows markers on map
    private void showRestaurantsMarkers(Place[] places) {
        Log.d(TAG, "showRestaurantsMarkers");
        map.clear();
        @DrawableRes int markerDrawable;
        String markerTitle;
        for (Place place : places) {
            if(distributionHashMap.containsKey(place.getId())){
                markerDrawable = R.drawable.ic_green_pin_24;
                markerTitle = place.getName() + " \uD83D\uDC64(" + distributionHashMap.get(place.getId()) + ")";
            } else {
                markerDrawable = R.drawable.ic_red_pin_24;
                markerTitle = place.getName();
            }
            map.addMarker(
                    new MarkerOptions().position(place.getCoordinates())
                            .title(markerTitle)
                            .snippet(place.getVicinity())
                            .icon(BitmapDescriptorFactory.fromResource(markerDrawable)))
                    .setTag(place.getId());
        }
    }


    private void displayPlaceDetails(Marker marker) {
        Log.d(TAG, "displayPlaceDetails");
        String restaurantId = marker.getTag().toString();

        Bundle bundle = new Bundle();
        bundle.putString("placeId", restaurantId);
        Navigation.findNavController(getView()).navigate(R.id.restaurantDetailsFragment, bundle);
    }

    private String getSearchRadius() {
        return ((HomepageActivity) getActivity()).getSearchRadius();
    }

    /***********************************************************************************************
     ** Error handling
     **********************************************************************************************/

    private void showErrorMessage() {
        Log.d(TAG, "showErrorMessage");
        //TODO: Implement Error handling here (Hide view, display Error message)
    }



}