package fr.joudar.go4lunch.ui.core.adapters;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.databinding.ColleagueItemBinding;
import fr.joudar.go4lunch.domain.models.Place;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.utils.Callback;

public class ColleaguesListAdapter extends RecyclerView.Adapter<ColleaguesListAdapter.ColleagueViewHolder> {

    private User[] users = new User[0];
    private final Callback<String> callback;

    // Null Callback indicates that the adapter is used in RestaurantDetailsFragment, the opposite is for ColleaguesListFragment
    public ColleaguesListAdapter(@Nullable Callback<String> callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public ColleagueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ColleagueItemBinding binding = ColleagueItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ColleagueViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ColleagueViewHolder holder, int position) {
        holder.updateViewHolder(users[position]);
    }

    @Override
    public int getItemCount() {
        return users.length;
    }

    // Use case: Because we can't set up the RecyclerView and its Adapter from a background thread,
    // we first init them from the main thread, then we call this method from the Call's onResponse
    // background thread to update the data of the RecyclerView
    public void updateData(User[] data) {
        users = data;
        notifyDataSetChanged();
    }

    /***********************************************************************************************
     ** ViewHolder
     **********************************************************************************************/

    public class ColleagueViewHolder extends RecyclerView.ViewHolder {

        ColleagueItemBinding binding;

        public ColleagueViewHolder(ColleagueItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // Null Callback indicates that the adapter is used in RestaurantDetailsFragment, the opposite is for ColleaguesListFragment
        private void updateViewHolder(@NonNull User user) {
            RequestOptions options = new RequestOptions().centerCrop().placeholder(R.drawable.avatar_placeholder).error(R.drawable.avatar_placeholder);
            Glide.with(binding.getRoot()).load(user.getAvatarUrl()).apply(options).into(binding.colleagueAvatar);
            final Resources resources = binding.getRoot().getResources();
            String contentText;
            if (callback == null) {
                contentText = resources.getString(R.string.colleague_is_joining, user.getUsername());
            }
            else {
                if (!user.isChosenRestaurantSet()) {
                    contentText = resources.getString(R.string.colleague_has_not_chosen, user.getUsername());
                    binding.colleagueName.setTypeface(null, Typeface.ITALIC);
                }
                else {
                    contentText = resources.getString(R.string.colleague_is_eating_at, user.getUsername(), user.getChosenRestaurantName());
                    binding.colleagueName.setTextColor(Color.BLACK);
//                    binding.colleagueName.setTypeface(null, Typeface.BOLD);
                    binding.getRoot().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            callback.onSuccess(user.getChosenRestaurantId());
                        }
                    });
                }
            }
            binding.colleagueName.setText(contentText);
        }

    }
}
