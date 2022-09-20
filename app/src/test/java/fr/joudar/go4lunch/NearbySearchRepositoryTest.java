package fr.joudar.go4lunch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import static fr.joudar.go4lunch.utils.TestUtils.dummyLocation;
import static fr.joudar.go4lunch.utils.TestUtils.dummyNearbysearchCallback;
import static fr.joudar.go4lunch.utils.TestUtils.dummySearchRadius;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import fr.joudar.go4lunch.domain.services.NearbysearchProvider;
import fr.joudar.go4lunch.repositories.NearbysearchRepository;

@RunWith(MockitoJUnitRunner.class)
public class NearbySearchRepositoryTest {

    NearbysearchProvider nearbysearchProvider;
    NearbysearchRepository nearbysearchRepository;

    @Before
    public void setUp() {
        nearbysearchProvider = mock(NearbysearchProvider.class);
        nearbysearchRepository = new NearbysearchRepository(nearbysearchProvider);
    }

    @Test
    public void should_get_places() {
        nearbysearchRepository.getNearbyRestaurant(dummyLocation, dummySearchRadius, dummyNearbysearchCallback);
        verify(nearbysearchProvider).getPlaces(dummyLocation, dummySearchRadius, dummyNearbysearchCallback);
    }
}
