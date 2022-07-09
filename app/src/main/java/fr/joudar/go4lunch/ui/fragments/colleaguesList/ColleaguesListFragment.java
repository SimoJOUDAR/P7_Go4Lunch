package fr.joudar.go4lunch.ui.fragments.colleaguesList;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.ui.activities.HomepageActivity;

public class ColleaguesListFragment extends Fragment {

    public ColleaguesListFragment() {
    }

    public static ColleaguesListFragment newInstance(String param1, String param2) {
        ColleaguesListFragment fragment = new ColleaguesListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_colleagues_list, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomepageActivity)getActivity()).colleaguesListFragmentDisplayOptions();
    }
}