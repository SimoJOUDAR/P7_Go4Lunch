package fr.joudar.go4lunch.domain.models;

public class Autocomplete {

    private final String title;
    private final String detail;
    private final String placeId;
    private final int distance;

    public Autocomplete(String title, String detail, String placeId, int distance) {
        this.title = title;
        this.detail = detail;
        this.placeId = placeId;
        this.distance = distance;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public String getPlaceId() {
        return placeId;
    }

    public int getDistance() {
        return distance;
    }
}
