package fr.joudar.go4lunch.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.databinding.ActivityHomepageBinding;
import fr.joudar.go4lunch.domain.core.LocationPermissionHandler;
import fr.joudar.go4lunch.domain.models.Autocomplete;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.services.CurrentLocationProvider;
import fr.joudar.go4lunch.domain.utils.Callback;
import fr.joudar.go4lunch.ui.core.adapters.AutocompleteListAdapter;
import fr.joudar.go4lunch.ui.core.dialogs.TimePreference;
import fr.joudar.go4lunch.ui.core.dialogs.WorkplaceDialogFragment;
import fr.joudar.go4lunch.viewmodel.HomepageViewModel;
import pub.devrel.easypermissions.EasyPermissions;

@AndroidEntryPoint
public class HomepageActivity extends AppCompatActivity {
    private final String TAG = "HomepageActivity";

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
    private static WorkplaceDialogFragment workplaceDialog;
    boolean firstInit = true;

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
        Glide.with(getApplicationContext()).load(homepageViewModel.getCurrentUser().getAvatarUrl()).centerCrop().into(userAvatar); // use of "applicationContext" instead of "activityContext" to avoid the error "You cannot start a load for a destroyed activity"
        ((TextView) drawerNavHeader.findViewById(R.id.drawer_nav_username)).setText(homepageViewModel.getCurrentUser().getUsername());
        ((TextView) drawerNavHeader.findViewById(R.id.drawer_nav_email)).setText(homepageViewModel.getCurrentUser().getEmail());
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
        if (homepageViewModel.isChosenRestaurantSet()) {
            Toast.makeText(this, R.string.your_lunch_toast, Toast.LENGTH_SHORT).show();
            navigateToPlaceDetailsFragment(homepageViewModel.getChosenRestaurantId());
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
     ** Current Location
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
     ** ViewModel and user data
     **********************************************************************************************/
    // Initializes the HomepageViewModel, sets the onAuthStateChangedListener, sets the currentUser observer
    private void initViewModel() {
        Log.d(TAG, "initViewModel");
        homepageViewModel = new ViewModelProvider(this).get(HomepageViewModel.class);
        homepageViewModel.getLiveCurrentUser().observe(this, user -> {
            if (user != null && firstInit) {
                initSharedPreferences();
                initUserWorkplaceId();
                firstInit = false;
            }
        });

        FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    onLogout();
                }
                else {
                    firstInit = true;
                    initDrawerNavDataDisplay();
                }
            }
        };
        homepageViewModel.initListener(authStateListener);
    }

    /***********************************************************************************************
     ** Menu - Searchbar
     **********************************************************************************************/
    // To connect our option menu to the Navigation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.toolbar_layout, menu);
        menu.findItem(R.id.sort).setVisible(false);
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
                        public void onFailure() {}
                    });
                }
                return false;
            }
        };
    }

    /***********************************************************************************************
     ** Basic Account functionalities
     **********************************************************************************************/

    //If logged out, it takes us back to AuthenticationActivity
    private void onLogout(){
        startActivity(new Intent(this, AuthenticationActivity.class));
        finish();

        Log.d(TAG, "onLogout");
    }

    public void deleteCurrentUserAccount(){
        homepageViewModel.deleteCurrentUserAccount(new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean results) {
                if (results)
                    Toast.makeText(HomepageActivity.this, R.string.account_successfully_deleted_msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure() {
                Toast.makeText(HomepageActivity.this, R.string.account_not_deleted_error_msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    /***********************************************************************************************
     ** SharedPreferences
     **********************************************************************************************/
    // Manages the SharedPreferences (to save basic user info)
    private void initSharedPreferences() {
        Log.d(TAG, "initSharedPreferences - Start - currentUser.getId() = " + homepageViewModel.getId());
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        if ( !sharedPreferences.getString("owner", "").equals(homepageViewModel.getId()) ) {
            sharedPreferencesEditor.clear();
            sharedPreferencesEditor.putString("owner", homepageViewModel.getId());
            sharedPreferencesEditor.putString("username", homepageViewModel.getCurrentUser().getUsername());
            initLunchNotification(); //If the currentUser has changed
        }
        if ( !sharedPreferences.getString("workplaceId", "0").equals(homepageViewModel.getWorkplaceId()) ) {
            if (homepageViewModel.isWorkplaceIdSet()) {
                sharedPreferencesEditor.putString("workplaceId", homepageViewModel.getWorkplaceId());
                sharedPreferencesEditor.putString("workplace", homepageViewModel.getWorkplaceName() + "\n" + homepageViewModel.getWorkplaceAddress());
            }
        }

        if ( !sharedPreferences.getString("username", "null").equals(homepageViewModel.getCurrentUser().getUsername()) ) {
            String username = homepageViewModel.getCurrentUser().getUsername();
            if (username != null || !username.isEmpty()) {
                sharedPreferencesEditor.putString("username", username);
            }
        }

        sharedPreferencesEditor.apply();
    }


    /***********************************************************************************************
     ** Notification
     **********************************************************************************************/
    // Schedules the Alarm for Notifications
    private void initLunchNotification(){
        Log.d(TAG, "initLunchNotification");
        Calendar dueDate = new TimePreference(this).getPersistedTime();
        scheduleNotification(getApplicationContext(),dueDate);

    }

    public void scheduleNotification(Context context, @Nullable Calendar dueDate) {
        homepageViewModel.scheduleNotificationJob(context,dueDate);
    }

    /***********************************************************************************************
     ** User data - Workplace - Username
     **********************************************************************************************/

    // Initializes the workplaceID
    private void initUserWorkplaceId() {
        Log.d(TAG, "initUserWorkplaceId");
        if (!homepageViewModel.isWorkplaceIdSet()) {
            launchWorkplacePickerDialog(null);
        }
    }

    // Calls the WorkplacePickerDialog and sends the selected result <Autocomplete> to the arg Callback
    public void launchWorkplacePickerDialog(@Nullable Callback<String> innerCallback) {

        Log.d(TAG, "launchWorkplacePickerDialog");

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
                                    Log.d(TAG, "launchWorkplacePickerDialog - onTextChanged- getAutocompletes - onSuccess");
                                    workplaceDialog.updateWorkplaceDialogList(results);
                                }

                                @Override
                                public void onFailure() {
                                    Log.d(TAG, "launchWorkplacePickerDialog - onTextChanged- getAutocompletes - onFailure");
                                }
                            });
                }
            }

            @Override
            public void onFailure() {
                Log.d(TAG, "launchWorkplacePickerDialog - onTextChanged - onFailure");
            }
        };

        // The WorkplacePickerDialog Callback to handle onItemClick action :
        // updates currentUser' workplaceId  - updateCurrentUserData() - updates "workplace" and "workplaceId" sharedPreferences - dismisses the dialog - launches a informative an toast
        Callback<Autocomplete> onItemSelected = new Callback<Autocomplete>() {
            @Override
            public void onSuccess(Autocomplete result) {
                // Updates currentUser workplaceId value
                Log.d(TAG, "launchWorkplacePickerDialog - onItemSelected - onSuccess");
                homepageViewModel.getCurrentUser().setWorkplace(result);

                // Updates sharedPreferences workplaceId, workplaceTitle & workplaceDetail values
                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(HomepageActivity.this);
                final SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                sharedPreferencesEditor.putString("workplaceId", homepageViewModel.getWorkplaceId());
                sharedPreferencesEditor.putString("workplace", homepageViewModel.getWorkplaceName() + "\n" + homepageViewModel.getWorkplaceAddress());
                sharedPreferencesEditor.apply();

                if (innerCallback != null) {
                    innerCallback.onSuccess(homepageViewModel.getWorkplaceName() + "\n" + homepageViewModel.getWorkplaceAddress());
                }

                homepageViewModel.updateAllCurrentUserData(homepageViewModel.getCurrentUser());

                workplaceDialog.dismiss();
                Toast.makeText(HomepageActivity.this, R.string.workplace_dialog_toast, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure() {
                Log.d(TAG, "launchWorkplacePickerDialog - onItemSelected - onFailure");
            }
        };
        workplaceDialog = new WorkplaceDialogFragment(onTextChanged, onItemSelected);
        workplaceDialog.show(getSupportFragmentManager(), getString(R.string.workplace_dialog_tag));
    }

    // Add to SettingsFragment "Change my username"
    // Displays a PickerDialog to ask for the username
    public void launchUsernamePickerDialog(Callback<String> callback) {
        Log.d(TAG, "launchUsernamePickerDialog");
        final EditText usernameInput = new EditText(this);
        usernameInput.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);

        AlertDialog.Builder usernameDialog = new AlertDialog.Builder(this);
        usernameDialog.setTitle(R.string.usernameDialog_title);
        usernameDialog.setView(usernameInput);
        usernameDialog.setPositiveButton(R.string.usernameDialog_positiveBtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String input = usernameInput.getText().toString();
                if (input.length() > 2) {
                    homepageViewModel.setUsername(input);
                    callback.onSuccess(input);
                    initDrawerNavDataDisplay();
                    dialogInterface.dismiss();
                }
                else
                    Toast.makeText(HomepageActivity.this, R.string.usernameDialog_Toast_invalidInput, Toast.LENGTH_SHORT).show();
            }
        });
        usernameDialog.setNegativeButton(R.string.usernameDialog_negativeBtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final Resources resources = binding.getRoot().getResources();
                String contentText = resources.getString(R.string.usernameDialog_snackbar, homepageViewModel.getCurrentUser().getUsername());
                Snackbar snackbar = Snackbar.make(binding.getRoot(), contentText, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.usernameDialog_snackbar_actionBtn, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchUsernamePickerDialog(callback);
                    }
                });
                snackbar.show();
                dialogInterface.dismiss();
            }
        });
        usernameDialog.show();

    }

    public String getSearchRadius() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(HomepageActivity.this);
        int val = sharedPreferences.getInt("search_radius", 2);
        Log.d(TAG, "getSearchRadius - String.valueOf(val*1000) = " + val);
        return String.valueOf(val*1000);
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
     ** Callbacks for fragments
     **********************************************************************************************/

    public void bottomNavVisibility(int visibility) {
        bottomNav.setVisibility(visibility);
    }
}