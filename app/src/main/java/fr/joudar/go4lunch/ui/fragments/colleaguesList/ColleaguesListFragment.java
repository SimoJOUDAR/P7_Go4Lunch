package fr.joudar.go4lunch.ui.fragments.colleaguesList;

import android.os.Bundle;

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

import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory;
import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.databinding.FragmentColleaguesListBinding;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.utils.Callback;
import fr.joudar.go4lunch.ui.activities.HomepageActivity;
import fr.joudar.go4lunch.ui.core.adapters.ColleaguesListAdapter;
import fr.joudar.go4lunch.viewmodel.HomepageViewModel;

@AndroidEntryPoint
public class ColleaguesListFragment extends Fragment {

    private FragmentColleaguesListBinding binding;
    private HomepageViewModel viewModel;
    private User currentUser;
    private User[] colleagues;
    private ColleaguesListAdapter adapter;
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

    public ColleaguesListFragment() {} // TODO: safe delete ?


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentColleaguesListBinding.inflate(LayoutInflater.from(container.getContext()), container, false);
        initViewModel(container);
        currentUser = viewModel.getCurrentUser();
        viewModel.getLiveCurrentUser().observe(getViewLifecycleOwner(), user -> currentUser = user);
        checkWorkplaceAvailable();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomepageActivity)getActivity()).colleaguesListFragmentDisplayOptions();
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
     ** Data fetching
     **********************************************************************************************/

    private void checkWorkplaceAvailable() {
        if (currentUser.getWorkplaceId() == null || currentUser.getWorkplaceId().isEmpty())
            handleNoWorkplaceSelectedError();
        else
            fetchData();
    }

    private void fetchData(){
        viewModel.getColleagues(new Callback<User[]>() {
            @Override
            public void onSuccess(User[] results) {
                colleagues = results;
                initRecyclerView();
            }

            @Override
            public void onFailure() {
                colleagues = null;
                handleErrorFetchingColleagues();
            }
        });
    }

    /***********************************************************************************************
     ** RecyclerView
     **********************************************************************************************/

    private void initRecyclerView() {
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        adapter = new ColleaguesListAdapter(colleagues, onClickCallback);
        binding.recyclerview.setAdapter(adapter);
    }

    /***********************************************************************************************
     ** Error messages
     **********************************************************************************************/

    private void handleNoWorkplaceSelectedError() {
        binding.noWorkplaceErrorMsgLayout.setVisibility(View.VISIBLE);
        binding.noWorkplaceErrorMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((HomepageActivity)getActivity()).launchWorkplacePickerDialog(new Callback<String>() {
                    @Override
                    public void onSuccess(String results) {
                        binding.noWorkplaceErrorMsgLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure() {

                    }
                });
            }
        });
    }

    private void handleErrorFetchingColleagues() {
        // TODO : handle here
    }
}