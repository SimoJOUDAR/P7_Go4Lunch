package fr.joudar.go4lunch.ui.fragments.restaurantDetails;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

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

    final String TAG = "RestaurantDetailsFragment";

    // Error code for emptyColleaguesListMessage method:
    final int NO_JOINING_COLLEAGUES_CODE = 1;
    final int NO_WORKPLACE_SELECTED_CODE = 2;
    final int ERROR_FETCHING_COLLEAGUES_LIST = 3;

    FragmentRestaurantDetailsBinding binding;
    HomepageViewModel viewModel;
    Place place;
    User[] users;
    User currentUser;

    RequestListener<Drawable> RVPhotosListener = new RequestListener<Drawable>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            binding.restaurantPhotosRecyclerview.setVisibility(View.GONE);
            binding.photo.setVisibility(View.VISIBLE);
            binding.photoShimmerLayout.stopShimmer();
            binding.photoShimmerLayout.setVisibility(View.GONE);
            binding.shadow.setVisibility(View.VISIBLE);
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            binding.photoShimmerLayout.stopShimmer();
            binding.photoShimmerLayout.setVisibility(View.GONE);
            binding.shadow.setVisibility(View.VISIBLE);
            return false;
        }
    };

    View.OnClickListener favoriteRestaurantBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String id = currentUser.getChosenRestaurantId();
            if (id != null && id.equals(place.getId())) {
                viewModel.resetChosenRestaurant();
                favoriteRestaurantBtnHandler(false);
            }
            else {
                currentUser.setChosenRestaurantId(place.getId());
                currentUser.setChosenRestaurantName(place.getName());
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
            final List<String> likedPlaces = currentUser.getLikedRestaurantsIdList();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRestaurantDetailsBinding.inflate(LayoutInflater.from(container.getContext()), container, false);
        initViewModel(container);
        currentUser = viewModel.getCurrentUser();
        viewModel.getLiveCurrentUser().observe(getViewLifecycleOwner(), user -> currentUser = user);
        fetchRestaurantDetails();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomepageActivity)getActivity()).restaurantDetailsFragmentDisplayOptions();
    }

    /***********************************************************************************************
     ** ViewModel
     **********************************************************************************************/
    // Init the PlacesViewModel
    private void initViewModel(View fragmentContainer) {
        Log.d("DetailsFragment", "initViewModel _started_");
        final NavController navController = Navigation.findNavController(fragmentContainer);
        final NavBackStackEntry backStackEntry = navController.getBackStackEntry(R.id.nav_graph);
        ViewModelProvider viewModelProvider = new ViewModelProvider(
                backStackEntry,
                HiltViewModelFactory.createInternal(getActivity(), backStackEntry, null, null));
        viewModel = viewModelProvider.get(HomepageViewModel.class);
        Log.d("DetailsFragment", "initViewModel _finished_");
    }

    /***********************************************************************************************
     ** Calls
     **********************************************************************************************/
    // Fetches the place details
    private void fetchRestaurantDetails() {
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
                errorFetchingPlaceDetails();
            }
        });
    }

    // Fetches the joining colleagues
    private void fetchJoiningColleagues(String placeId) {
        String workplaceID = currentUser.getWorkplaceId();
        if (workplaceID == null || workplaceID.isEmpty())
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
        updatePhotosRecyclerView();
        updateTextViews();
        ratingStarsHandler();
        updateFavoriteRestaurantBtn();
        updateCallBtn();
        updateLikeBtn();
        updateWebsiteBtn();
    }

    private void errorFetchingPlaceDetails(){
        //TODO : case fetching place details when wrong
        Toast.makeText(getContext(), R.string.error_fetching_place_details, Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStackImmediate();
    }

    /***********************************************************************************************
     ** Rating stars
     **********************************************************************************************/
    public void updateTextViews(){
        binding.name.setText(place.getName());
        binding.address.setText(place.getVicinity());
    }
    /***********************************************************************************************
     ** Rating stars
     **********************************************************************************************/
    // Update the rating stars
    private void ratingStarsHandler() {
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
        String id = currentUser.getChosenRestaurantId();
        favoriteRestaurantBtnHandler(id != null && id.equals(place.getId()));
        binding.btnSelectFavoriteRestaurant.setOnClickListener(favoriteRestaurantBtnListener);
    }

    private void favoriteRestaurantBtnHandler(boolean isFavorite) {
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
        likeBtnHandler(checkIfPlaceLiked());
        binding.likeButton.setOnClickListener(likeBtnListener);
    }

    private boolean checkIfPlaceLiked(){
        final List<String> likedPlaces = currentUser.getLikedRestaurantsIdList();
        return (likedPlaces != null && likedPlaces.contains(place.getId()));
    }

    private void likeBtnHandler(boolean isLiked) {
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
        if (place.getWebsiteUrl() == null)
            binding.websiteButton.setEnabled(false);
        else
            binding.websiteButton.setOnClickListener(websiteBtnListener);
    }

    /***********************************************************************************************
     ** Photos RecyclerView
     **********************************************************************************************/
    // Updates photos' horizontal recyclerView
    private void updatePhotosRecyclerView() {
        binding.restaurantPhotosRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.restaurantPhotosRecyclerview.setAdapter(new RestaurantDetailsPictureListAdapter(getContext(), place.getAllPhotos(), RVPhotosListener));
    }
    /***********************************************************************************************
     ** Colleagues RecyclerView
     **********************************************************************************************/
    // Updates colleagues' vertical recyclerView
    private void updateColleaguesRecyclerView() {
        if (users.length == 0)
            emptyColleaguesListMessage(NO_JOINING_COLLEAGUES_CODE);
        else {
            binding.joiningColleagues.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.joiningColleagues.setAdapter(new ColleaguesListAdapter(users, null));
        }
    }

    private void emptyColleaguesListMessage(int errorCode) {
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
            default:
                binding.emptyColleaguesListTextview.setText(R.string.default_empty_colleagues_list_message);
        }
    }

}