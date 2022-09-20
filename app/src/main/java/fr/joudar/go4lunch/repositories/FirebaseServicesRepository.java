package fr.joudar.go4lunch.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.services.FirebaseServicesProvider;
import fr.joudar.go4lunch.domain.utils.Callback;

//@Singleton  TODO : use as singleton ?
public class FirebaseServicesRepository {

    private final FirebaseServicesProvider firebaseServicesProvider;

    @Inject
    public FirebaseServicesRepository(FirebaseServicesProvider firebaseServicesProvider) {
        this.firebaseServicesProvider = firebaseServicesProvider;
    }

    public User getCurrentUser() {
        return firebaseServicesProvider.getCurrentUser();
    }

    public MutableLiveData<User> getLiveCurrentUser() {
        return firebaseServicesProvider.getLiveCurrentUser();
    }


    public void getColleagues(Callback<User[]> callback) {
        firebaseServicesProvider.getColleagues(
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

    public void getColleaguesByRestaurant(String restaurantId, Callback<User[]> callback) {
        firebaseServicesProvider.getColleaguesByRestaurant(restaurantId, callback);
    }

    public void updateAllCurrentUserData(User user) {
        firebaseServicesProvider.updateAllCurrentUserData(user);
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

    public void getColleaguesDistributionOverRestaurants(Callback<Map<String, Integer>> callback) {
        firebaseServicesProvider.getColleaguesDistributionOverRestaurants(callback);
    }

    public String getWorkplaceId(){
        return firebaseServicesProvider.getWorkplaceId();
    }

    public String getWorkplaceName() {
        return firebaseServicesProvider.getWorkplaceName();
    }

    public String getWorkplaceAddress() {
        return firebaseServicesProvider.getWorkplaceAddress();
    }

    public void setUsername(String username) {
        firebaseServicesProvider.setUsername(username);
    }

    public void setLikedRestaurantsIdList(List<String> likedRestaurants) {
        firebaseServicesProvider.getCurrentUser().setLikedRestaurantsIdList(likedRestaurants);
    }
    public List<String> getLikedRestaurantsIdList() {
        return firebaseServicesProvider.getCurrentUser().getLikedRestaurantsIdList();
    }
}
