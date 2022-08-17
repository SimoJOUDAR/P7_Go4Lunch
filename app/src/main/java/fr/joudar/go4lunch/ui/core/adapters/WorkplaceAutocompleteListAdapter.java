package fr.joudar.go4lunch.ui.core.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import fr.joudar.go4lunch.databinding.WorkplaceDialogAutocompleteItemBinding;
import fr.joudar.go4lunch.domain.models.Autocomplete;
import fr.joudar.go4lunch.domain.utils.Callback;

public class WorkplaceAutocompleteListAdapter extends RecyclerView.Adapter<WorkplaceAutocompleteListAdapter.WorkplaceAutocompleteViewHolder>{

    private Autocomplete[] autocompleteList = new Autocomplete[0];
    private final Callback<Autocomplete> callback;

    public WorkplaceAutocompleteListAdapter(Callback<Autocomplete> callback) {
        this.callback = callback;
    }


    @NonNull
    @Override
    public WorkplaceAutocompleteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        WorkplaceDialogAutocompleteItemBinding workplaceDialogAutocompleteItemBinding = WorkplaceDialogAutocompleteItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);
        return new WorkplaceAutocompleteViewHolder(workplaceDialogAutocompleteItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkplaceAutocompleteViewHolder holder, int position) {
        holder.updateView(autocompleteList[position]);
    }

    @Override
    public int getItemCount() {
        return autocompleteList.length;
    }

    public void updateWorkplaceDialogList(Autocomplete[] autocompleteList) {
        this.autocompleteList = autocompleteList;
        notifyDataSetChanged();
    }

    /***********************************************************************************************
     ** ViewHolder
     **********************************************************************************************/

    public class WorkplaceAutocompleteViewHolder extends RecyclerView.ViewHolder {

        WorkplaceDialogAutocompleteItemBinding binding;

        public WorkplaceAutocompleteViewHolder(@NonNull WorkplaceDialogAutocompleteItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void updateView(Autocomplete autocomplete) {
            binding.workplaceTitle.setText(autocomplete.getTitle());
            binding.workplaceDetail.setText(autocomplete.getDetail());
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.onSuccess(autocomplete);
                }
            });

        }
    }
}
