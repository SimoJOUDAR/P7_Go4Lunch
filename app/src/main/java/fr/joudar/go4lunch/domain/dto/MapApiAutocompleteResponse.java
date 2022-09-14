package fr.joudar.go4lunch.domain.dto;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Objects;


import fr.joudar.go4lunch.domain.models.Autocomplete;

public class MapApiAutocompleteResponse {

    @SerializedName("predictions")
    @Expose
    public List<Prediction> predictions = null;
    @SerializedName("status")
    @Expose
    public String status;

    public Autocomplete[] getAutocomplete(boolean isFiltered) {
        Function<Prediction, Autocomplete> getter;
        if (isFiltered)
            getter = Prediction::toFilteredAutocomplete;
        else
            getter = Prediction::toAutocomplete;

        Autocomplete[] autocompletes = new Autocomplete[0];
        if (status.equals("OK")) {
            autocompletes = Stream.of(predictions)
                    .map(getter)
                    .filter(Objects::nonNull)
                    .toArray(Autocomplete[]::new);
        }
        return autocompletes;
    }


    /***********************************************************************************************
     ** Prediction
     **********************************************************************************************/
    class Prediction {

        @SerializedName("description")
        @Expose
        public String description;
        @SerializedName("distance_meters")
        @Expose
        public Integer distanceMeters;
        @SerializedName("matched_substrings")
        @Expose
        public List<MatchedSubstring> matchedSubstrings = null;
        @SerializedName("place_id")
        @Expose
        public String placeId;
        @SerializedName("reference")
        @Expose
        public String reference;
        @SerializedName("structured_formatting")
        @Expose
        public StructuredFormatting structuredFormatting;
        @SerializedName("terms")
        @Expose
        public List<Term> terms = null;
        @SerializedName("types")
        @Expose
        public List<String> types = null;

        public Autocomplete toAutocomplete() {
            Autocomplete autocomplete;
            try {
                autocomplete = new Autocomplete(
                        structuredFormatting.mainText,
                        structuredFormatting.secondaryText,
                        placeId,
                        distanceMeters);
            } catch (Exception e) {
                autocomplete = null;
            }
            return autocomplete;
        }

        public Autocomplete toFilteredAutocomplete() {
            if (types.contains("restaurant")
                    || types.contains("food")
                    || types.contains("meal_takeaway")
                    || types.contains("meal_delivery")) {
                return toAutocomplete();
            }
            return null;
        }

    }
    /***********************************************************************************************
     ** MainTextMatchedSubstring
     **********************************************************************************************/
    class MainTextMatchedSubstring {

        @SerializedName("length")
        @Expose
        public Integer length;
        @SerializedName("offset")
        @Expose
        public Integer offset;

    }
    /***********************************************************************************************
     ** MatchedSubstring
     **********************************************************************************************/
    class MatchedSubstring {

        @SerializedName("length")
        @Expose
        public Integer length;
        @SerializedName("offset")
        @Expose
        public Integer offset;

    }
    /***********************************************************************************************
     ** StructuredFormatting
     **********************************************************************************************/
    class StructuredFormatting {

        @SerializedName("main_text")
        @Expose
        public String mainText;
        @SerializedName("main_text_matched_substrings")
        @Expose
        public List<MainTextMatchedSubstring> mainTextMatchedSubstrings = null;
        @SerializedName("secondary_text")
        @Expose
        public String secondaryText;

    }
    /***********************************************************************************************
     ** Term
     **********************************************************************************************/
    class Term {

        @SerializedName("offset")
        @Expose
        public Integer offset;
        @SerializedName("value")
        @Expose
        public String value;

    }
}



