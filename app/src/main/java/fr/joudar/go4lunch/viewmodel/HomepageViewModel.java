package fr.joudar.go4lunch.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.repositories.FirebaseServicesRepository;

@HiltViewModel
public class HomepageViewModel extends ViewModel {

    private final FirebaseServicesRepository firebaseServicesRepository;
    private User currentUser;
    private final Observer<User> observer = new Observer<User>() {
        @Override
        public void onChanged(User user) {
            currentUser = user;
        }
    };

    //TODO: To define search radius in SettingsFragment and store it in SharedPreferences
    public static int searchRadius = 0;

    @Inject
    public HomepageViewModel(FirebaseServicesRepository firebaseServicesRepository) {
        this.firebaseServicesRepository = firebaseServicesRepository;
        currentUser = this.firebaseServicesRepository.getCurrentUser();
    }

    /***********************************************************************************************
     ** Firebase
     **********************************************************************************************/

    public void initListener(Runnable runnable){
        getLiveCurrentUser().observeForever(observer);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        //Todo: ErrorMessage()?
        FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    runnable.run();
                } else {
                    //Todo: ErrorMessage()?
                }
            }
        };
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    //Logout the user
    public void logout() {
        firebaseServicesRepository.logout();
    }

    public User getCurrentUser() {
        return currentUser;
    }
    public MutableLiveData<User> getLiveCurrentUser() {
        return firebaseServicesRepository.getLiveCurrentUser();
    }

    public String getWorkplaceId(){
        return firebaseServicesRepository.getWorkplaceId();
    }

    /***********************************************************************************************
     ** Utils
     **********************************************************************************************/
    public static String getSystemLanguage() {
        String lang;
        if (Locale.getDefault().getLanguage().equals("fr"))
            lang = "fr";
        else
            lang = "en";
        return lang;
    }

    public static String getRadius() {
        int defaultRadius = 2000;
        if (searchRadius >= defaultRadius)
            return String.valueOf(searchRadius);
        else
            return String.valueOf(defaultRadius);
    }

    @Override
    protected void onCleared() {
        getLiveCurrentUser().removeObserver(observer);
        super.onCleared();
    }
}
