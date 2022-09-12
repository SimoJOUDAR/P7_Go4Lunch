package fr.joudar.go4lunch.ui.fragments.colleaguesList;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

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

    private final String TAG = "ColleaguesListFragment";

    // Error code for emptyListMessage method:
    final int NO_COLLEAGUES_CODE = 11;
    final int ERROR_FETCHING_COLLEAGUES_CODE = 22;
    final int NO_WORKPLACE_SELECTED_CODE = 33;

    private FragmentColleaguesListBinding binding;
    private HomepageViewModel viewModel;
    private User[] colleagues = new User[0];
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
    private ColleaguesListAdapter adapter = new ColleaguesListAdapter(onClickCallback);

    public ColleaguesListFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        binding = FragmentColleaguesListBinding.inflate(LayoutInflater.from(container.getContext()), container, false);
        initViewModel(container);
        initRecyclerView();
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
        Log.d(TAG, "initViewModel");
        final NavController navController = Navigation.findNavController(fragmentContainer);
        final NavBackStackEntry backStackEntry = navController.getBackStackEntry(R.id.nav_graph);
        ViewModelProvider viewModelProvider = new ViewModelProvider(
                backStackEntry,
                HiltViewModelFactory.createInternal(getActivity(), backStackEntry, null, null));
        viewModel = viewModelProvider.get(HomepageViewModel.class);
        viewModel.getLiveCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                checkWorkplaceAvailable();
            }
        });
    }

    /***********************************************************************************************
     ** Data fetching
     **********************************************************************************************/

    private void checkWorkplaceAvailable() {
        Log.d(TAG, "checkWorkplaceAvailable");
        if (viewModel.getWorkplaceId() == null || viewModel.getWorkplaceId().isEmpty())
            emptyListMessage(NO_WORKPLACE_SELECTED_CODE);
        else
            fetchData();
    }

    private void fetchData(){
        Log.d(TAG, "fetchData");
        viewModel.getColleagues(new Callback<User[]>() {
            @Override
            public void onSuccess(User[] results) {
                colleagues = results;
                updateRecyclerView();
            }

            @Override
            public void onFailure() {
                colleagues = null;
                emptyListMessage(ERROR_FETCHING_COLLEAGUES_CODE);
            }
        });
    }

    /***********************************************************************************************
     ** RecyclerView
     **********************************************************************************************/
    // Init the RecyclerView within the main thread
    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView");
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        binding.recyclerview.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        binding.recyclerview.setAdapter(adapter);
    }

    // Updates the RecyclerView's Adapter's data from the Call's onResponse background thread
    private void updateRecyclerView() {
        Log.d(TAG, "updateRecyclerView");
        if (colleagues.length == 0)
            emptyListMessage(NO_COLLEAGUES_CODE);
        else
            adapter.updateData(colleagues);
    }

    /***********************************************************************************************
     ** Error messages
     **********************************************************************************************/

    private void emptyListMessage(int errorCode) {
        Log.d(TAG, "emptyListMessage");
        binding.recyclerview.setVisibility(View.GONE);
        binding.emptyListMsgLayout.setVisibility(View.VISIBLE);
        switch (errorCode) {
            case NO_COLLEAGUES_CODE:
                binding.emptyListMsg.setText(R.string.NoColleagues_msg);
                break;
            case ERROR_FETCHING_COLLEAGUES_CODE:
                binding.emptyListMsg.setText(R.string.ErrorFetchingColleagues_msg);
                break;
            case NO_WORKPLACE_SELECTED_CODE:
                binding.emptyListMsg.setText(R.string.NoWorkplaceSelected_msg);
                binding.noWorkplaceErrorMsgBtn.setVisibility(View.VISIBLE);
                binding.noWorkplaceErrorMsgBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((HomepageActivity)getActivity()).launchWorkplacePickerDialog(null);
                    }
                });
                break;

            default:
                binding.emptyListMsg.setText(R.string.default_empty_list_message);
                break;
        }

    }
}