package fr.joudar.go4lunch.domain.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

public class Place {

    private final String id;
    private final String name;
    private final double rating;
    private final String vicinity;
    private boolean isOpen;
    private String mainPhotoUrl;
    private LatLng coordinates;
    private String websiteUrl;
    private String phoneNumber;
    private Photo[] allPhotos;
    private String icon;

    /***********************************************************************************************
     ** Nearbysearch Place constructor
     **********************************************************************************************/
    public Place(
            String id,
            String name,
            Double rating,
            String vicinity,
            String mainPhotoUrl,
            boolean isOpen,
            LatLng coordinates,
            String icon) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.vicinity = vicinity;
        this.mainPhotoUrl = mainPhotoUrl;
        this.isOpen = isOpen;
        this.coordinates = coordinates;
        this.icon = icon;
    }

    /***********************************************************************************************
     ** Details Place constructor
     **********************************************************************************************/
    public Place(
            String id,
            String name,
            Double rating,
            String vicinity,
            Photo[] allPhotos,
            String websiteUrl,
            String phoneNumber) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.vicinity = vicinity;
        this.allPhotos = allPhotos;
        this.websiteUrl = websiteUrl;
        this.phoneNumber = phoneNumber;
        if (allPhotos.length > 0) mainPhotoUrl = allPhotos[0].getReference();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getRating() {
        return rating;
    }

    public String getVicinity() {
        return vicinity;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public String getMainPhotoUrl() {
        return mainPhotoUrl;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Photo[] getAllPhotos() {
        return allPhotos;
    }

    public String getIcon() {
        return icon;
    }

    /***********************************************************************************************
     ** Photo Class
     **********************************************************************************************/
    public static class Photo {

        private final String reference;
        private final String[] attributions;

        public Photo(String reference, String[] attributions) {
            this.reference = reference;
            this.attributions = attributions;
        }

        public String getReference() {
            return reference;
        }
        public String[] getAttributions() {
            return attributions;
        }
    }
}
