package fr.joudar.go4lunch.domain.core;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.auth.UserProfileChangeRequest;
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
    private MutableLiveData<User> liveCurrentUser = new MutableLiveData<>();

    public FirebaseServicesHandler(FirebaseFirestore firestore, FirebaseAuth firebaseAuth) {
        Log.d("FirebaseServicesHandler", "Constructor");
        liveCurrentUser.postValue(null);
        this.firestore = firestore;
        this.firebaseAuth = firebaseAuth;
        initUser(this.firebaseAuth);
        this.firebaseAuth.addAuthStateListener(this::initUser); // addAuthStateListener is triggered on authentication state change (user signed in, signed out, changed) and then executes the passed arg listener.
    }

    // Creates currentUser based on Firebase
    public void initUser(FirebaseAuth firebaseAuth) {
        Log.d("FirebaseServicesHandler", "initUser");
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            final Uri userPhotoUrl = firebaseUser.getPhotoUrl();
            currentUser = new User(
                            firebaseUser.getUid(),
                            firebaseUser.getDisplayName(),
                            firebaseUser.getEmail(),
                            userPhotoUrl != null ? userPhotoUrl.toString() : AVATAR_URL);
            initUserData();
        } else currentUser = null;

        //Todo: ErrorMessage()?

    }

    private void initUserData() {
        Log.d("FirebaseServicesHandler", "initFirebaseData");
        firestore.collection("users")
                .document(currentUser.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                initUserDataFromFirestore();

                            }
                            else {
                                initUserDataInFirestore();
                            }
                        }
                        else {
                            Log.d("FirebaseServicesHandler", "Task failed with: ", task.getException());
                        }
                    }
                });
    }

//    // Not optimal because of the lack of accuracy of isCurrentUserNew().
//    private void initUserDataInFirestore() {
//        Log.d("FirebaseServicesHandler", "initUserDataInFirestore");
//        if (isCurrentUserNew()) {
//            Log.d("FirebaseServicesHandler", "isCurrentUserNew : true");
//            firestore.collection("users")
//                    .document(currentUser.getId())
//                    .set(currentUser);
//            liveCurrentUser.postValue(currentUser);
//        }
//        else {
//            initUserDataFromFirestore();
//        }
//    }

    // Updates currentUser from Firestore.
    private void initUserDataFromFirestore() {
        Log.d("FirebaseServicesHandler", "initUserDataFromFirestore");
        firestore.collection("users")
                .document(currentUser.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot userDocument) {
                        currentUser.setWorkplaceId(userDocument.getString(WORKPLACE_ID));
                        currentUser.setWorkplaceName(userDocument.getString(WORKPLACE_NAME));
                        currentUser.setWorkplaceAddress(userDocument.getString(WORKPLACE_ADDRESS));
                        currentUser.setChosenRestaurantId(userDocument.getString(CHOSEN_RESTAURANT_ID));
                        currentUser.setChosenRestaurantName(userDocument.getString(CHOSEN_RESTAURANT_NAME));
                        currentUser.setChosenRestaurantAddress(userDocument.getString(CHOSEN_RESTAURANT_ADDRESS));
                        List<String> likedRestaurant = (List<String>) userDocument.get(LIKED_RESTAURANTS_ID_LIST);
                        if (likedRestaurant == null) likedRestaurant = new ArrayList<>(); // To avoid null ArrayList
                        currentUser.setLikedRestaurantsIdList(likedRestaurant);
                        isUsernameNull(userDocument.getString(USERNAME));
                        liveCurrentUser.postValue(currentUser);
                    }
                });
    }

    // Because sometimes the user from FirebaseAuth isn't initiated successfully, therefore pushes the value "username = null" to firestore. The method below is to avoid is bug.
    private void isUsernameNull(String username) {
        Log.d("FirebaseServicesHandler", "isUsernameNull");
        if (username == null || username.isEmpty()) {
            Log.d("FirebaseServicesHandler", "isUsernameNull - true");
            updateCurrentUserData(USERNAME, currentUser.getUsername());
        }
    }

    // Creates new user in Firestore.
    private void initUserDataInFirestore() {
        Log.d("FirebaseServicesHandler", "initUserDataInFirestore");
        firestore.collection("users")
                .document(currentUser.getId())
                .set(currentUser);
        liveCurrentUser.postValue(currentUser);
    }

    @Override
    public User getCurrentUser() {
        Log.d("FirebaseServicesHandler", "getCurrentUser");
        return currentUser;
    }

    @Override
    public MutableLiveData<User> getLiveCurrentUser() {
        Log.d("FirebaseServicesHandler", "getLiveCurrentUser");
        return liveCurrentUser;
    }

    @Override
    public void getColleagues(Callback<User[]> callback) {
        Log.d("FirebaseServicesHandler", "getColleagues");
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
        Log.d("FirebaseServicesHandler", "getColleaguesByRestaurant");
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
    public void updateAllCurrentUserData(User user) {
        Log.d("FirebaseServicesHandler", "updateAllCurrentUserData");
//        final Map<String, Object> userData = new HashMap<>();
//        userData.put(WORKPLACE_ID, currentUser.getWorkplaceId());
//        userData.put(WORKPLACE_NAME, currentUser.getWorkplaceName());
//        userData.put(WORKPLACE_ADDRESS, currentUser.getWorkplaceAddress());
//        userData.put(CHOSEN_RESTAURANT_ID, currentUser.getChosenRestaurantId());
//        userData.put(CHOSEN_RESTAURANT_NAME, currentUser.getChosenRestaurantName());
//        userData.put(LIKED_RESTAURANTS_ID_LIST, currentUser.getLikedRestaurantsIdList());
//        firestore.collection("users").document(currentUser.getId()).update(userData);
        firestore.collection("users").document(currentUser.getId()).set(user);
        currentUser = user;
        liveCurrentUser.postValue(user);
    }

    @Override
    public void resetChosenRestaurant() {
        Log.d("FirebaseServicesHandler", "resetChosenRestaurant");
        currentUser.setChosenRestaurantId("");
        currentUser.setChosenRestaurantName("");
        currentUser.setChosenRestaurantAddress("");

        updateCurrentUserData(CHOSEN_RESTAURANT_ID, "");
    }

    @Override
    public void updateCurrentUserData(String key, Object value) {
        Log.d("FirebaseServicesHandler", "updateCurrentUserData");
        final Map<String, Object> userData = new HashMap<>();
        userData.put(key, value);
        if (key.equals(CHOSEN_RESTAURANT_ID)) {
            userData.put(CHOSEN_RESTAURANT_NAME, currentUser.getChosenRestaurantName());
            userData.put(CHOSEN_RESTAURANT_ADDRESS, currentUser.getChosenRestaurantAddress());
        }
        firestore.collection("users").document(currentUser.getId()).update(userData);
        liveCurrentUser.postValue(currentUser);
    }

    @Override
    public boolean isCurrentUserNew() {
        Log.d("FirebaseServicesHandler", "isCurrentUserNew");
        final FirebaseUserMetadata userMetadata = firebaseAuth.getCurrentUser().getMetadata();
        // getLastSignInTimestamp() is only accurate up to a granularity of 2 minutes for consecutive sign-in attempts, which might create confusion with getCreationTimestamp().
        return userMetadata.getLastSignInTimestamp() == userMetadata.getCreationTimestamp();
    }

    @Override
    public void logout() {
        Log.d("FirebaseServicesHandler", "logout");
        firebaseAuth.signOut();
    }


    @Override
    public void deleteCurrentUserAccount(Callback<Boolean> callback) {
        Log.d("FirebaseServicesHandler", "deleteCurrentUserAccount");
        deleteUserFromFirestore(callback);
    }

    private void deleteUserFromFirestore(Callback<Boolean> callback){
        Log.d("FirebaseServicesHandler", "deleteUserFromFirestore");
        firestore.collection("users").document(currentUser.getId()).delete()
                .addOnSuccessListener(__ -> deleteUserFromFirebase(callback))
                .addOnFailureListener(__ -> callback.onFailure());
    }

    private void deleteUserFromFirebase(Callback<Boolean> callback) {
        Log.d("FirebaseServicesHandler", "deleteUserFromFirebase");
        firebaseAuth
                .getCurrentUser()
                .delete()
                .addOnSuccessListener(__ -> callback.onSuccess(true))
                .addOnFailureListener(__ -> callback.onFailure());
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
        Log.d("FirebaseServicesHandler", "getWorkplaceId");
        return currentUser.getWorkplaceId();
    }

    @Override
    public String getWorkplaceName() {
        return currentUser.getWorkplaceName();
    }

    @Override
    public String getWorkplaceAddress() {
        return currentUser.getWorkplaceAddress();
    }

    @Override
    public void setUsername(String username) {
//        currentUser.setUsername(username);
        UserProfileChangeRequest usernameUpdater = new UserProfileChangeRequest.Builder()
                .setDisplayName(username).build();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        firebaseUser.updateProfile(usernameUpdater).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateCurrentUserData(USERNAME, username);
                }
            }
        });
    }

    private User[] snapshotsToArrayConverter(List<DocumentSnapshot> usersDocuments) {
        Log.d("FirebaseServicesHandler", "snapshotsToArrayConverter");
        final List<User> userList = new ArrayList<>();
        String id = getCurrentUser().getId();
        for (DocumentSnapshot userDoc : usersDocuments) {
            if (!userDoc.getId().equals(id)) {
                userList.add(
                        new User(
                                userDoc.getId(),
                                userDoc.getString(USERNAME),
                                "",
                                userDoc.getString("avatarUrl"),
                                userDoc.getString(WORKPLACE_ID),
                                "", // userDoc.getString(WORKPLACE_NAME),
                                "", // userDoc.getString(WORKPLACE_ADDRESS),
                                userDoc.getString(CHOSEN_RESTAURANT_ID),
                                "", // userDoc.getString(CHOSEN_RESTAURANT_NAME),
                                "", // userDoc.getString(CHOSEN_RESTAURANT_ADDRESS),
                                null //(List<String>) userDoc.get(LIKED_RESTAURANTS_ID_LIST)
                        ));
            }
        }

        return userList.toArray(new User[0]); //We added (new User[0]) as arg to avoid NullPointer exceptions
    }
}
