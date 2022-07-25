package fr.joudar.go4lunch.domain.di;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import fr.joudar.go4lunch.domain.core.CurrentLocationHandler;
import fr.joudar.go4lunch.domain.services.CurrentLocationProvider;

@Module
@InstallIn(ActivityComponent.class)
public abstract class ActivityModule {

    @Binds
    public abstract CurrentLocationProvider bindCurrentLocationProvider(
            CurrentLocationHandler currentLocationHandler
    );

}
