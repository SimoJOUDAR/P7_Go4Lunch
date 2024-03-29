package fr.joudar.go4lunch.domain.di;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ViewModelComponent;
import dagger.hilt.android.scopes.ViewModelScoped;
import fr.joudar.go4lunch.domain.core.GoogleApiHandler;
import fr.joudar.go4lunch.domain.services.AutocompleteProvider;
import fr.joudar.go4lunch.domain.services.NearbysearchProvider;
import fr.joudar.go4lunch.domain.services.PlaceDetailsProvider;

@Module
@InstallIn(ViewModelComponent.class)
public abstract class ViewModelModuleForBinding {

    @Binds
    @ViewModelScoped
    public abstract NearbysearchProvider bindNearbysearchProvider(GoogleApiHandler googleApiHandler);

    @Binds
    @ViewModelScoped
    public abstract AutocompleteProvider bindAutocompleteProvider(GoogleApiHandler googleApiHandler);

    @Binds
    @ViewModelScoped
    public abstract PlaceDetailsProvider bindPlaceDetailsProvider(GoogleApiHandler googleApiHandler);
}
