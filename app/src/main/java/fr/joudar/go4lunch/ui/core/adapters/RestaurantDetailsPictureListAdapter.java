package fr.joudar.go4lunch.ui.core.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;

import fr.joudar.go4lunch.databinding.RestaurantDetailsPhotosRecyclerviewItemBinding;
import fr.joudar.go4lunch.domain.models.Place;

public class RestaurantDetailsPictureListAdapter extends RecyclerView.Adapter<RestaurantDetailsPictureListAdapter.RestaurantDetailsPictureItem> {

    private Context context;
    private Place.Photo[] photos;
    private final RequestListener<Drawable> listener;

    public RestaurantDetailsPictureListAdapter(Context context, Place.Photo[] photos, RequestListener<Drawable> listener) {
        this.context = context;
        this.photos = photos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RestaurantDetailsPictureItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RestaurantDetailsPhotosRecyclerviewItemBinding mBinding = RestaurantDetailsPhotosRecyclerviewItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RestaurantDetailsPictureItem(context, mBinding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantDetailsPictureItem holder, int position) {
        holder.updateView(photos[position]);
    }

    @Override
    public int getItemCount() {
        return photos.length;
    }

    /***********************************************************************************************
     ** ViewHolder
     **********************************************************************************************/
    public class RestaurantDetailsPictureItem extends RecyclerView.ViewHolder {

        Context context;
        RestaurantDetailsPhotosRecyclerviewItemBinding binding;
        RequestListener<Drawable> listener;



        public RestaurantDetailsPictureItem(@NonNull Context context, @NonNull RestaurantDetailsPhotosRecyclerviewItemBinding binding, @NonNull RequestListener<Drawable> listener) {
            super(binding.getRoot());
            this.context = context;
            this.binding = binding;
            this.listener = listener;

        }

        public void updateView(Place.Photo photo) {
            Glide.with(context).load(photo.getReference()).centerCrop().listener(listener).into(binding.photo);
        }
    }
}
