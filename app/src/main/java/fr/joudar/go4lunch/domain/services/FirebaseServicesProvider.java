package fr.joudar.go4lunch.domain.services;

import androidx.lifecycle.MutableLiveData;
import java.util.Map;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.utils.Callback;

public interface FirebaseServicesProvider {

    String USERNAME = "username";
    String AVATAR_URL = "https://raw.githubusercontent.com/SimoJOUDAR/default_avatar/main/user_avatar_v2.png";
    String WORKPLACE_ID = "workplaceId";
    String WORKPLACE_NAME = "workplaceName";
    String WORKPLACE_ADDRESS = "workplaceAddress";
    String CHOSEN_RESTAURANT_ID = "chosenRestaurantId";
    String CHOSEN_RESTAURANT_NAME = "chosenRestaurantName";
    String CHOSEN_RESTAURANT_ADDRESS = "chosenRestaurantAddress";
    String LIKED_RESTAURANTS_ID_LIST = "likedRestaurantsIdList";

    // The current location of the connected user
    User getCurrentUser();

    MutableLiveData<User> getLiveCurrentUser();

    // Fetches a list of users working in the same place as the currently connected user, and passes it to the arg callback
    void getColleagues(Callback<User[]> callback);

    // Fetches a list of users working in the same place, with the same "chosen restaurant" as the currently connected user, and passes it to the arg callback
    void getColleaguesByRestaurant(String restaurantId, Callback<User[]> callback);

    // Updates all of the current user's data in the firestore
    void updateAllCurrentUserData(User user);

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

    String getWorkplaceName();

    String getWorkplaceAddress();

    void setUsername(String username);
}
