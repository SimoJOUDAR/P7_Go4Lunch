package fr.joudar.go4lunch.ui.fragments.restaurantDetails;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory;
import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.databinding.FragmentRestaurantDetailsBinding;
import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.services.FirebaseServicesProvider;
import fr.joudar.go4lunch.domain.utils.Calculus;
import fr.joudar.go4lunch.domain.utils.Callback;
import fr.joudar.go4lunch.ui.activities.HomepageActivity;
import fr.joudar.go4lunch.ui.core.adapters.ColleaguesListAdapter;
import fr.joudar.go4lunch.ui.core.adapters.RestaurantDetailsPictureListAdapter;
import fr.joudar.go4lunch.viewmodel.HomepageViewModel;

@AndroidEntryPoint
public class RestaurantDetailsFragment extends Fragment {

    private final String TAG = "RestaurantDetailsFrag";
    boolean isFirstInit = true;

    // Error code for emptyColleaguesListMessage method:
    final int NO_JOINING_COLLEAGUES_CODE = 1;
    final int NO_WORKPLACE_SELECTED_CODE = 2;
    final int ERROR_FETCHING_COLLEAGUES_LIST = 3;
    final int ERROR_FETCHING_PLACE_DETAILS = 4;

    FragmentRestaurantDetailsBinding binding;
    HomepageViewModel viewModel;
    Place place;
    List<String> likedPlaces = new ArrayList<>();

    User[] users = new User[0];

    View.OnClickListener favoriteRestaurantBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String id = viewModel.getCurrentUser().getChosenRestaurantId();
            if (id != null && id.equals(place.getId())) {
                favoriteRestaurantBtnHandler(false);
                viewModel.resetChosenRestaurant();

            }
            else {
                viewModel.getCurrentUser().setChosenRestaurant(place);
                viewModel.updateCurrentUserData(FirebaseServicesProvider.CHOSEN_RESTAURANT_ID, place.getId());
                favoriteRestaurantBtnHandler(true);
            }
        }
    };

    View.OnClickListener callBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final Uri phoneUri = Uri.parse("tel:" + place.getPhoneNumber());
            startActivity(new Intent(Intent.ACTION_VIEW, phoneUri));
        }
    };

    View.OnClickListener likeBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (checkIfPlaceLiked()) {
                likedPlaces.remove(place.getId());
                likeBtnHandler(false);
            }
            else {
                likedPlaces.add(place.getId());
                likeBtnHandler(true);
            }
            viewModel.updateCurrentUserData(FirebaseServicesProvider.LIKED_RESTAURANTS_ID_LIST, likedPlaces);
        }
    };

    View.OnClickListener websiteBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final Uri websiteUri = Uri.parse(place.getWebsiteUrl());
            startActivity(new Intent(Intent.ACTION_VIEW, websiteUri));
        }
    };

    RestaurantDetailsPictureListAdapter photosAdapter;

    ColleaguesListAdapter colleaguesAdapter = new ColleaguesListAdapter(null);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        binding = FragmentRestaurantDetailsBinding.inflate(LayoutInflater.from(container.getContext()), container, false);
        initViewModel(container);
        photosAdapter = new RestaurantDetailsPictureListAdapter(getContext());
        setupPhotosRecyclerView();
        setupColleaguesRecyclerView();
        return binding.getRoot();
    }

    /***********************************************************************************************
     ** ViewModel
     **********************************************************************************************/
    // Init the PlacesViewModel
    private void initViewModel(View fragmentContainer) {
        Log.d(TAG, "initViewModel");
        final NavController navController = Navigation.findNavController(fragmentContainer);
        final NavBackStackEntry backStackEntry = navController.getBackStackEntry(R.id.nav_graph);
        ViewModelProvider viewModelProvider = new ViewModelProvider(
                backStackEntry,
                HiltViewModelFactory.createInternal(getActivity(), backStackEntry, null, null));
        viewModel = viewModelProvider.get(HomepageViewModel.class);

        viewModel.getLiveCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (isFirstInit && user != null) {
                likedPlaces = viewModel.getCurrentUser().getLikedRestaurantsIdList();
                fetchRestaurantDetails();
                isFirstInit = false;
            }
        });
    }

    /***********************************************************************************************
     ** Calls
     **********************************************************************************************/
    // Fetches the place details
    private void fetchRestaurantDetails() {
        Log.d(TAG, "fetchRestaurantDetails");
        final String placeId = getArguments().getString("placeId");
        viewModel.getPlaceDetails(placeId, new Callback<Place>() {
            @Override
            public void onSuccess(Place results) {
                place = results;
                updateRestaurantDetailsViews();
                fetchJoiningColleagues(placeId);
            }

            @Override
            public void onFailure() {
                emptyColleaguesListMessage(ERROR_FETCHING_PLACE_DETAILS);
            }
        });
    }

    // Fetches the joining colleagues
    private void fetchJoiningColleagues(String placeId) {
        Log.d(TAG, "fetchJoiningColleagues");
        if (!viewModel.isWorkplaceIdSet())
            emptyColleaguesListMessage(NO_WORKPLACE_SELECTED_CODE);
        else
            viewModel.getColleaguesByRestaurant(placeId, new Callback<User[]>() {
                @Override
                public void onSuccess(User[] results) {
                    users = results;
                    updateColleaguesRecyclerView();
                }

                @Override
                public void onFailure() {
                    emptyColleaguesListMessage(ERROR_FETCHING_COLLEAGUES_LIST);
                }
            });
    }

    /***********************************************************************************************
     ** Views update
     **********************************************************************************************/
    // Updates the fragment views
    private void updateRestaurantDetailsViews() {
        Log.d(TAG, "updateRestaurantDetailsViews");
        updatePhotosRecyclerView();
        updateTextViews();
        ratingStarsHandler();
        updateFavoriteRestaurantBtn();
        updateCallBtn();
        updateLikeBtn();
        updateWebsiteBtn();
    }

    /***********************************************************************************************
     ** Place's Name and Address
     **********************************************************************************************/
    public void updateTextViews(){
        Log.d(TAG, "updateTextViews");
        binding.restaurantName.setText(place.getName());
        binding.address.setText(place.getVicinity());
    }
    /***********************************************************************************************
     ** Rating stars
     **********************************************************************************************/
    // Update the rating stars
    private void ratingStarsHandler() {
        Log.d(TAG, "ratingStarsHandler");
        int rating = Calculus.ratingStarsCalculator(place.getRating());
        switch (rating) {
            case 3:
                binding.rateStarN1.setVisibility(View.VISIBLE);
                binding.rateStarN2.setVisibility(View.VISIBLE);
                binding.rateStarN3.setVisibility(View.VISIBLE);
                break;
            case 2:
                binding.rateStarN1.setVisibility(View.VISIBLE);
                binding.rateStarN2.setVisibility(View.VISIBLE);
                binding.rateStarN3.setVisibility(View.GONE);
                break;
            case 1:
                binding.rateStarN1.setVisibility(View.VISIBLE);
                binding.rateStarN2.setVisibility(View.GONE);
                binding.rateStarN3.setVisibility(View.GONE);
                break;
            default:
                binding.rateStarN1.setVisibility(View.GONE);
                binding.rateStarN2.setVisibility(View.GONE);
                binding.rateStarN3.setVisibility(View.GONE);
        }
    }

    /***********************************************************************************************
     ** Favorite restaurant button
     **********************************************************************************************/
    // Updates favoriteRestaurant button
    private void updateFavoriteRestaurantBtn(){
        Log.d(TAG, "updateFavoriteRestaurantBtn");
        String id = viewModel.getCurrentUser().getChosenRestaurantId();
        favoriteRestaurantBtnHandler(id != null && id.equals(place.getId()));
        binding.btnSelectFavoriteRestaurant.setOnClickListener(favoriteRestaurantBtnListener);
    }

    private void favoriteRestaurantBtnHandler(boolean isFavorite) {
        Log.d(TAG, "favoriteRestaurantBtnHandler");
        if (isFavorite)
            binding.btnSelectFavoriteRestaurant.setImageResource(R.drawable.ic_check_circle_24_checked);
        else
            binding.btnSelectFavoriteRestaurant.setImageResource(R.drawable.ic_check_circle_24_unchecked);
    }

    /***********************************************************************************************
     ** Call button
     **********************************************************************************************/
    // Updates the callButton
    private void updateCallBtn() {
        Log.d(TAG, "updateCallBtn");
        if (place.getPhoneNumber() == null)
            binding.callButton.setEnabled(false);
        else
            binding.callButton.setOnClickListener(callBtnListener);
    }

    /***********************************************************************************************
     ** Like button
     **********************************************************************************************/
    // Updates the likeButton
    private void updateLikeBtn() {
        Log.d(TAG, "updateLikeBtn");
        likeBtnHandler(checkIfPlaceLiked());
        binding.likeButton.setOnClickListener(likeBtnListener);
    }

    private boolean checkIfPlaceLiked(){
        Log.d(TAG, "checkIfPlaceLiked");
        return (likedPlaces != null && likedPlaces.contains(place.getId()));
    }

    private void likeBtnHandler(boolean isLiked) {
        Log.d(TAG, "likeBtnHandler");
        final Drawable likeBtnDrawable;
        if (isLiked)
            likeBtnDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_star_full_24, null);
        else
            likeBtnDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_star_border_24, null);
        likeBtnDrawable.setBounds(binding.likeButton.getCompoundDrawables()[1].getBounds());
        binding.likeButton.setCompoundDrawables(null, likeBtnDrawable, null, null);
    }

    /***********************************************************************************************
     ** Website button
     **********************************************************************************************/
    // Updates the websiteButton
    private void updateWebsiteBtn() {
        Log.d(TAG, "updateWebsiteBtn");
        if (place.getWebsiteUrl() == null)
            binding.websiteButton.setEnabled(false);
        else
            binding.websiteButton.setOnClickListener(websiteBtnListener);
    }

    /***********************************************************************************************
     ** Photos RecyclerView
     **********************************************************************************************/
    //Sets up the photos' horizontal recyclerView
    private void setupPhotosRecyclerView() {
        Log.d(TAG, "setupPhotosRecyclerView");
        binding.restaurantPhotosRecyclerview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.restaurantPhotosRecyclerview.setAdapter(photosAdapter);
    }

    // Updates recyclerView's adapter's data
    private void updatePhotosRecyclerView() {
        Log.d(TAG, "updatePhotosRecyclerView");
        if (place.getAllPhotos().length == 0)
            onEmptyPhotosRecyclerView();
        else
            onLoadPhotosRecyclerView();
    }

    private void onEmptyPhotosRecyclerView() {
        Log.d(TAG, "onEmptyPhotosRecyclerView");
        binding.photoShimmerLayout.stopShimmer();
        binding.photoShimmerLayout.setVisibility(View.GONE);
        binding.photo.setVisibility(View.VISIBLE);
        binding.shadow.setVisibility(View.VISIBLE);
    }

    private void onLoadPhotosRecyclerView() {
        Log.d(TAG, "onLoadPhotosRecyclerView");
        photosAdapter.updateData(place.getAllPhotos());
        binding.photoShimmerLayout.stopShimmer();
        binding.photoShimmerLayout.setVisibility(View.GONE);
        binding.restaurantPhotosRecyclerview.setVisibility(View.VISIBLE);
        binding.photo.setVisibility(View.GONE);
        binding.shadow.setVisibility(View.VISIBLE);

    }
    /***********************************************************************************************
     ** Colleagues RecyclerView
     **********************************************************************************************/

    private void setupColleaguesRecyclerView() {
        Log.d(TAG, "setupColleaguesRecyclerView");
        binding.joiningColleagues.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.joiningColleagues.setAdapter(colleaguesAdapter);
    }

    // Updates colleagues' vertical recyclerView
    private void updateColleaguesRecyclerView() {
        Log.d(TAG, "updateColleaguesRecyclerView");
        if (users == null || users.length == 0)
            emptyColleaguesListMessage(NO_JOINING_COLLEAGUES_CODE);
        else
            colleaguesAdapter.updateData(users);
    }

    /***********************************************************************************************
     ** Toolbar & Option menu
     **********************************************************************************************/

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.findItem(R.id.search).setVisible(false);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        ((HomepageActivity)getActivity()).bottomNavVisibility(View.INVISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        ((HomepageActivity)getActivity()).bottomNavVisibility(View.VISIBLE);
    }

    /***********************************************************************************************
     ** Error handling
     **********************************************************************************************/

    private void emptyColleaguesListMessage(int errorCode) {
        Log.d(TAG, "emptyColleaguesListMessage");
        binding.joiningColleagues.setVisibility(View.GONE);
        binding.emptyColleaguesListLayout.setVisibility(View.VISIBLE);
        switch (errorCode) {
            case NO_JOINING_COLLEAGUES_CODE:
                binding.emptyColleaguesListTextview.setText(R.string.no_colleagues_joining_message);
                break;
            case NO_WORKPLACE_SELECTED_CODE:
                binding.emptyColleaguesListTextview.setText(R.string.no_workplace_selected_message);
                break;
            case ERROR_FETCHING_COLLEAGUES_LIST:
                binding.emptyColleaguesListTextview.setText(R.string.error_fetching_colleagues_list_message);
                break;
            case ERROR_FETCHING_PLACE_DETAILS:
                Toast.makeText(getContext(), R.string.error_fetching_place_details, Toast.LENGTH_LONG).show();
                getParentFragmentManager().popBackStackImmediate();
                break;
            default:
                binding.emptyColleaguesListTextview.setText(R.string.default_empty_list_message);
                break;
        }
    }

}