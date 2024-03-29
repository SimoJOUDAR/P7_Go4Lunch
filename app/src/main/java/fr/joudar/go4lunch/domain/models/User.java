package fr.joudar.go4lunch.domain.models;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class User {

    private String id;
    private String username;
    private String email;
    @Nullable private String avatarUrl;
    @Nullable private String workplaceId ="";
    @Nullable private String workplaceName ="";
    @Nullable private String workplaceAddress = "";
    @Nullable private String chosenRestaurantId = "";
    @Nullable private String chosenRestaurantName = "";
    @Nullable private String chosenRestaurantAddress = "";
    @Nullable private List<String> likedRestaurantsIdList = new ArrayList<>();

    public User(){}

    public User(String id, String username, String email, @Nullable String avatarUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }
    public User(String id, String username, String email, @Nullable String avatarUrl, @Nullable String workplaceId, @Nullable String workplaceName, @Nullable String workplaceAddress, @Nullable String chosenRestaurantId, @Nullable String chosenRestaurantName, @Nullable String chosenRestaurantAddress, @Nullable List<String> likedRestaurantsIdList) {
        this(id, username, email, avatarUrl);
        this.workplaceId = workplaceId;
        this.workplaceName = workplaceName;
        this.workplaceAddress = workplaceAddress;
        this.chosenRestaurantId = chosenRestaurantId;
        this.chosenRestaurantName = chosenRestaurantName;
        this.chosenRestaurantAddress = chosenRestaurantAddress;
        this.likedRestaurantsIdList = likedRestaurantsIdList;
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
    public String getWorkplaceId() {
        return workplaceId;
    }

    @Nullable
    public String getWorkplaceName() {
        return workplaceName;
    }

    @Nullable
    public String getWorkplaceAddress() {
        return workplaceAddress;
    }

    @Nullable
    public String getChosenRestaurantId() {
        return chosenRestaurantId;
    }

    @Nullable
    public String getChosenRestaurantName() {
        return chosenRestaurantName;
    }

    @Nullable
    public String getChosenRestaurantAddress() {
        return chosenRestaurantAddress;
    }

    @Nullable
    public List<String> getLikedRestaurantsIdList() {
        return likedRestaurantsIdList;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAvatarUrl(@Nullable String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setWorkplaceId(@Nullable String workplaceId) {
        this.workplaceId = workplaceId;
    }

    public void setWorkplaceName(@Nullable String workplaceName) {
        this.workplaceName = workplaceName;
    }

    public void setWorkplaceAddress(@Nullable String workplaceAddress) {
        this.workplaceAddress = workplaceAddress;
    }

    public void setChosenRestaurantId(@Nullable String chosenRestaurantId) {
        this.chosenRestaurantId = chosenRestaurantId;
    }

    public void setChosenRestaurantName(@Nullable String chosenRestaurantName) {
        this.chosenRestaurantName = chosenRestaurantName;
    }

    public void setChosenRestaurantAddress(@Nullable String chosenRestaurantAddress) {
        this.chosenRestaurantAddress = chosenRestaurantAddress;
    }

    public void setLikedRestaurantsIdList(@Nullable List<String> likedRestaurantsIdList) {
        this.likedRestaurantsIdList = likedRestaurantsIdList;
    }

    public void setWorkplace(Autocomplete workplace) {
        this.workplaceId = workplace.getPlaceId();
        this.workplaceName = workplace.getTitle();
        this.workplaceAddress = workplace.getDetail();
    }

    public void setChosenRestaurant(Place place) {
        this.chosenRestaurantId = place.getId();
        this.chosenRestaurantName = place.getName();
        this.chosenRestaurantAddress = place.getVicinity();
    }

    public boolean isWorkplaceIdSet() { //TODO: use these to replace the long expressions
        return (getWorkplaceId() != null && !getWorkplaceId().isEmpty() && !getWorkplaceId().equals(""));
    }

    public boolean isChosenRestaurantSet() { //TODO: use these to replace the long expressions
        return (getChosenRestaurantId() != null && !getChosenRestaurantId().isEmpty() && !getChosenRestaurantId().equals(""));
    }
}


