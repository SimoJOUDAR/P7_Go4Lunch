package fr.joudar.go4lunch.ui.core.adapters;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import fr.joudar.go4lunch.R;
import fr.joudar.go4lunch.databinding.ColleagueItemBinding;
import fr.joudar.go4lunch.domain.models.User;
import fr.joudar.go4lunch.domain.utils.Callback;

public class ColleaguesListAdapter extends RecyclerView.Adapter<ColleaguesListAdapter.ColleagueViewHolder> {

    private User[] users;
    private final Callback<String> callback;

    // Null Callback indicates that the adapter is used in RestaurantDetailsFragment, the opposite is for ColleaguesListFragment
    public ColleaguesListAdapter(@Nullable User[] users, @Nullable Callback<String> callback) {
        this.users = users;
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

    public void updateList(User[] colleagues){
        users = colleagues;
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
        private void updateViewHolder(User user) {
            Glide.with(binding.getRoot()).load(user.getAvatarUrl()).centerCrop().into(binding.colleagueAvatar);
            final Resources resources = binding.getRoot().getResources();
            String contentText;
            if (callback == null) {
                contentText = resources.getString(R.string.colleague_is_joining, user.getUsername());
            }
            else {
                if (user.getChosenRestaurantName() == null)
                    contentText = resources.getString(R.string.colleague_has_not_chosen, user.getUsername());
                else {
                    contentText = resources.getString(R.string.colleague_is_eating_at, user.getUsername(), user.getChosenRestaurantName());
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
