package fr.joudar.go4lunch.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.databinding.ActivityHomepageBinding;
import fr.joudar.go4lunch.domain.core.LocationPermissionHandler;
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

    // Domain
    @Inject public LocationPermissionHandler mLocationPermissionHandler;
    private HomepageViewModel homepageViewModel;


    /***********************************************************************************************
     ** The onCreate method
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomepageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InitNavigation();
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

    // To connect our option menu to the Navigation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.toolbar_layout, menu);
        mapFragmentDisplayOptions();
        final SearchView searchField = (SearchView) menu.findItem(R.id.search).getActionView();
//        searchField.setOnQueryTextFocusChangeListener(this::onSearchFieldFocusChanged);
//        searchField.setOnQueryTextListener(getQueryListener());
//        searchField.setQueryHint(getString(R.string.search_restaurants_hint));
        return super.onCreateOptionsMenu(menu);
    }

    // Sets up option menu items action (YOUR LUNCH, SETTINGS, LOGOUT).
    private boolean onDrawerNavMenuItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.your_lunch:
                Toast.makeText(this, "Your lunch", Toast.LENGTH_SHORT).show();
                //showCurrentUserChosenRestaurant();
                return true;
            case R.id.logout:
                Toast.makeText(this, "You've signed out", Toast.LENGTH_SHORT).show();
                //logout();
                homepageViewModel.logout();
                return true;
            case R.id.settingsFragment:
                Navigation.findNavController(binding.navHostFragmentContainer).navigate(R.id.settingsFragment);
                binding.getRoot().closeDrawer(binding.drawerNav, false);
                return true;
            case R.id.restaurantDetailsFragment: //TODO : This last case is just for testing... To be removed along with the xml menu item
//                Navigation.findNavController(binding.navHostFragmentContainer).navigate(R.id.restaurantDetailsFragment);
//                binding.getRoot().closeDrawer(binding.drawerNav, false);
                return true;
        }
        return true;
    }

    /***********************************************************************************************
     ** ViewModel
     **********************************************************************************************/

    private void initViewModel() {
        Log.d("HomepageActivity", "initViewModel _Start_");
        homepageViewModel = new ViewModelProvider(this).get(HomepageViewModel.class);
        Log.d("HomepageActivity", "initViewModel _homepageViewModel_Finish_");
        homepageViewModel.initListener(this::onLogout);
        Log.d("HomepageActivity", "initViewModel _initFirebaseAuth_Finish_");
    }

    //If logged out, it takes us back to AuthenticationActivity
    private void onLogout(){
        startActivity(new Intent(this, AuthenticationActivity.class));
        finish();
        Log.d("HomepageActivity", "onLogout");
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

    public void mapFragmentDisplayOptions(){
        actionbarVisibility(true);
        menuVisibility(false);
        bottomNavigationVisibility(true);
    }
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
    public void restaurantDetailsFragmentDisplayOptions(){
        actionbarVisibility(false);
        bottomNavigationVisibility(false);
    }
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