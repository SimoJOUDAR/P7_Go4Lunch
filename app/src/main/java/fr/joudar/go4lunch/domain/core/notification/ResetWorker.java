package fr.joudar.go4lunch.domain.core.notification;

import static fr.joudar.go4lunch.domain.services.FirebaseServicesProvider.CHOSEN_RESTAURANT_ADDRESS;
import static fr.joudar.go4lunch.domain.services.FirebaseServicesProvider.CHOSEN_RESTAURANT_ID;
import static fr.joudar.go4lunch.domain.services.FirebaseServicesProvider.CHOSEN_RESTAURANT_NAME;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ResetWorker extends Worker {

    private final String TAG = "ResetWorker";

    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;
    private String userId = null;

    public ResetWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.d(TAG, "Constructor");
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork");
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
            resetChosenRestaurant();
        }

        return Result.success();
    }

    // Resets chosenRestaurant data back to "" in Firestore
    private void resetChosenRestaurant() {
        Log.d(TAG, "resetChosenRestaurant");
        final Map<String, Object> userData = new HashMap<>();
        userData.put(CHOSEN_RESTAURANT_ID, "");
        userData.put(CHOSEN_RESTAURANT_NAME, "");
        userData.put(CHOSEN_RESTAURANT_ADDRESS, "");
        firestore.collection("users").document(userId).update(userData);
    }
}
