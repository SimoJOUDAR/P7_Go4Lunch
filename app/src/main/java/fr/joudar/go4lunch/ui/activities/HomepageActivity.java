package fr.joudar.go4lunch.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;

import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.databinding.ActivityHomepageBinding;

public class HomepageActivity extends AppCompatActivity {

    ActivityHomepageBinding binding;
    NavHostFragment navHostFragment;
    NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomepageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        InitNavigation();

    }

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
        getMenuInflater().inflate(R.menu.toolbar_layout, menu);
        menu.findItem(R.id.sort).setVisible(false);
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
                Toast.makeText(this, "You signed out", Toast.LENGTH_SHORT).show();
                signOut();
                return true;
            case R.id.settingsFragment:
                Navigation.findNavController(binding.navHostFragmentContainer).navigate(R.id.settingsFragment);
                binding.getRoot().closeDrawer(binding.drawerNav, false);
        }
        return true;
    }


    // Logout the user and takes him to the login activity
    private void signOut() {
        AuthUI.getInstance().signOut(this).addOnSuccessListener(__ -> {
                            startActivity(new Intent(this, AuthenticationActivity.class));
                            finish();
                        });
    }
}