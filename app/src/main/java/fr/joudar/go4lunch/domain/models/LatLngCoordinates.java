package fr.joudar.go4lunch.domain.models;

//TODO: unused? To delete? (because we used Position instead)
public class LatLngCoordinates {
    private double latitude;
    private double longitude;

    public LatLngCoordinates() {}

    public LatLngCoordinates(double latitudes, double longitudes) {
        setLatitude(latitudes);
        setLongitude(longitudes);
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return latitude + "," + longitude;
    }
}
