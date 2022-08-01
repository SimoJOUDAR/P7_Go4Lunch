package fr.joudar.go4lunch.domain.di;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import fr.joudar.go4lunch.domain.core.FirebaseServicesHandler;
import fr.joudar.go4lunch.domain.services.FirebaseServicesProvider;

@Module
@InstallIn(SingletonComponent.class)
public abstract class SingletonModuleForBinding {

    @Binds
    @Singleton
    public abstract FirebaseServicesProvider bindFirebaseServicesProvider(
            FirebaseServicesHandler firebaseServicesHandler
    );

}
