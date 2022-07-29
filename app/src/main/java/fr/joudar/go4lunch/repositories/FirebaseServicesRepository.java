package fr.joudar.go4lunch.repositories;

import androidx.lifecycle.MutableLiveData;

import com.annimon.stream.Stream;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.services.FirebaseServicesProvider;
import fr.joudar.go4lunch.domain.utils.Callback;

@Singleton
public class FirebaseServicesRepository {
    private final FirebaseServicesProvider firebaseServicesProvider;
    private MutableLiveData<User> currentUser = new MutableLiveData<>();

    @Inject
    public FirebaseServicesRepository(FirebaseServicesProvider firebaseServicesProvider) {
        this.firebaseServicesProvider = firebaseServicesProvider;
        currentUser.postValue(this.firebaseServicesProvider.getCurrentUser());
    }

    public MutableLiveData<User> getCurrentUser() {
        return currentUser;
    }

    public void getColleagues(Callback<User[]> callback) {
        firebaseServicesProvider.getColleagues(
                firebaseServicesProvider.getCurrentUser().getWorkplaceId(),
                new Callback<User[]>() {
                    @Override
                    public void onSuccess(User[] users) {
                        // The method strips the current user from the list
                        final Stream<User> colleagues = Stream.of(users).filterNot((user) -> user.getId().equals(firebaseServicesProvider.getCurrentUser().getId()));
                        callback.onSuccess(colleagues.toArray(User[]::new));
                    }

                    @Override
                    public void onFailure() {
                        callback.onFailure();
                    }
                });
    }

    public void getColleaguesByRestaurant(String restaurantId, String usersWorkplaceId, Callback<User[]> callback) {
        firebaseServicesProvider.getColleaguesByRestaurant(restaurantId, usersWorkplaceId, callback);
    }

    public void resetChosenRestaurant(){
        firebaseServicesProvider.resetChosenRestaurant();
    }

    public void updateCurrentUserData(String dataType, Object data){
        firebaseServicesProvider.updateCurrentUserData(dataType, data);
    }

    public boolean isCurrentUserNew(){
        return firebaseServicesProvider.isCurrentUserNew();
    }

    public void logout() {
        firebaseServicesProvider.logout();
    }


    public void deleteCurrentUserAccount(Callback<Boolean> callback){
        firebaseServicesProvider.deleteCurrentUserAccount(callback);
    }

    public void getColleaguesDistributionOverRestaurants(String workplaceId, Callback<Map<String, Integer>> callback) {
        firebaseServicesProvider.getColleaguesDistributionOverRestaurants(workplaceId, callback);
    }
}
