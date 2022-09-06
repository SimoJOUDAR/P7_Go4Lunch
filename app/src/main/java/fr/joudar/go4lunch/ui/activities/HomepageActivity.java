package fr.joudar.go4lunch.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.databinding.ActivityHomepageBinding;
import fr.joudar.go4lunch.domain.core.LocationPermissionHandler;
import fr.joudar.go4lunch.domain.core.notification.NotificationDataFetching;
import fr.joudar.go4lunch.domain.models.Autocomplete;
import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.services.CurrentLocationProvider;
import fr.joudar.go4lunch.domain.utils.Callback;
import fr.joudar.go4lunch.ui.core.adapters.AutocompleteListAdapter;
import fr.joudar.go4lunch.ui.core.dialogs.TimeDialogPreference;
import fr.joudar.go4lunch.ui.core.dialogs.WorkplaceDialogFragment;
import fr.joudar.go4lunch.viewmodel.HomepageViewModel;
import pub.devrel.easypermissions.EasyPermissions;

@AndroidEntryPoint
public class HomepageActivity extends AppCompatActivity {

    // UI
    ActivityHomepageBinding binding;
    NavHostFragment navHostFragment;
    NavController navController;
    Menu menu;
    BottomNavigationView bottomNav;
    private final AutocompleteListAdapter autocompleteListAdapter = new AutocompleteListAdapter(new Callback<String>() {
        @Override
        public void onSuccess(String results) {
            navigateToPlaceDetailsFragment(results);
        }

        @Override
        public void onFailure() {

        }
    });

    // Domain
    @Inject public LocationPermissionHandler mLocationPermissionHandler;
    @Inject public CurrentLocationProvider currentLocationProvider;
    private HomepageViewModel homepageViewModel;
    private Location currentLocation;
    private User currentUser;
    private static WorkplaceDialogFragment workplaceDialog;


    /***********************************************************************************************
     ** The onCreate method
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomepageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InitNavigation();
        initCurrentLocation();
        initViewModel();
    }
    /***********************************************************************************************
     ** Navigation
     **********************************************************************************************/

    // Sets up the NavHost (fragments), the Toolbar, the DrawerNavigation and the BottomNavigation.
    private void InitNavigation() {

        // Gets the NavHost's NavController (used to setup the Toolbar, the DrawerNavigation and the BottomNavigation.).
        navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_container);
        assert navHostFragment != null;  // (To avoid 'NullPointerException' produced by 'getNavController' below)
        navController = navHostFragment.getNavController();

        //Sets our Toolbar.
        setSupportActionBar(binding.toolbar);
        NavigationUI.setupActionBarWithNavController(this, navController, getAppBarConfiguration());

        // Sets the DrawerNav.
        NavigationUI.setupWithNavController(binding.drawerNav, navController);
        binding.drawerNav.setNavigationItemSelectedListener(
                this::onDrawerNavMenuItemSelected);

        // Sets the BottomNav.
        NavigationUI.setupWithNavController(binding.bottomNav, navController);
        this.bottomNav = binding.bottomNav;
    }

    // Initializes the DrawerNav data display
    private void initDrawerNavDataDisplay() {
        final View drawerNavHeader = binding.drawerNav.getHeaderView(0);
        final ImageView userAvatar = drawerNavHeader.findViewById(R.id.drawer_nav_user_avatar);
        Glide.with(this).load(currentUser.getAvatarUrl()).centerCrop().into(userAvatar);
        ((TextView) drawerNavHeader.findViewById(R.id.drawer_nav_username)).setText(currentUser.getUsername());
        ((TextView) drawerNavHeader.findViewById(R.id.drawer_nav_email)).setText(currentUser.getEmail());
    }

    // Sets up the BottomNavigation components.
    private AppBarConfiguration getAppBarConfiguration() {
        return new AppBarConfiguration.Builder(
                R.id.mapFragment,
                R.id.restaurantsListFragment,
                R.id.colleaguesListFragment)
                .setOpenableLayout(binding.getRoot())
                .build();
    }

    // Defines all of our 3 fragments as top level destination, so that the up button won't concern them.
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, getAppBarConfiguration()) || super.onSupportNavigateUp();
    }

    // Sets up option menu items action (YOUR LUNCH, SETTINGS, LOGOUT).
    private boolean onDrawerNavMenuItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.your_lunch:
                displayChosenRestaurant();
                return true;
            case R.id.logout:
                Toast.makeText(this, R.string.signed_out, Toast.LENGTH_SHORT).show();
                homepageViewModel.logout();
                return true;
            case R.id.settingsFragment:
                //Navigation.findNavController(binding.navHostFragmentContainer).navigate(R.id.settingsFragment);
                navController.navigate(R.id.settingsFragment);
                binding.getRoot().closeDrawer(binding.drawerNav, false);
                return true;
        }
        return true;
    }

    // Opens up the RestaurantDetailsFragment to display the chosen restaurant
    private void displayChosenRestaurant() {
        String id = currentUser.getChosenRestaurantId();
        if (id != null && !id.isEmpty()) {
            Toast.makeText(this, R.string.your_lunch_toast, Toast.LENGTH_SHORT).show();
            navigateToPlaceDetailsFragment(id);
            binding.getRoot().closeDrawer(binding.drawerNav, false);
        }
        else {
            Snackbar snackbar = Snackbar.make(binding.getRoot(), R.string.empty_chosen_restaurant, Snackbar.LENGTH_SHORT);
            snackbar.setAction(R.string.empty_chosen_restaurant_snackbar_action, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(HomepageActivity.this, R.string.empty_chosen_restaurant_toast, Toast.LENGTH_SHORT).show();
                }
            });
            snackbar.show();
        }
    }

    // Opens up the RestaurantDetailsFragment to display the said restaurant
    public void navigateToPlaceDetailsFragment(String placeId){
        Bundle bundle = new Bundle();
        bundle.putString("placeId", placeId);
        //Navigation.findNavController(binding.navHostFragmentContainer).navigate(R.id.settingsFragment, bundle);
        navController.navigate(R.id.restaurantDetailsFragment, bundle);
    }

    /***********************************************************************************************
     ** Searchbar
     **********************************************************************************************/
    // To connect our option menu to the Navigation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.toolbar_layout, menu);
        mapFragmentDisplayOptions();
        final SearchView searchbar = (SearchView) menu.findItem(R.id.search).getActionView();
        searchbar.setOnQueryTextFocusChangeListener(this::getSearchbarFocusListener);
        searchbar.setOnQueryTextListener(getQueryTextListener());
        searchbar.setQueryHint(getString(R.string.homepage_search_field_hint));
        return super.onCreateOptionsMenu(menu);
    }

    private void getSearchbarFocusListener(View v, boolean hasFocus) {
        if (hasFocus) {
            initCurrentLocation();
            binding.autocompleteList.setLayoutManager(new LinearLayoutManager(this));
            binding.autocompleteList.setAdapter(autocompleteListAdapter);
            binding.searchView.setVisibility(View.VISIBLE);
        } else {
            binding.searchView.setVisibility(View.GONE);
        }
    }

    private SearchView.OnQueryTextListener getQueryTextListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE); //  The central point of the system that manages interaction between applications and the current accessing input method.
                inputMethodManager.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);  // Hides the keyboard
                //TODO: Config it to also show result in map ?
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (currentLocation != null && newText.length() >= 3) {

                    homepageViewModel.getAutocompletes(newText, currentLocation, getSearchRadius(), true, new Callback<Autocomplete[]>() {
                        @Override
                        public void onSuccess(Autocomplete[] results) {
                            autocompleteListAdapter.updateAutocompleteList(results);
                        }

                        @Override
                        public void onFailure() {

                        }
                    });
                }
                return false;
            }
        };
    }

    /***********************************************************************************************
     ** Init currentLocation
     **********************************************************************************************/
    // Initializes the currentLocation
    private void initCurrentLocation() {
        currentLocationProvider.getCurrentCoordinates(new CurrentLocationProvider.OnCoordinatesResultListener() {
            @Override
            public void onResult(Location location) {
                currentLocation = location;
            }
        });
    }

    /***********************************************************************************************
     ** Init ViewModel
     **********************************************************************************************/
    // Initializes the HomepageViewModel, sets the onAuthStateChangedListener, sets the currentUser observer
    private void initViewModel() {
        Log.d("HomepageActivity", "initViewModel_1");
        homepageViewModel = new ViewModelProvider(this).get(HomepageViewModel.class);

        Log.d("HomepageActivity", "initViewModel_2");
        FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    onLogout();
                } else {
                    initDrawerNavDataDisplay();
                    initInitialUserData();
                    initSharedPreferences();
                    initLunchNotification();
                }
            }
        };

        Log.d("HomepageActivity", "initViewModel_3");
        homepageViewModel.initListener(authStateListener);
        Log.d("HomepageActivity", "initViewModel_4");
        currentUser = homepageViewModel.getCurrentUser();
        Log.d("HomepageActivity", "initViewModel_5");
        homepageViewModel.getLiveCurrentUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                currentUser = user;
                initDrawerNavDataDisplay();
            }
        });

        Log.d("HomepageActivity", "initViewModel _initFirebaseAuth_Finish_");

    }

    //If logged out, it takes us back to AuthenticationActivity
    private void onLogout(){
//        startActivity(new Intent(this, AuthenticationActivity.class));
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.commitAllowingStateLoss();   // To avoid "Can not perform this action after onSaveInstanceState" IllegalStateException, if we want to log back in.
        Intent intent = new Intent(this, AuthenticationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

        Log.d("HomepageActivity", "onLogout");
    }

    public void deleteCurrentUserAccount(){
        homepageViewModel.deleteCurrentUserAccount(new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean results) {
                Toast.makeText(HomepageActivity.this, R.string.account_successfully_deleted_msg, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomepageActivity.this, AuthenticationActivity.class));
                finish();
            }

            @Override
            public void onFailure() {
                Toast.makeText(HomepageActivity.this, R.string.account_not_deleted_error_msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /***********************************************************************************************
     ** Basic user data enquiries operations
     **********************************************************************************************/

    // Initializes the workplaceID
    private void initInitialUserData() {
        Log.d("HomepageActivity", "initInitialUserData");
        if (currentUser.getWorkplaceId() == null || currentUser.getWorkplaceId().isEmpty()) {

            launchWorkplacePickerDialog(new Callback<String>() {
                @Override
                public void onSuccess(String results) {
                    //TODO : Refresh fragment here, to load newly saved workplaceId
                }

                @Override
                public void onFailure() {

                }
            });
        }
    }

    // Calls the WorkplacePickerDialog and sends the selected result <Autocomplete> to the arg Callback
    public void launchWorkplacePickerDialog(Callback<String> innerCallback) {

        Log.d("HomepageActivity", "launchWorkplacePickerDialog_0");
        if (workplaceDialog == null) {

            // The WorkplacePickerDialog Callback to handle onTextChanged event
            Callback<String> onTextChanged = new Callback<String>() {
                @Override
                public void onSuccess(String results) {
                    if (results.length() >= 3) {
                        homepageViewModel.getAutocompletes(
                                results,
                                currentLocation,
                                getSearchRadius(),
                                false,
                                new Callback<Autocomplete[]>() {
                                    @Override
                                    public void onSuccess(Autocomplete[] results) {

                                        //TODO: test to delete -start
                                        int n = results.length;
                                        String m = "Dialog number_" + n;
                                        Log.d("HWDialogAutocomplete", m);
                                        //TODO: Test to delete -end

                                        workplaceDialog.updateWorkplaceDialogList(results);
                                    }

                                    @Override
                                    public void onFailure() {

                                        //TODO: test to delete -start
                                        String m = "Dialog number_Null";
                                        Log.d("HWDialogAutocomplete", m);
                                        //TODO: Test to delete -end

                                    }
                                });
                    }
                }

                @Override
                public void onFailure() {

                }
            };
            Log.d("HomepageActivity", "launchWorkplacePickerDialog_1");

            // The WorkplacePickerDialog Callback to handle onItemClick action :
            // updates currentUser' workplaceId  - updateCurrentUserData() - updates "workplace" and "workplaceId" sharedPreferences - dismisses the dialog - launches a informative an toast
            Callback<Autocomplete> onItemSelected = new Callback<Autocomplete>() {
                @Override
                public void onSuccess(Autocomplete results) {
                    // Updates currentUser workplaceId value
                    currentUser.setWorkplaceId(results.getPlaceId());
                    homepageViewModel.updateCurrentUserData("workplaceId", results.getPlaceId());

                    // Updates sharedPreferences workplaceId, workplaceTitle & workplaceDetail values
                    final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(HomepageActivity.this);
                    final SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                    sharedPreferencesEditor.putString("workplaceId", results.getPlaceId());
                    sharedPreferencesEditor.putString("workplace", results.getTitle()  + "\n" + results.getDetail());

                    innerCallback.onSuccess(results.getTitle()  + "\n" + results.getDetail());

                    sharedPreferencesEditor.apply();
                    workplaceDialog.dismiss();
                    Toast.makeText(HomepageActivity.this, R.string.workplace_dialog_toast, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure() {

                }
            };
            Log.d("HomepageActivity", "launchWorkplacePickerDialog_2");

            workplaceDialog = new WorkplaceDialogFragment(onTextChanged, onItemSelected);
            Log.d("HomepageActivity", "launchWorkplacePickerDialog_3");
        }

        workplaceDialog.show(getSupportFragmentManager(), getString(R.string.workplace_dialog_tag));
        Log.d("HomepageActivity", "launchWorkplacePickerDialog_4");

        // TODO: initInitialUserData(); // Relaunch the dialog in case the workplace hasn't been selected

    }

    /***********************************************************************************************
     ** Notification
     **********************************************************************************************/
    // Schedules the Alarm for Notifications
    private void initLunchNotification(){
        if (homepageViewModel.isCurrentUserNew()) {
            Calendar dueDate = new TimeDialogPreference(this).getPersistedTime();
            //new LunchAlarmHandler(this).scheduleLunchAlarm(time, LunchAlarmReceiver.class);
            scheduleNotificationJob(getApplicationContext(),dueDate);
        }
    }

    public void scheduleNotificationJob(Context context, @Nullable Calendar dueDate) {
        final String JOB_TAG = "NOTIFICATION_DATA_FETCHING_JOB";
        Calendar currentDate = Calendar.getInstance();
        long timeDiff =  dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        final PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(NotificationDataFetching.class,24, TimeUnit.HOURS)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .addTag(JOB_TAG)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(JOB_TAG, ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest);
    }

    public String getSearchRadius() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(HomepageActivity.this);
        return sharedPreferences.getString("search_radius", "2000");
    }

    /***********************************************************************************************
     ** SharedPreferences
     **********************************************************************************************/
    // Manages the SharedPreferences (to save basic user info)
    private void initSharedPreferences() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        if ( !sharedPreferences.getString("owner", "").equals(currentUser.getId()) ) {
            sharedPreferencesEditor.clear();
            sharedPreferencesEditor.putString("owner", currentUser.getId());
        }
        if ( !sharedPreferences.getString("workplaceId", "0").equals(currentUser.getWorkplaceId()) ) {
            sharedPreferencesEditor.putString("workplaceId", currentUser.getWorkplaceId());
            homepageViewModel.getPlaceDetails(currentUser.getWorkplaceId(), new Callback<Place>() {
                @Override
                public void onSuccess(Place results) {
                    sharedPreferencesEditor.putString("workplaceTitle", results.getName());
                    sharedPreferencesEditor.putString("workplaceDetail", results.getVicinity());
                }

                @Override
                public void onFailure() {

                }
            });

        }

        sharedPreferencesEditor.apply();
    }

    /***********************************************************************************************
     ** Permissions
     **********************************************************************************************/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, mLocationPermissionHandler);
    }

    /***********************************************************************************************
    ** Fragments display options
    **********************************************************************************************/
    // Visibility setups for the toolbar, menu and bottomNav

    //TODO: How to control these components straight from the fragments ? (to avoid calling the methods below from this parent activity, which might result in bugs)

    public void mapFragmentDisplayOptions(){
        actionbarVisibility(true);
        menuVisibility(false);
        bottomNavigationVisibility(true);
    }

    //TODO: Check why search menu is displayed twice
    public void restaurantsListFragmentDisplayOptions(){
        actionbarVisibility(true);
        menuVisibility(true);
        bottomNavigationVisibility(true);
    }

    public void colleaguesListFragmentDisplayOptions(){
        actionbarVisibility(true);
        menuVisibility(false);
        bottomNavigationVisibility(true);
    }

    //TODO: Display UpButton
    public void restaurantDetailsFragmentDisplayOptions(){
        actionbarVisibility(false);
        bottomNavigationVisibility(false);
    }
    //TODO: Display UpButton
    public void settingsFragmentDisplayOptions(){
        actionbarVisibility(false);
        bottomNavigationVisibility(false);
    }

    public void actionbarVisibility(boolean val) {
        if(val) {
            if(getSupportActionBar() != null && !getSupportActionBar().isShowing()) {
                getSupportActionBar().show();
            }
        } else {
            if(getSupportActionBar() != null && getSupportActionBar().isShowing()) {
                getSupportActionBar().hide();
            }
        }
    }
    public void menuVisibility(boolean val) {
        if (val) {
            if(menu != null && !menu.findItem(R.id.sort).isVisible()) {
                menu.findItem(R.id.sort).setVisible(true);
            }
        } else {
            if(menu != null && menu.findItem(R.id.sort).isVisible()) {
                menu.findItem(R.id.sort).setVisible(false);
            }
        }
    }
    public void bottomNavigationVisibility(boolean val) {
        if (val) {
            if(bottomNav != null && bottomNav.getVisibility() != View.VISIBLE) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        } else {
            if(bottomNav != null && bottomNav.getVisibility() == View.VISIBLE) {
                bottomNav.setVisibility(View.INVISIBLE);
            }
        }
    }

}