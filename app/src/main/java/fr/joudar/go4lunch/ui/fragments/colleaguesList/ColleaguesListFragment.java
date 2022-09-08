package fr.joudar.go4lunch.ui.fragments.colleaguesList;

import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
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

    public ColleaguesListFragment() {} // TODO: safe delete ?


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentColleaguesListBinding.inflate(LayoutInflater.from(container.getContext()), container, false);
        initViewModel(container);
        initRecyclerView();
//        checkWorkplaceAvailable();

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
        viewModel.getLiveCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                checkWorkplaceAvailable();
            }
        });
        Log.d("RestaurantsListFragment", "initViewModel _finished_");
    }

    /***********************************************************************************************
     ** Data fetching
     **********************************************************************************************/

    private void checkWorkplaceAvailable() {
        if (viewModel.getWorkplaceId() == null || viewModel.getWorkplaceId().isEmpty()) {

            //TODO: test to delete -start
            Log.d("ColleaguesListFragment", "checkWorkplaceAvailable - workplaceId = null");
            //TODO: Test to delete -end

            emptyListMessage(NO_WORKPLACE_SELECTED_CODE);
        }
        else {

            //TODO: test to delete -start
            Log.d("ColleaguesListFragment", "checkWorkplaceAvailable - workplaceId not null");
            //TODO: Test to delete -end

            fetchData();
        }
    }

    private void fetchData(){
        viewModel.getColleagues(new Callback<User[]>() {
            @Override
            public void onSuccess(User[] results) {

                //TODO: test to delete -start
                Log.d("ColleaguesListFragment", "fetchData - getColleagues - onSuccess()");
                //TODO: Test to delete -end

                colleagues = results;
                updateRecyclerView();
            }

            @Override
            public void onFailure() {

                //TODO: test to delete -start
                Log.d("ColleaguesListFragment", "fetchData - getColleagues - onSuccess()");
                //TODO: Test to delete -end

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

        //TODO: test to delete -start
        Log.d("ColleaguesListFragment", "initRecyclerView()");
        //TODO: Test to delete -end

        binding.recyclerview.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        binding.recyclerview.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        binding.recyclerview.setAdapter(adapter);
    }

    // Updates the RecyclerView's Adapter's data from the Call's onResponse background thread
    private void updateRecyclerView() {

        //TODO: test to delete -start
        Log.d("ColleaguesListFragment", "updateRecyclerView()");
        //TODO: Test to delete -end

        if (colleagues.length == 0) {

            //TODO: test to delete -start
            Log.d("ColleaguesListFragment", "updateRecyclerView() - colleagues = null");
            //TODO: Test to delete -end

            emptyListMessage(NO_COLLEAGUES_CODE);
        }
        else {

            //TODO: test to delete -start
            Log.d("ColleaguesListFragment", "updateRecyclerView() - colleagues not null - colleague[0] = " + colleagues[0].getUsername());
            //TODO: Test to delete -end

            adapter.updateData(colleagues);
        }
    }

    /***********************************************************************************************
     ** Error messages
     **********************************************************************************************/

    private void emptyListMessage(int errorCode) {
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
                        ((HomepageActivity)getActivity()).launchWorkplacePickerDialog(new Callback<String>() {
                            @Override
                            public void onSuccess(String results) {
//                                binding.noWorkplaceErrorMsgBtn.setVisibility(View.GONE);
//                                binding.emptyListMsgLayout.setVisibility(View.GONE);

                                //TODO: test to delete -start
                                Log.d("ColleaguesListFragment", "workPlace dialog - choice pressed - is refreshFragment() working?");
                                //TODO: Test to delete -end

                                // TODO: Refresh Fragment : use livedata
                            }

                            @Override
                            public void onFailure() {

                            }
                        });
                    }
                });
                break;

            default:
                binding.emptyListMsg.setText(R.string.default_empty_list_message);
                break;
        }

    }
}