package fr.joudar.go4lunch.ui.fragments.map;


import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory;
import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.services.CurrentLocationProvider;
import fr.joudar.go4lunch.domain.utils.Callback;
import fr.joudar.go4lunch.repositories.FirebaseServicesRepository;
import fr.joudar.go4lunch.ui.activities.HomepageActivity;
import fr.joudar.go4lunch.viewmodel.HomepageViewModel;
import fr.joudar.go4lunch.viewmodel.PlacesViewModel;

@AndroidEntryPoint
public class MapFragment extends Fragment {

    private static final String LOG_TAG = "MapFragment";
    private GoogleMap map;
    @Inject public CurrentLocationProvider currentLocationProvider;
    private PlacesViewModel placesViewModel;
    private HomepageViewModel homepageViewModel;
    private Map<String, Integer> distributionHashMap = new HashMap<>();

    public MapFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mapView = inflater.inflate(R.layout.fragment_map, container, false);

        initViewModel(container);

        Log.d("MapFragment", "SupportMapFragment _stared_");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        Log.d("MapFragment", "SupportMapFragment _finished_");
        mapFragment.getMapAsync(this::onMapResults);
        return mapView;
    }

    // Init the PlacesViewModel
    private void initViewModel(View fragmentContainer) {
        Log.d("MapFragment", "initViewModel _started_");
        final NavController navController = Navigation.findNavController(fragmentContainer);
        final NavBackStackEntry backStackEntry = navController.getBackStackEntry(R.id.nav_graph);
        ViewModelProvider viewModelProvider = new ViewModelProvider(
                backStackEntry,
                HiltViewModelFactory.createInternal(getActivity(), backStackEntry, null, null));
        placesViewModel = viewModelProvider.get(PlacesViewModel.class);
        homepageViewModel = viewModelProvider.get(HomepageViewModel.class);
        Log.d("MapFragment", "initViewModel _finished_");
    }

    // Init the map, updateUI & onClickListeners()
    private void onMapResults(GoogleMap map) {
        Log.d("MapFragment", "onMapResults");
        this.map = map;
        initMapUpdates();
        map.setOnInfoWindowClickListener(this::displayPlaceDetails);
    }

    // Observes currentUser to keep "colleagues distribution overs nearby restaurant" updated
    private void initMapUpdates(){
        Log.d("MapFragment", "initMapUpdates");
        //TODO : Use ViewModel abstraction
        //homepageViewModel.getLiveCurrentUser().observe(getViewLifecycleOwner(), __ -> getColleaguesDistributionOverRestaurants());
        getColleaguesDistributionOverRestaurants();
    }

    // a HashMap of colleagues distribution overs nearby restaurant
    private void getColleaguesDistributionOverRestaurants() {
        Log.d("MapFragment", "getColleaguesDistributionOverRestaurants");
        //TODO : Use ViewModel abstraction
        if (homepageViewModel.getWorkplaceId() != null) {
            placesViewModel.getColleaguesDistributionOverRestaurants(new Callback<Map<String, Integer>>() {
                @Override
                public void onSuccess(Map<String, Integer> results) {
                    distributionHashMap = results;
                    Log.d("MapFragment", "getColleaguesDistributionOverRestaurants _onSuccess_");
                    currentLocationProvider.getCurrentCoordinates(MapFragment.this::updateMapUI);
                }

                @Override
                public void onFailure() {
                    distributionHashMap = null;
                    Log.d("MapFragment", "getColleaguesDistributionOverRestaurants _onFailure_");
                    showErrorMessage();
                }
            });
        }
    }

    // Updates the map with buttons, camera coordinates & showNearbyRestaurants().
    @SuppressLint("MissingPermission")
    private void updateMapUI(Location currentLocation) {
        Log.d("MapFragment", "updateMapUI");
        if (currentLocation != null) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 14));
            showNearbyPlaces(currentLocation);
        } else {
            map.getUiSettings().setMyLocationButtonEnabled(false);
            showErrorMessage();
        }
    }

    //Shows nearby places and markers
    private void showNearbyPlaces(Location currentLocation) {
        Log.d("MapFragment", "showNearbyPlaces");
        placesViewModel.getNearbyRestaurant(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), new Callback<Place[]>() {
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
        Log.d("MapFragment", "showRestaurantsMarkers");
        map.clear();
        int markerDrawable;
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
        Log.d("MapFragment", "displayPlaceDetails");
        //TODO : NavGraph actions towards RestaurantDetailsFragment
    }

    private void showErrorMessage() {
        Log.d("MapFragment", "showErrorMessage");
        //
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomepageActivity)getActivity()).mapFragmentDisplayOptions();
    }

}