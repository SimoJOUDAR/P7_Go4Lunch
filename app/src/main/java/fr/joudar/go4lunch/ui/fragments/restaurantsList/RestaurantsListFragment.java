package fr.joudar.go4lunch.ui.fragments.restaurantsList;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.Arrays;
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

            //TODO: Test - Start
            int n = results.length;
            Log.d("RestaurantsListFragment", "nearbyRestaurantsCallback - onSuccess - results.length = " + n);
            //TODO: Test - End

            if (results.length == 0) {
                emptyListMessage(NO_RESTAURANT_FOUND_CODE);
            }
            else {
                places = results;
                fetchingColleaguesDistribution();
            }
        }

        @Override
        public void onFailure() {

            //TODO: Test - Start
            Log.d("RestaurantsListFragment", "nearbyRestaurantsCallback - onFailure");
            //TODO: Test - End

            emptyListMessage(FAIL_FETCHING_NEARBY_RESTAURANTS_CODE);
        }
    };
    private final Callback<Map<String, Integer>> fetchingColleaguesCallback = new Callback<Map<String, Integer>>() {
        @Override
        public void onSuccess(Map<String, Integer> results) {

            //TODO: Test - Start
            Log.d("RestaurantsListFragment", "fetchingColleaguesCallback - onSuccess");
            //TODO: Test - End

            colleaguesDistribution = results;
            updateRecyclerView();
        }

        @Override
        public void onFailure() {

            //TODO: Test - Start
            Log.d("RestaurantsListFragment", "fetchingColleaguesCallback - onFailure");
            //TODO: Test - End

            emptyListMessage(FAIL_FETCHING_COLLEAGUES_CODE);
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

    private final RestaurantListAdapter listAdapter = new RestaurantListAdapter(onClickCallback);

    public RestaurantsListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRestaurantsListBinding.inflate(LayoutInflater.from(container.getContext()), container, false);
        initRecyclerView();
        initViewModel(container);
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

                if (location != null) {
                    currentLocation = location;
                    viewModel.getNearbyRestaurant(currentLocation, getSearchRadius(), nearbyRestaurantsCallback);
                }
                else
                    emptyListMessage(CURRENT_LOCATION_ERROR_CODE);
            }
        });
    }

    private void fetchingColleaguesDistribution(){
        String workplaceId = viewModel.getCurrentUser().getWorkplaceId();
        if (workplaceId == null || workplaceId.equals("")) {
            emptyListMessage(NO_WORKPLACE_SELECTED_CODE);
        }
        else {
            viewModel.getColleaguesDistributionOverRestaurants(fetchingColleaguesCallback);
        }
    }


    /***********************************************************************************************
     ** RecyclerView
     **********************************************************************************************/

    private void initRecyclerView() {
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerview.setAdapter(listAdapter);
    }
    private void updateRecyclerView() {
        listAdapter.updateData(places, currentLocation, colleaguesDistribution);
        binding.recyclerview.setVisibility(View.VISIBLE);
        binding.shimmerListLayout.stopShimmer();
        binding.shimmerListLayout.setVisibility(View.GONE);
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

    // Error code for emptyListMessage method:
    final int CURRENT_LOCATION_ERROR_CODE = 11;
    final int FAIL_FETCHING_NEARBY_RESTAURANTS_CODE = 22;
    final int NO_RESTAURANT_FOUND_CODE = 33;
    final int NO_WORKPLACE_SELECTED_CODE = 44;
    final int FAIL_FETCHING_COLLEAGUES_CODE = 55;

    private void emptyListMessage(int errorCode) {
        binding.recyclerview.setVisibility(View.GONE);
        binding.restaurantsEmptyListMsgLayout.setVisibility(View.VISIBLE);
        binding.shimmerListLayout.stopShimmer();
        binding.shimmerListLayout.setVisibility(View.GONE);

        switch (errorCode) {

            case CURRENT_LOCATION_ERROR_CODE:
                binding.restaurantEmptyListMsg.setText(R.string.RestaurantsListFragment_CurrentLocationError_msg);
                break;

            case FAIL_FETCHING_NEARBY_RESTAURANTS_CODE:
                binding.restaurantEmptyListMsg.setText(R.string.RestaurantsListFragment_FailFetchingNearbyRestaurant_msg);
                break;

            case NO_RESTAURANT_FOUND_CODE:
                binding.restaurantEmptyListMsg.setText(R.string.RestaurantsListFragment_NoRestaurantFound_msg);
                break;

            case NO_WORKPLACE_SELECTED_CODE:
                binding.restaurantEmptyListMsg.setText(R.string.NoWorkplaceSelected_msg);
                break;

            case FAIL_FETCHING_COLLEAGUES_CODE:
                binding.restaurantEmptyListMsg.setText(R.string.ErrorFetchingColleagues_msg);
                break;

            default:
                binding.restaurantEmptyListMsg.setText(R.string.default_empty_list_message);
                break;

        }

    }

    /***********************************************************************************************
     ** Utils
     **********************************************************************************************/

    private String getSearchRadius() {
        return ((HomepageActivity) getActivity()).getSearchRadius();
    }
}