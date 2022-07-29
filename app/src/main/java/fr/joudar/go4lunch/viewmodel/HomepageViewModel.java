package fr.joudar.go4lunch.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

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

}
