package fr.joudar.go4lunch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static fr.joudar.go4lunch.utils.TestUtils.colleaguesDistributionCallback;
import static fr.joudar.go4lunch.utils.TestUtils.dummyUser;
import static fr.joudar.go4lunch.utils.TestUtils.dummyUsersCallback;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.junit.MockitoJUnitRunner;

import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.services.FirebaseServicesProvider;
import fr.joudar.go4lunch.domain.utils.Callback;
import fr.joudar.go4lunch.repositories.FirebaseServicesRepository;

@RunWith(MockitoJUnitRunner.class)
public class FirebaseServicesRepositoryTest {

    FirebaseServicesProvider firebaseProvider;
    FirebaseServicesRepository firebaseRepository;


    @Before
    public void setup() {
        firebaseProvider = mock(FirebaseServicesProvider.class);
        firebaseRepository = new FirebaseServicesRepository(firebaseProvider);
        when(firebaseProvider.getCurrentUser()).thenReturn(dummyUser);
    }

    @Test
    public void should_get_current_user() {
        User user = firebaseRepository.getCurrentUser();
        Assert.assertEquals(dummyUser, user);
    }

    @Test
    public void should_get_colleagues() {
        firebaseRepository.getColleagues(dummyUsersCallback);
        verify(firebaseProvider).getColleagues(any(Callback.class));
    }

    @Test
    public void should_get_colleagues_by_restaurant() {
        firebaseRepository.getColleaguesByRestaurant("workplaceId", dummyUsersCallback);
        verify(firebaseProvider).getColleaguesByRestaurant(eq(dummyUser.getWorkplaceId()), any(Callback.class));
    }

    @Test
    public void should_get_colleagues_Distribution_over_restaurants() {
        firebaseRepository.getColleaguesDistributionOverRestaurants(colleaguesDistributionCallback);
        verify(firebaseProvider).getColleaguesDistributionOverRestaurants(any(Callback.class));
    }
}
