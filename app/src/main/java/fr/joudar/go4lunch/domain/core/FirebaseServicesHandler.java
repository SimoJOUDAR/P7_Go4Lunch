package fr.joudar.go4lunch.domain.core;

import android.net.Uri;
import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.services.FirebaseServicesProvider;
import fr.joudar.go4lunch.domain.utils.Callback;

public class FirebaseServicesHandler implements FirebaseServicesProvider {

    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;
    private User currentUser;
    private MutableLiveData<User> liveCurrentUser;

    public FirebaseServicesHandler(FirebaseFirestore firestore, FirebaseAuth firebaseAuth) {
        this.firestore = firestore;
        this.firebaseAuth = firebaseAuth;
        initUser(this.firebaseAuth);
        this.firebaseAuth.addAuthStateListener(this::initUser); // addAuthStateListener is triggered on authentication state change (user signed in, signed out, changed) and then executes the passed arg listener.
    }

    public void initUser(FirebaseAuth firebaseAuth) {
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            final Uri userPhotoUrl = firebaseUser.getPhotoUrl();
            currentUser = new User(
                            firebaseUser.getUid(),
                            firebaseUser.getDisplayName(),
                            firebaseUser.getEmail(),
                            userPhotoUrl != null ? userPhotoUrl.toString() : AVATAR_URL);
            initUserInFirebase();
            liveCurrentUser.postValue(currentUser);
        } else currentUser = null;

        //Todo: ErrorMessage()?

    }

    // TODO: Tentative to solve the Firestore problem :/
    private void initUserInFirebase() {
        if (isCurrentUserNew()) {
            firestore.collection("users")
                    .document(currentUser.getId())
                    .set(currentUser);
            // TODO: Ask user in HomepageActivity to enter his data (WORKPLACE_ID) (LIKED_RESTAURANTS_ID_LIST & CHOSEN_RESTAURANT data are going to be updated alongside app utilisation)
        }
        else {
            initUserData();
        }
    }

    private void initUserData() {
        firestore.collection("users")
                .document(currentUser.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot userDocument) {
                        currentUser.setLikedRestaurantsIdList((List<String>) userDocument.get(LIKED_RESTAURANTS_ID_LIST));
                        currentUser.setWorkplaceId(userDocument.getString(WORKPLACE_ID));
                        currentUser.setChosenRestaurantId(userDocument.getString(CHOSEN_RESTAURANT_ID));
                        currentUser.setChosenRestaurantName(userDocument.getString(CHOSEN_RESTAURANT_NAME));
                    }
                });
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public MutableLiveData<User> getLiveCurrentUser() {
        return liveCurrentUser;
    }

    @Override
    public void getColleagues(Callback<User[]> callback) {
        firestore
                .collection("users")
                .whereEqualTo(WORKPLACE_ID, currentUser.getWorkplaceId())
                .get()
                .addOnSuccessListener(
                        snapshots -> callback.onSuccess(snapshotsToArrayConverter(snapshots.getDocuments())))
                .addOnFailureListener(__ -> callback.onFailure());
    }

    @Override
    public void getColleaguesByRestaurant(String restaurantId, Callback<User[]> callback) {
        firestore
                .collection("users")
                .whereEqualTo(WORKPLACE_ID, currentUser.getWorkplaceId())
                .whereEqualTo(CHOSEN_RESTAURANT_ID, restaurantId)
                .get()
                .addOnSuccessListener(
                        snapshots -> callback.onSuccess(snapshotsToArrayConverter(snapshots.getDocuments())))
                .addOnFailureListener(__ -> callback.onFailure());
    }

    @Override
    public void updateAllCurrentUserData() {
        final Map<String, Object> userData = new HashMap<>();
        userData.put(LIKED_RESTAURANTS_ID_LIST, currentUser.getLikedRestaurantsIdList());
        userData.put(WORKPLACE_ID, currentUser.getWorkplaceId());
        userData.put(CHOSEN_RESTAURANT_ID, currentUser.getChosenRestaurantId());
        userData.put(CHOSEN_RESTAURANT_NAME, currentUser.getChosenRestaurantName());
        firestore.collection("users").document(currentUser.getId()).update(userData);
        liveCurrentUser.postValue(currentUser);
    }

    @Override
    public void resetChosenRestaurant() {
        currentUser.setChosenRestaurantId("");
        currentUser.setChosenRestaurantName("");
        updateCurrentUserData(CHOSEN_RESTAURANT_ID, "");
    }

    @Override
    public void updateCurrentUserData(String key, Object value) {
        final Map<String, Object> userData = new HashMap<>();
        userData.put(key, value);
        if (key.equals(CHOSEN_RESTAURANT_ID)) {
            userData.put(CHOSEN_RESTAURANT_NAME, currentUser.getChosenRestaurantName());
        } else if (key.equals(CHOSEN_RESTAURANT_NAME)) {
            userData.put(CHOSEN_RESTAURANT_ID, currentUser.getChosenRestaurantId());
        }
        firestore.collection("users").document(currentUser.getId()).update(userData);
        liveCurrentUser.postValue(currentUser);
    }

    @Override
    public boolean isCurrentUserNew() {
        final FirebaseUserMetadata userMetadata = firebaseAuth.getCurrentUser().getMetadata();
        return userMetadata.getCreationTimestamp() != userMetadata.getLastSignInTimestamp();
    }

    @Override
    public void logout() {
        firebaseAuth.signOut();
    }


    @Override
    public void deleteCurrentUserAccount(Callback<Boolean> callback) {
        firebaseAuth
                .getCurrentUser()
                .delete()
                .addOnSuccessListener(__ -> callback.onSuccess(true))
                .addOnFailureListener(__ -> callback.onFailure());

        // Or : AuthUI.getInstance().delete(context);   ?
        //TODO: Logout()
    }

    @Override
    public void getColleaguesDistributionOverRestaurants(Callback<Map<String, Integer>> callback) {
        Log.d("FirebaseServicesHandler", "getColleaguesDistributionOverRestaurants");
        firestore
                .collection("users")
                .whereEqualTo(WORKPLACE_ID, currentUser.getWorkplaceId())
                .get()
                .addOnSuccessListener(
                        usersSnapshot -> {
                            final Map<String, Integer> colleaguesDistributionOverRestaurants = new HashMap<>();
                            String restaurantId;
                            Integer colleaguesCount;
                            for (DocumentSnapshot snapshot : usersSnapshot.getDocuments()) {
                                restaurantId = snapshot.getString(CHOSEN_RESTAURANT_ID);
                                colleaguesCount = colleaguesDistributionOverRestaurants.get(restaurantId);
                                if (colleaguesCount == null) {
                                    colleaguesDistributionOverRestaurants.put(restaurantId, 1);
                                } else
                                    colleaguesDistributionOverRestaurants.put(restaurantId, ++colleaguesCount);
                            }
                            callback.onSuccess(colleaguesDistributionOverRestaurants);
                        });
    }

    @Override
    public String getWorkplaceId() {
        return currentUser.getWorkplaceId();
    }

    private User[] snapshotsToArrayConverter(List<DocumentSnapshot> usersDocuments) {
        final List<User> userList = new ArrayList<>();
        String id = getCurrentUser().getId();
        for (DocumentSnapshot userDoc : usersDocuments) {
            if (userDoc.getId() != id) {
                userList.add(
                        new User(
                                userDoc.getId(),
                                userDoc.getString("name"),
                                "",
                                userDoc.getString("photoUrl"),
                                (List<String>) userDoc.get(LIKED_RESTAURANTS_ID_LIST),
                                userDoc.getString(WORKPLACE_ID),
                                userDoc.getString(CHOSEN_RESTAURANT_ID),
                                userDoc.getString(CHOSEN_RESTAURANT_NAME)));
            }
        }

        return userList.toArray(new User[0]); //We added (new User[0]) as arg to avoid NullPointer exceptions
    }
}
