package fr.joudar.go4lunch.ui.fragments.restaurantsList;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory;
import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.databinding.FragmentRestaurantsListBinding;
import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.services.CurrentLocationProvider;
import fr.joudar.go4lunch.domain.utils.Calculus;
import fr.joudar.go4lunch.domain.utils.Callback;
import fr.joudar.go4lunch.ui.activities.HomepageActivity;
import fr.joudar.go4lunch.ui.core.adapters.RestaurantListAdapter;
import fr.joudar.go4lunch.viewmodel.HomepageViewModel;

@AndroidEntryPoint
public class RestaurantsListFragment extends Fragment {

    private FragmentRestaurantsListBinding binding;
    public HomepageViewModel viewModel;
    @Inject public CurrentLocationProvider currentLocationProvider;
    private Location currentLocation;
    private Map<String, Integer> colleaguesDistribution = new HashMap<>();
    private Place[] places;
    private List<Place> restaurants = new ArrayList<>(); //restaurants = Arrays.asList(places);
    private final Callback<Place[]> nearbyRestaurantsCallback = new Callback<Place[]>() {
        @Override
        public void onSuccess(Place[] results) {
            places = results;
            fetchingColleaguesDistribution();
        }

        @Override
        public void onFailure() {
            fetchingNearbyRestaurantError();
        }
    };
    private final Callback<Map<String, Integer>> fetchingColleaguesCallback = new Callback<Map<String, Integer>>() {
        @Override
        public void onSuccess(Map<String, Integer> results) {
            colleaguesDistribution = results;
            updateRecyclerView();
        }

        @Override
        public void onFailure() {
            fetchingColleaguesError();
        }
    };
    private final Callback<String> onClickCallback = new Callback<String>() {
        @Override
        public void onSuccess(String id) {
            Bundle bundle = new Bundle();
            bundle.putString("placeId", id);
            Navigation.findNavController(getView()).navigate(R.id.restaurantDetailsFragment, bundle);
        }

        @Override
        public void onFailure() {

        }
    };
    private RestaurantListAdapter listAdapter;

    public RestaurantsListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRestaurantsListBinding.inflate(LayoutInflater.from(container.getContext()), container, false);
        initViewModel(container);
        //viewModel.getLiveCurrentUser().observe(getViewLifecycleOwner(), user -> getCurrentLocation()); // To re-actualize the whole fragment
        getCurrentLocation();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomepageActivity)getActivity()).restaurantsListFragmentDisplayOptions();
    }

    /***********************************************************************************************
     ** ViewModel
     **********************************************************************************************/
    // Init the PlacesViewModel
    private void initViewModel(View fragmentContainer) {
        Log.d("RestaurantsListFragment", "initViewModel _started_");
        final NavController navController = Navigation.findNavController(fragmentContainer);
        final NavBackStackEntry backStackEntry = navController.getBackStackEntry(R.id.nav_graph);
        ViewModelProvider viewModelProvider = new ViewModelProvider(
                backStackEntry,
                HiltViewModelFactory.createInternal(getActivity(), backStackEntry, null, null));
        viewModel = viewModelProvider.get(HomepageViewModel.class);
        Log.d("RestaurantsListFragment", "initViewModel _finished_");
    }

    /***********************************************************************************************
     ** Current location and fetching data
     **********************************************************************************************/
    private void getCurrentLocation() {
        currentLocationProvider.getCurrentCoordinates(new CurrentLocationProvider.OnCoordinatesResultListener() {
            @Override
            public void onResult(Location location) {

                if (currentLocation != null) {
                    currentLocation = location;
                    viewModel.getNearbyRestaurant(currentLocation, getSearchRadius(), nearbyRestaurantsCallback);
                }
                else
                    currentLocationError();
            }
        });
    }

    private void fetchingColleaguesDistribution(){
        viewModel.getColleaguesDistributionOverRestaurants(fetchingColleaguesCallback);
    }


    /***********************************************************************************************
     ** RecyclerView
     **********************************************************************************************/
    //
    private void updateRecyclerView() {
        binding.shimmerListLayout.stopShimmer();
        binding.shimmerListLayout.setVisibility(View.GONE);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        listAdapter = new RestaurantListAdapter(places, currentLocation, onClickCallback, colleaguesDistribution);
        binding.recyclerview.setAdapter(listAdapter);
    }

    /***********************************************************************************************
     ** Option menu
     **********************************************************************************************/

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.toolbar_layout, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                restaurants = Arrays.asList(places);
                Stream<Place> stream;
                switch (menuItem.getItemId()) {
                    case R.id.sorting_rates:
                        stream = restaurants.stream().sorted((t1, t2) -> sortByRatings(t1, t2));
                        listAdapter.updateRestaurantList(stream.toArray(Place[]::new), currentLocation);
                        return true;
                    case R.id.sorting_nearest:
                        stream = restaurants.stream().sorted((t1, t2) -> sortByDistance(t1, t2));
                        listAdapter.updateRestaurantList(stream.toArray(Place[]::new), currentLocation);
                        return true;
                    case R.id.sorting_Workmates_most_frequented:
                        stream = restaurants.stream().sorted((t1, t2) -> sortByColleaguesDistribution(t1, t2));
                        listAdapter.updateRestaurantList(stream.toArray(Place[]::new), currentLocation);
                        return true;
                    default:
                        return false;
                }
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    /***********************************************************************************************
     ** Sorting
     **********************************************************************************************/

    public int sortByRatings(Place place1, Place place2) {
        return (int) (place2.getRating() - place1.getRating());
    }

    private int sortByDistance(Place place1, Place place2) {
        final float distance1 = Calculus.distanceBetween(currentLocation, place1.getCoordinates());
        final float distance2 = Calculus.distanceBetween(currentLocation, place2.getCoordinates());
        return (int) (distance1 - distance2);
    }

    private int sortByColleaguesDistribution(Place place1, Place place2) {
        Integer count1 = colleaguesDistribution.get(place1.getId());
        if (count1 == null) count1 = 0;
        Integer count2 = colleaguesDistribution.get(place2.getId());
        if (count2 == null) count2 = 0;
        return count2 - count1;
    }

    /***********************************************************************************************
     ** Error handling
     **********************************************************************************************/

    private void currentLocationError(){
        //TODO: handle here
    }

    private void fetchingNearbyRestaurantError() {
        // TODO: handle here
    }

    private void fetchingColleaguesError() {
        // TODO: handle here
    }

    /***********************************************************************************************
     ** Utils
     **********************************************************************************************/

    private String getSearchRadius() {
        return ((HomepageActivity) getActivity()).getSearchRadius();
    }
}