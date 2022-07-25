package fr.joudar.go4lunch.ui.fragments.map;


import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.domain.core.CurrentLocationHandler;
import fr.joudar.go4lunch.domain.models.LatLngCoordinates;
import fr.joudar.go4lunch.domain.services.CurrentLocationProvider;
import fr.joudar.go4lunch.ui.activities.HomepageActivity;

@AndroidEntryPoint
public class MapFragment extends Fragment {

    private static final String LOG_TAG = "MapFragment";
    private GoogleMap map;
    @Inject public CurrentLocationProvider currentLocationProvider;

    public MapFragment() {}

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mapView = inflater.inflate(R.layout.fragment_map, container, false);

        //TODO : implement restaurant workmates frequentation here

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this::onMapResults);
        return mapView;

    }

    // Init the map, updateUI & onClickListeners()
    private void onMapResults(GoogleMap map) {
        Log.d(LOG_TAG, "MAP LOADED SUCCESSFULLY");
        this.map = map;
        currentLocationProvider.getCurrentCoordinates(this::updateMapUI);
        //TODO: map.setOnInfoWindowClickListener(this::displayRestaurantDetails);
    }

    // Updates the map with buttons, camera coordinates & showNearbyRestaurants().

    @SuppressLint("MissingPermission")
    private void updateMapUI(Location currentLocation) {
        Log.d(LOG_TAG, "CURRENT_LOCATION = " + currentLocation);
        if (currentLocation != null) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 14));
            //TODO: showNearbyRestaurants(currentLocation);
        } else {
            map.getUiSettings().setMyLocationButtonEnabled(false);
            //TODO: showErrorMessage();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomepageActivity)getActivity()).mapFragmentDisplayOptions();
    }

}