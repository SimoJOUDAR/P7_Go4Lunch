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

    User getCurrentUser();
    void getColleagues(String workplaceId, Callback<User[]> callback);
    void getColleaguesByRestaurant(String restaurantId, String usersWorkplaceId, Callback<User[]> callback);
    void uploadCurrentUserData();  //TODO: When used ?
    void resetChosenRestaurant();
    void updateCurrentUserData(String dataType, Object data);
    boolean isCurrentUserNew();
    void logout();
    void deleteCurrentUserAccount(Callback<Boolean> callback);
    void getColleaguesDistributionOverRestaurants(String workplaceId, Callback<Map<String, Integer>> callback);

}
