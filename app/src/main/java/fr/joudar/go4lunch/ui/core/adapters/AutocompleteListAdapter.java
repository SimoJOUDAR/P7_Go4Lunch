package fr.joudar.go4lunch.ui.core.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import fr.joudar.go4lunch.databinding.AutocompleteItemBinding;
import fr.joudar.go4lunch.domain.models.Autocomplete;
import fr.joudar.go4lunch.domain.utils.Callback;

public class AutocompleteListAdapter extends RecyclerView.Adapter<AutocompleteListAdapter.AutocompleteViewHolder>{

    private Autocomplete[] autocompleteList = new Autocomplete[0];
    private final Callback<String> callback;

    public AutocompleteListAdapter(Callback<String> callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public AutocompleteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AutocompleteItemBinding autocompleteItemBinding = AutocompleteItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);
        return new AutocompleteViewHolder(autocompleteItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull AutocompleteViewHolder holder, int position) {
        holder.updateView(autocompleteList[position]);
    }

    @Override
    public int getItemCount() {
        return autocompleteList.length;
    }

    public void updateAutocompleteList(Autocomplete[] autocompleteList) {
        this.autocompleteList = autocompleteList;
        notifyDataSetChanged();
    }

    /***********************************************************************************************
     ** ViewHolder
     **********************************************************************************************/

    public class AutocompleteViewHolder extends RecyclerView.ViewHolder {

        private final AutocompleteItemBinding binding;

        public AutocompleteViewHolder(@NonNull AutocompleteItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void updateView(Autocomplete autocomplete) {
            binding.autocompleteTitle.setText(autocomplete.getTitle());
            binding.autocompleteSubtitle.setText(autocomplete.getDetail());
            binding.distance.setText(autocomplete.getDistance());
            binding.showDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.onSuccess(autocomplete.getPlaceId());
                    //binding.getRoot().setVisibility(View.INVISIBLE); // TODO : Close the search RecyclerView before navigation ?
                }
            });

        }


    }
}
