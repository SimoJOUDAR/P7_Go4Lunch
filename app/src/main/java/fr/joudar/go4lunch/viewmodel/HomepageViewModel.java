package fr.joudar.go4lunch.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.repositories.FirebaseServicesRepository;

@HiltViewModel
public class HomepageViewModel extends ViewModel {

    private FirebaseServicesRepository firebaseServicesRepository;
    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    public static int searchRadius = 0;

    @Inject
    public HomepageViewModel(FirebaseServicesRepository firebaseServicesRepository) {
        this.firebaseServicesRepository = firebaseServicesRepository;
        currentUser = this.firebaseServicesRepository.getCurrentUser();
    }

    public MutableLiveData<User> getCurrentUser() {
        return currentUser;
    }

    /***********************************************************************************************
     ** Firebase init
     **********************************************************************************************/

    public void initFirebaseAuth(Runnable runnable){
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null){
                    runnable.run();
                }
                else {
                }
            }
        };
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    //Logout the user
    public void logout() {
        firebaseServicesRepository.logout();

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

}
