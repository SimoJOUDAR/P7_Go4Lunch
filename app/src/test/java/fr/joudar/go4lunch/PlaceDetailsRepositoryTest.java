package fr.joudar.go4lunch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import static fr.joudar.go4lunch.utils.TestUtils.dummyPlaceDetailsCallback;
import static fr.joudar.go4lunch.utils.TestUtils.dummyPlaceId;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import fr.joudar.go4lunch.domain.services.PlaceDetailsProvider;
import fr.joudar.go4lunch.repositories.PlaceDetailsRepository;

@RunWith(MockitoJUnitRunner.class)
public class PlaceDetailsRepositoryTest {

    PlaceDetailsProvider placeDetailsProvider;
    PlaceDetailsRepository placeDetailsRepository;

    @Before
    public void setUp() {
        placeDetailsProvider = mock(PlaceDetailsProvider.class);
        placeDetailsRepository = new PlaceDetailsRepository(placeDetailsProvider);
    }

    @Test
    public void should_get_places() {
        placeDetailsRepository.getPlaceDetails(dummyPlaceId, dummyPlaceDetailsCallback);
        verify(placeDetailsProvider).getPlaceDetails(dummyPlaceId, dummyPlaceDetailsCallback);
    }
}
