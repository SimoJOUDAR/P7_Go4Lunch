package fr.joudar.go4lunch.domain.models;

import java.util.List;
import javax.annotation.Nullable;

public class User {

    private final String id;
    private final String username;
    private final String email;
    @Nullable private String avatarUrl;
    @Nullable private List<String> likedRestaurantsIdList;
    @Nullable private String workplaceId ="";
    @Nullable private String chosenRestaurantId = "";
    @Nullable private String chosenRestaurantName = "";

    public User(String id, String username, String email, @Nullable String avatarUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }
    public User(String id, String username, String email, @Nullable String avatarUrl, @Nullable List<String> likedRestaurantsIdList, @Nullable String workplaceId, @Nullable String chosenRestaurantId, @Nullable String chosenRestaurantName) {
        this(id, username, email, avatarUrl);
        this.likedRestaurantsIdList = likedRestaurantsIdList;
        this.workplaceId = workplaceId;
        this.chosenRestaurantId = chosenRestaurantId;
        this.chosenRestaurantName = chosenRestaurantName;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Nullable
    public String getAvatarUrl() {
        return avatarUrl;
    }

    @Nullable
    public List<String> getLikedRestaurantsIdList() {
        return likedRestaurantsIdList;
    }

    @Nullable
    public String getWorkplaceId() {
        return workplaceId;
    }

    @Nullable
    public String getChosenRestaurantId() {
        return chosenRestaurantId;
    }

    @Nullable
    public String getChosenRestaurantName() {
        return chosenRestaurantName;
    }

    public void setAvatarUrl(@Nullable String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setLikedRestaurantsIdList(@Nullable List<String> likedRestaurantsIdList) {
        this.likedRestaurantsIdList = likedRestaurantsIdList;
    }

    public void setWorkplaceId(@Nullable String workplaceId) {
        this.workplaceId = workplaceId;
    }

    public void setChosenRestaurantId(@Nullable String chosenRestaurantId) {
        this.chosenRestaurantId = chosenRestaurantId;
    }

    public void setChosenRestaurantName(@Nullable String chosenRestaurantName) {
        this.chosenRestaurantName = chosenRestaurantName;
    }
}


