package fr.joudar.go4lunch.ui.fragments.restaurantDetails;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.ui.activities.HomepageActivity;

public class RestaurantDetailsFragment extends Fragment {

    public RestaurantDetailsFragment() {
    }

    public static RestaurantDetailsFragment newInstance(String param1, String param2) {
        RestaurantDetailsFragment fragment = new RestaurantDetailsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurant_details, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomepageActivity)getActivity()).restaurantDetailsFragmentDisplayOptions();
    }
}