package fr.joudar.go4lunch.ui.fragments.restaurantsList;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.joudar.go4lunch.R;

public class RestaurantsListFragment extends Fragment {

    public RestaurantsListFragment() {
    }

    public static RestaurantsListFragment newInstance(String param1, String param2) {
        RestaurantsListFragment fragment = new RestaurantsListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurants_list, container, false);
    }
}