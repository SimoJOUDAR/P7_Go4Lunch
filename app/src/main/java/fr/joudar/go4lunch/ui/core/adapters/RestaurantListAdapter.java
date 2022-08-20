package fr.joudar.go4lunch.ui.core.adapters;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import java.util.Map;
import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.databinding.RestaurantListItemBinding;
import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.utils.Calculus;
import fr.joudar.go4lunch.domain.utils.Callback;

public class RestaurantListAdapter extends RecyclerView.Adapter<RestaurantListAdapter.RestaurantListViewHolder> {

    private Place[] places;
    private Location currentLocation;
    private final Callback<String> onItemClicked;
    private Map<String, Integer> colleaguesDistribution;

    public RestaurantListAdapter(Place[] places, Location currentLocation, Callback<String> onItemClicked, Map<String, Integer> colleaguesDistribution) {
        this.places = places;
        this.currentLocation = currentLocation;
        this.onItemClicked = onItemClicked;
        this.colleaguesDistribution = colleaguesDistribution;
    }

    @NonNull
    @Override
    public RestaurantListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RestaurantListItemBinding binding = RestaurantListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RestaurantListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantListViewHolder holder, int position) {
        holder.updateView(places[position]);
    }

    @Override
    public int getItemCount() {
        return places.length;
    }

    public void updateColleaguesDistribution(Map<String, Integer> map) {
        colleaguesDistribution = map;
        notifyDataSetChanged();
    }

    public void updateRestaurantList(Place[] results, Location currentLocation) {
        places = results;
        this.currentLocation = currentLocation;
        notifyDataSetChanged();
    }


    /***********************************************************************************************
     ** ViewHolder
     **********************************************************************************************/
    public class RestaurantListViewHolder  extends RecyclerView.ViewHolder {

        RestaurantListItemBinding binding;
        RequestListener<Drawable> listener = new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                binding.photoShimmerLayout.stopShimmer();
                binding.photoShimmerLayout.setVisibility(View.GONE);
                return false;
            }
        };

        public RestaurantListViewHolder(@NonNull RestaurantListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void updateView(Place place) {
            binding.name.setText(place.getName());
            binding.address.setText(place.getVicinity());
            isOpen(place);
            getDistance(place);
            ColleaguesDistribution(place);
            ratingStarsHandler(place);
            loadPhoto(place);
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClicked.onSuccess(place.getId());
                }
            });
        }

        /***********************************************************************************************
         ** Utils
         **********************************************************************************************/

        private void isOpen(Place place){
            if (place.isOpen()) {
                binding.isOpen.setText(R.string.is_open_true);
                binding.isOpen.setTextColor(ResourcesCompat.getColor(binding.getRoot().getResources(), R.color.light_green, null));
            } else {
                binding.isOpen.setText(R.string.is_open_false);
                binding.isOpen.setTextColor(ResourcesCompat.getColor(binding.getRoot().getResources(), R.color.light_red, null));
            }
        }

        private void getDistance(Place place) {
            if (currentLocation == null)
                binding.restaurantDistance.setText(R.string.restaurant_distance_unavailable);
            else {
                binding.restaurantDistance.setText((int) Calculus.distanceBetween(currentLocation, place.getCoordinates()));
            }
        }

        private void ColleaguesDistribution(Place place) {
            if (colleaguesDistribution != null) {
                final Integer count = colleaguesDistribution.get(place.getId());
                if (count != null && count != 0) {
                    String fc = "("+ count +")";
                    binding.joiningColleaguesSum.setText(fc);
                }
            }
        }

        // Update the rating stars
        private void ratingStarsHandler(Place place) {
            int rating = Calculus.ratingStarsCalculator(place.getRating());
            switch (rating) {
                case 3:
                    binding.rateStarN1.setVisibility(View.VISIBLE);
                    binding.rateStarN2.setVisibility(View.VISIBLE);
                    binding.rateStarN3.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    binding.rateStarN1.setVisibility(View.VISIBLE);
                    binding.rateStarN2.setVisibility(View.VISIBLE);
                    binding.rateStarN3.setVisibility(View.GONE);
                    break;
                case 1:
                    binding.rateStarN1.setVisibility(View.VISIBLE);
                    binding.rateStarN2.setVisibility(View.GONE);
                    binding.rateStarN3.setVisibility(View.GONE);
                    break;
                default:
                    binding.rateStarN1.setVisibility(View.GONE);
                    binding.rateStarN2.setVisibility(View.GONE);
                    binding.rateStarN3.setVisibility(View.GONE);
            }
        }

        public void loadPhoto(Place place) {
            Glide.with(binding.getRoot().getContext())
                    .load(place.getMainPhotoUrl())
                    .centerCrop()
                    .listener(listener)
                    .into(binding.photo);
        }

    }
}
