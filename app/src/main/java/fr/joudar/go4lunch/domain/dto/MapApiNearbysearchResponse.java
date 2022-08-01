package fr.joudar.go4lunch.domain.dto;

import com.annimon.stream.Stream;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

import fr.joudar.go4lunch.domain.models.Place;


public class MapApiNearbysearchResponse {

    @SerializedName("html_attributions")
    @Expose
    public List<Object> htmlAttributions = null;
    @SerializedName("next_page_token")
    @Expose
    public String nextPageToken;
    @SerializedName("results")
    @Expose
    public List<Result> results = null;
    @SerializedName("status")
    @Expose
    public String status;

    public Place[] getPlaces(){
        Place[] places = new Place[0];
        if (status.equals("OK")) {
            places = Stream.of(results)
                    .map(Result::toPlace)
                    .filter((place) -> place != null)
                    .toArray(Place[]::new);
        }
        return places;
    }

}
/***********************************************************************************************
 ** Result
 **********************************************************************************************/
class Result {

    @SerializedName("business_status")
    @Expose
    public String businessStatus;
    @SerializedName("geometry")
    @Expose
    public Geometry geometry;
    @SerializedName("icon")
    @Expose
    public String icon;
    @SerializedName("icon_background_color")
    @Expose
    public String iconBackgroundColor;
    @SerializedName("icon_mask_base_uri")
    @Expose
    public String iconMaskBaseUri;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("photos")
    @Expose
    public List<Photo> photos = null;
    @SerializedName("place_id")
    @Expose
    public String placeId;
    @SerializedName("plus_code")
    @Expose
    public PlusCode plusCode;
    @SerializedName("rating")
    @Expose
    public Double rating;
    @SerializedName("reference")
    @Expose
    public String reference;
    @SerializedName("scope")
    @Expose
    public String scope;
    @SerializedName("types")
    @Expose
    public List<String> types = null;
    @SerializedName("user_ratings_total")
    @Expose
    public Integer userRatingsTotal;
    @SerializedName("vicinity")
    @Expose
    public String vicinity;
    @SerializedName("opening_hours")
    @Expose
    public OpeningHours openingHours;
    @SerializedName("price_level")
    @Expose
    public Integer priceLevel;
    @SerializedName("permanently_closed")
    @Expose
    public Boolean permanentlyClosed;

    public Place toPlace() {
        Place place;
        try{
            place = new Place(
                    placeId, name, rating, vicinity, photos.get(0).photoReference,openingHours.openNow,
                    new LatLng(geometry.location.lat, geometry.location.lng), icon);
        }
        catch (Exception e) {
            place = null;
        }
        return place;
    }

}
/***********************************************************************************************
 ** Geometry
 **********************************************************************************************/
class Geometry {

    @SerializedName("location")
    @Expose
    public Location location;
    @SerializedName("viewport")
    @Expose
    public Viewport viewport;

}
/***********************************************************************************************
 ** Location
 **********************************************************************************************/
class Location {

    @SerializedName("lat")
    @Expose
    public Double lat;
    @SerializedName("lng")
    @Expose
    public Double lng;

}
/***********************************************************************************************
 ** Northeast
 **********************************************************************************************/
class Northeast {

    @SerializedName("lat")
    @Expose
    public Double lat;
    @SerializedName("lng")
    @Expose
    public Double lng;

}
/***********************************************************************************************
 ** OpeningHours
 **********************************************************************************************/
class OpeningHours {

    @SerializedName("open_now")
    @Expose
    public Boolean openNow;

}
/***********************************************************************************************
 ** Photo
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
/***********************************************************************************************
 ** PlusCode
 **********************************************************************************************/
class PlusCode {

    @SerializedName("compound_code")
    @Expose
    public String compoundCode;
    @SerializedName("global_code")
    @Expose
    public String globalCode;

}
/***********************************************************************************************
 ** Southwest
 **********************************************************************************************/
class Southwest {

    @SerializedName("lat")
    @Expose
    public Double lat;
    @SerializedName("lng")
    @Expose
    public Double lng;

}
/***********************************************************************************************
 ** Viewport
 **********************************************************************************************/

class Viewport {

    @SerializedName("northeast")
    @Expose
    public Northeast northeast;
    @SerializedName("southwest")
    @Expose
    public Southwest southwest;

}

