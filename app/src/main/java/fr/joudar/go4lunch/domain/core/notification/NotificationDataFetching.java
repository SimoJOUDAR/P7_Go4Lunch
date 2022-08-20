package fr.joudar.go4lunch.domain.core.notification;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.utils.Callback;
import fr.joudar.go4lunch.repositories.FirebaseServicesRepository;
import fr.joudar.go4lunch.repositories.PlaceDetailsRepository;

@HiltWorker
public class NotificationDataFetching extends Worker {

    FirebaseServicesRepository firebaseServicesRepository;
    PlaceDetailsRepository placeDetailsRepository;

    User currentUser;

    String username;
    String chosenRestaurantId = null;
    String chosenRestaurantName;
    String chosenRestaurantAddress;
    String joiningColleagues;

    @AssistedInject
    public NotificationDataFetching(
            @Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters workerParams,
            @Assisted FirebaseServicesRepository firebaseServicesRepository,
            @Assisted PlaceDetailsRepository placeDetailsRepository) {
        super(context, workerParams);
        this.firebaseServicesRepository = firebaseServicesRepository;
        this.placeDetailsRepository = placeDetailsRepository;
    }

    @NonNull
    @Override
    public Result doWork() {

        currentUser = firebaseServicesRepository.getCurrentUser();
        username = currentUser.getUsername();
        chosenRestaurantId = currentUser.getChosenRestaurantId();

        if (!chosenRestaurantId.isEmpty()) {
            if (!currentUser.getWorkplaceId().isEmpty()) {
                firebaseServicesRepository.getColleaguesByRestaurant(chosenRestaurantId, colleaguesByRestaurantCallback);
            }
            placeDetailsRepository.getPlaceDetails(chosenRestaurantId, placeDetailCallback);
        }

        Data output = new Data.Builder().
                putString("username", username).
                putString("chosenRestaurantId", chosenRestaurantId).
                putString("chosenRestaurantName", chosenRestaurantName).
                putString("chosenRestaurantAddress", chosenRestaurantAddress).
                putString("joiningColleagues", joiningColleagues).
                build();

        chainNextWork(getApplicationContext(), output);

        return Result.success();
    }

    Callback<User[]> colleaguesByRestaurantCallback = new Callback<User[]>() {
        @Override
        public void onSuccess(User[] colleagues) {
            Log.d("LunchNotificationWorker", "Colleagues fetched : " + Arrays.toString(colleagues));
            UsersToStringConverter(colleagues);
        }

        @Override
        public void onFailure() {
            UsersToStringConverter(null);
        }
    };

    Callback<Place> placeDetailCallback = new Callback<Place>() {
        @Override
        public void onSuccess(Place place) {
            chosenRestaurantName = place.getName();
            chosenRestaurantAddress = place.getVicinity();
        }

        @Override
        public void onFailure() {
            chosenRestaurantName = null;
            chosenRestaurantAddress = null;
        }
    };

    private void UsersToStringConverter(User[] users) {

        if (users != null) {
            StringBuilder builder = new StringBuilder();
            for (User user : users) {
                builder.append(user.getUsername()).append(", ");
                final int length = builder.length();
                builder.delete(length - 2, length);  // To remove the last comma-space
            }
            joiningColleagues = builder.toString();
        }
        else joiningColleagues = null;
    }

    private void chainNextWork(Context context,Data input) {

        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationBuilding.class)
                .setInputData(input)
                .addTag("NOTIFICATION_DISPLAY_JOB")
                .build();

        WorkManager.getInstance(context).enqueue(workRequest);
    }


}
