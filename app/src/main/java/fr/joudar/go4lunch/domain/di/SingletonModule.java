package fr.joudar.go4lunch.domain.di;

import static java.util.Collections.emptyList;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import fr.joudar.go4lunch.domain.core.FirebaseServicesHandler;
import fr.joudar.go4lunch.domain.core.GoogleApiHandler;
import fr.joudar.go4lunch.domain.services.HttpQueryProvider;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class) // lives throughout the app's lifecycle
public class SingletonModule implements Initializer<WorkManager> {

    @Provides
    @Singleton // Insures instance singleness
    public FirebaseServicesHandler provideFirebaseServicesHandler() {
        return new FirebaseServicesHandler(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance());
    }

    @Provides
    @Singleton
    public HttpQueryProvider provideHttpQueryProvider() {
        return new Retrofit.Builder()
                .baseUrl(GoogleApiHandler.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(HttpQueryProvider.class);
    }

    @Provides
    @Singleton
    @NonNull
    @Override
    public WorkManager create(@ApplicationContext @NonNull Context context) {
        Configuration configuration = new Configuration.Builder().build();
        WorkManager.initialize(context, configuration);
        Log.d("Hilt Init", "WorkManager initialized by Hilt this time");
        return WorkManager.getInstance(context);
    }


    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return emptyList();
    }
}
