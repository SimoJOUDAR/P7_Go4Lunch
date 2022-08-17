package fr.joudar.go4lunch.viewmodel;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import fr.joudar.go4lunch.domain.models.Autocomplete;
import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.utils.Callback;
import fr.joudar.go4lunch.repositories.AutocompleteRepository;
import fr.joudar.go4lunch.repositories.FirebaseServicesRepository;
import fr.joudar.go4lunch.repositories.PlaceDetailsRepository;

@HiltViewModel
public class HomepageViewModel extends ViewModel {


    //private User currentUser;
//    private final Observer<User> observer = new Observer<User>() {
//        @Override
//        public void onChanged(User user) {
//            currentUser = user;
//        }
//    };

    private final FirebaseServicesRepository firebaseServicesRepository;
    private final AutocompleteRepository autocompleteRepository;
    private final PlaceDetailsRepository placeDetailsRepository;

    //TODO: To define search radius in SettingsFragment and store it in SharedPreferences

    @Inject
    public HomepageViewModel(FirebaseServicesRepository firebaseServicesRepository,
                             AutocompleteRepository autocompleteRepository,
                             PlaceDetailsRepository placeDetailsRepository) {
        this.firebaseServicesRepository = firebaseServicesRepository;
        this.autocompleteRepository = autocompleteRepository;
        this.placeDetailsRepository = placeDetailsRepository;
        //currentUser = this.firebaseServicesRepository.getCurrentUser();
    }

    /***********************************************************************************************
     ** Firebase
     **********************************************************************************************/

    public void initListener(FirebaseAuth.AuthStateListener authStateListener){
        // getLiveCurrentUser().observeForever(observer);
//        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//        firebaseAuth.addAuthStateListener(authStateListener);
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    public User getCurrentUser() {
        return firebaseServicesRepository.getCurrentUser();
    }
    public MutableLiveData<User> getLiveCurrentUser() {
        return firebaseServicesRepository.getLiveCurrentUser();
    }

    public void updateAllCurrentUserData() {
        firebaseServicesRepository.updateAllCurrentUserData();
    }

    public void updateCurrentUserData(String dataType, Object data){
        firebaseServicesRepository.updateCurrentUserData(dataType, data);
    }

    public boolean isCurrentUserNew() {
        return firebaseServicesRepository.isCurrentUserNew();
    }

    public void logout() {
        firebaseServicesRepository.logout();
    }

    public void deleteCurrentUserAccount(Callback<Boolean> callback){
        firebaseServicesRepository.deleteCurrentUserAccount(callback);
    }

    public String getWorkplaceId(){
        return firebaseServicesRepository.getWorkplaceId();
    }



    /***********************************************************************************************
     ** Autocomplete
     **********************************************************************************************/

    //Fetches the Autocomplete results and passes them to the callback.
    // The boolean isFiltered is "true" for Restaurants autocomplete, "false" for Workplace autocomplete.
    public void getAutocompletes(String input, Location location, String searchRadius, boolean isFiltered, Callback<Autocomplete[]> callback) {
        autocompleteRepository.getAutocompletes(input, location, searchRadius, isFiltered, new Callback<Autocomplete[]>() {
            @Override
            public void onSuccess(Autocomplete[] results) {
                callback.onSuccess(results);
            }

            @Override
            public void onFailure() {
                callback.onFailure();
            }
        });
    }

    /***********************************************************************************************
     ** PlaceDetails
     **********************************************************************************************/

    public void getPlaceDetails(String placeId, Callback<Place> callback) {
        placeDetailsRepository.getPlaceDetails(placeId, callback);
    }



//    @Override
//    protected void onCleared() {
//        getLiveCurrentUser().removeObserver(observer);
//        super.onCleared();
//    }
}
