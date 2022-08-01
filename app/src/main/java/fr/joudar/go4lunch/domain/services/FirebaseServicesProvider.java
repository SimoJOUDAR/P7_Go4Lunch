package fr.joudar.go4lunch.domain.services;

import android.content.Context;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.utils.Callback;

public interface FirebaseServicesProvider {

    String CHOSEN_RESTAURANT_ID = "chosenRestaurantId";
    String CHOSEN_RESTAURANT_NAME = "chosenRestaurantName";
    String WORKPLACE_ID = "workplaceId";
    String LIKED_RESTAURANTS_ID_LIST = "likedRestaurantsIdList";
    String AVATAR_URL = "https://raw.githubusercontent.com/SimoJOUDAR/default_avatar/main/user_avatar_v2.png";

    // The current location of the connected user
    User getCurrentUser();

    // Fetches a list of users working in the same place as the currently connected user, and passes it to the arg callback
    void getColleagues(Callback<User[]> callback);

    // Fetches a list of users working in the same place, with the same "chosen restaurant" as the currently connected user, and passes it to the arg callback
    void getColleaguesByRestaurant(String restaurantId, Callback<User[]> callback);

    // Updates all of the current user's data in the firestore
    void uploadCurrentUserData();

    // Resets all related data to the "chosen restaurant" to empty fields (local & firestore updates)
    void resetChosenRestaurant();

    // Updates the precised current user's data in the firestore
    void updateCurrentUserData(String dataType, Object data);

    // Returns true if the user is new
    boolean isCurrentUserNew();

    // Logout the user from FirebaseAuth
    void logout();

    // Deletes current user's account
    void deleteCurrentUserAccount(Callback<Boolean> callback);

    // Creates a HashMap of "key: restaurant's id","value: number of colleagues joining the place" and passes it to the arg callback
    void getColleaguesDistributionOverRestaurants(Callback<Map<String, Integer>> callback);

    // Returns the Workplace id
    String getWorkplaceId();
}
