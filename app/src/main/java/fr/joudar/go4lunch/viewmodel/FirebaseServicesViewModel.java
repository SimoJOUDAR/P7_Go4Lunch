package fr.joudar.go4lunch.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.repositories.FirebaseServicesRepository;

//TODO: Not wise - Better to implement Repo directly and use ActivityViewModel instead
public class FirebaseServicesViewModel extends ViewModel {

    FirebaseServicesRepository firebaseServicesRepository;
    private final MutableLiveData<User> currentUser;

    public FirebaseServicesViewModel(FirebaseServicesRepository firebaseServicesRepository) {
        this.firebaseServicesRepository = firebaseServicesRepository;
        this.currentUser = new MutableLiveData<>(this.firebaseServicesRepository.getCurrentUser());
    }
}
