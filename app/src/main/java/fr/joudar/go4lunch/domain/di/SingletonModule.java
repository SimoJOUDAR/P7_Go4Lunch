package fr.joudar.go4lunch.domain.di;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import fr.joudar.go4lunch.domain.core.FirebaseServicesHandler;
import fr.joudar.go4lunch.domain.core.GoogleApiHandler;
import fr.joudar.go4lunch.domain.services.HttpQueryProvider;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class) // lives throughout the app's lifecycle
public class SingletonModule {

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
}
