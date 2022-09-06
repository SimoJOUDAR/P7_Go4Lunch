package fr.joudar.go4lunch.ui.core.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.databinding.RestaurantDetailsPhotosRecyclerviewItemBinding;
import fr.joudar.go4lunch.domain.models.Place;

public class RestaurantDetailsPictureListAdapter extends RecyclerView.Adapter<RestaurantDetailsPictureListAdapter.RestaurantDetailsPictureItem> {

    private Context context;
    private Place.Photo[] photos = new Place.Photo[0];

    public RestaurantDetailsPictureListAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public RestaurantDetailsPictureItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RestaurantDetailsPhotosRecyclerviewItemBinding mBinding = RestaurantDetailsPhotosRecyclerviewItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RestaurantDetailsPictureItem(context, mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantDetailsPictureItem holder, int position) {
        holder.updateView(photos[position]);
    }

    @Override
    public int getItemCount() {
        return photos.length;
    }

    // Use case: Because we can't set up the RecyclerView and its Adapter from a background thread,
    // we first init them from the main thread, then we call this method from the Call's onResponse
    // background thread to update the data of the RecyclerView
    public void updateData(Place.Photo[] data) {
        photos = data;
        notifyDataSetChanged();

        //TODO: test to delete -start
        Log.d("Adapter1", "updateData - photos.length = " + photos.length);
        for (Place.Photo photo : photos) {
            Log.d("Adapter1", "updateData - photo.reference: " + photo.getReference());
        }
        Log.d("Adapter1", "updateData - getItemCount() = " + getItemCount());
        //TODO: Test to delete -end
    }

    /***********************************************************************************************
     ** ViewHolder
     **********************************************************************************************/
    public class RestaurantDetailsPictureItem extends RecyclerView.ViewHolder {

        Context context;
        RestaurantDetailsPhotosRecyclerviewItemBinding binding;


        //TODO: test to delete -start
        RequestListener<Drawable> listener = new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                String s = "";
                if (e != null) s = e.getMessage();
                Log.d("Adapter1", "Glide listener: onLoadFailed() - " + s);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                Log.d("Adapter1", "Glide listener: onResourceReady()");
                return false;
            }
        };
        //TODO: Test to delete -end

        public RestaurantDetailsPictureItem(@NonNull Context context, @NonNull RestaurantDetailsPhotosRecyclerviewItemBinding binding) {
            super(binding.getRoot());
            this.context = context;
            this.binding = binding;
        }

        public void updateView(Place.Photo photo) {
            RequestOptions options = new RequestOptions().centerCrop().placeholder(R.drawable.gray_gradient_design).error(R.drawable.main_image);
            Glide.with(context).load(photo.getReference()).apply(options).listener(listener).into(binding.photo);
        }
    }
}
