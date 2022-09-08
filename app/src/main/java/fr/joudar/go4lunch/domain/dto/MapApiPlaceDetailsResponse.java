package fr.joudar.go4lunch.domain.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import fr.joudar.go4lunch.domain.models.Place;

public class MapApiPlaceDetailsResponse {

    @SerializedName("html_attributions")
    @Expose
    public List<Object> htmlAttributions = null;
    @SerializedName("result")
    @Expose
    public PlaceDetailsResult result;
    @SerializedName("status")
    @Expose
    public String status;


    public Place getPlaceDetails() {
        Place resultPlace = null;
        if (status.equals("OK")) {
            resultPlace = new Place(
                    result.placeId,
                    result.name,
                    result.rating,
                    result.formattedAddress,
                    toPhotos(),
                    result.website,
                    result.formattedPhoneNumber);
        }
        return resultPlace;
    }

    private Place.Photo[] toPhotos() {
        Place.Photo[] photosResult = new Place.Photo[0];

        if (result.photos !=  null) {
            final List<Place.Photo> photoList = new ArrayList<>();
            for (Photo currentPhoto : result.photos) {
                photoList.add(
                        new Place.Photo(
                                Place.photoReferenceConverter(currentPhoto.photoReference),
                                currentPhoto.htmlAttributions.toArray(new String[0])));
            }
            photosResult = photoList.toArray(new Place.Photo[0]);
        }
        return photosResult;
    }

    /***********************************************************************************************
     ** Prediction
     **********************************************************************************************/
    class PlaceDetailsResult {

        @SerializedName("formatted_address")
        @Expose
        public String formattedAddress;
        @SerializedName("formatted_phone_number")
        @Expose
        public String formattedPhoneNumber;
        @SerializedName("name")
        @Expose
        public String name;
        @SerializedName("opening_hours")
        @Expose
        public OpeningHours openingHours;
        @SerializedName("photos")
        @Expose
        public List<Photo> photos = null;
        @SerializedName("place_id")
        @Expose
        public String placeId;
        @SerializedName("rating")
        @Expose
        public Double rating = 0.;
        @SerializedName("website")
        @Expose
        public String website;

    }
    /***********************************************************************************************
     ** Prediction
     **********************************************************************************************/
    class Close {

        @SerializedName("day")
        @Expose
        public Integer day;
        @SerializedName("time")
        @Expose
        public String time;

    }
    /***********************************************************************************************
     ** Prediction
     **********************************************************************************************/
    class Open {

        @SerializedName("day")
        @Expose
        public Integer day;
        @SerializedName("time")
        @Expose
        public String time;

    }
    /***********************************************************************************************
     ** Prediction
     **********************************************************************************************/
    class OpeningHours {

        @SerializedName("open_now")
        @Expose
        public Boolean openNow;
        @SerializedName("periods")
        @Expose
        public List<Period> periods = null;
        @SerializedName("weekday_text")
        @Expose
        public List<String> weekdayText = null;

    }
    /***********************************************************************************************
     ** Prediction
     **********************************************************************************************/
    class Period {

        @SerializedName("close")
        @Expose
        public Close close;
        @SerializedName("open")
        @Expose
        public Open open;

    }
    /***********************************************************************************************
     ** Prediction
     **********************************************************************************************/
    class Photo {

        @SerializedName("height")
        @Expose
        public Integer height;
        @SerializedName("html_attributions")
        @Expose
        public List<String> htmlAttributions = null;
        @SerializedName("photo_reference")
        @Expose
        public String photoReference;
        @SerializedName("width")
        @Expose
        public Integer width;

    }


}

