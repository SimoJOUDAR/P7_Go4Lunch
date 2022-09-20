package fr.joudar.go4lunch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import static fr.joudar.go4lunch.utils.TestUtils.dummyAutoCompleteCallback;
import static fr.joudar.go4lunch.utils.TestUtils.dummyAutocompleteInput;
import static fr.joudar.go4lunch.utils.TestUtils.dummyIsFiltered;
import static fr.joudar.go4lunch.utils.TestUtils.dummyIsNotFiltered;
import static fr.joudar.go4lunch.utils.TestUtils.dummyLocation;
import static fr.joudar.go4lunch.utils.TestUtils.dummySearchRadius;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import fr.joudar.go4lunch.domain.services.AutocompleteProvider;
import fr.joudar.go4lunch.repositories.AutocompleteRepository;

@RunWith(MockitoJUnitRunner.class)
public class AutocompleteRepositoryTest {

    AutocompleteProvider autocompleteProvider;
    AutocompleteRepository autocompleteRepository;

    @Before
    public void setUp() {
        autocompleteProvider = mock(AutocompleteProvider.class);
        autocompleteRepository = new AutocompleteRepository(autocompleteProvider);
    }

    @Test
    public void should_get_autocompletes_with_filter() {
        autocompleteRepository.getAutocompletes(dummyAutocompleteInput, dummyLocation, dummySearchRadius, dummyIsFiltered, dummyAutoCompleteCallback);
        verify(autocompleteProvider).getAutocompletes(dummyAutocompleteInput, dummyLocation, dummySearchRadius, dummyIsFiltered, dummyAutoCompleteCallback);
    }

    @Test
    public void should_get_autocompletes_with_no_filter() {
        autocompleteRepository.getAutocompletes(dummyAutocompleteInput, dummyLocation, dummySearchRadius, dummyIsNotFiltered, dummyAutoCompleteCallback);
        verify(autocompleteProvider).getAutocompletes(dummyAutocompleteInput, dummyLocation, dummySearchRadius, dummyIsNotFiltered, dummyAutoCompleteCallback);
    }

}
