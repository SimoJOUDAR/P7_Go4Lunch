package fr.joudar.go4lunch.ui.fragments.map;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.ui.activities.HomepageActivity;

public class MapFragment extends Fragment {

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
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomepageActivity)getActivity()).mapFragmentDisplayOptions();
    }
}