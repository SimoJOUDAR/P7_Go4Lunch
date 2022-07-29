package fr.joudar.go4lunch.repositories;

import android.content.Context;

import com.annimon.stream.Stream;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;
import javax.inject.Inject;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.services.FirebaseServicesProvider;
import fr.joudar.go4lunch.domain.utils.Callback;

public class FirebaseServicesRepository {
    private final FirebaseServicesProvider firebaseServicesProvider;

    @Inject
    public FirebaseServicesRepository(FirebaseServicesProvider firebaseServicesProvider) {
        this.firebaseServicesProvider = firebaseServicesProvider;
    }

    public User getCurrentUser() {
        return firebaseServicesProvider.getCurrentUser();
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
