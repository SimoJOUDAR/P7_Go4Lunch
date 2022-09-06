package fr.joudar.go4lunch.ui.core.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import fr.joudar.go4lunch.databinding.WorkplaceDialogBinding;
import fr.joudar.go4lunch.domain.models.Autocomplete;
import fr.joudar.go4lunch.domain.utils.Callback;
import fr.joudar.go4lunch.ui.core.adapters.WorkplaceAutocompleteListAdapter;

public class WorkplaceDialogFragment extends DialogFragment {

    final Callback<String> onTextChanged;
    final WorkplaceAutocompleteListAdapter workplaceAutocompleteListAdapter;

    public WorkplaceDialogFragment(Callback<String> onTextChanged, Callback<Autocomplete> onItemSelected) {
        this.onTextChanged = onTextChanged;
        workplaceAutocompleteListAdapter = new WorkplaceAutocompleteListAdapter(onItemSelected);
        Log.d("WorkplaceDialogFragment", "WorkplaceDialogFragment_C");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Log.d("WorkplaceDialogFragment", "WorkplaceDialogFragment_0");
        final WorkplaceDialogBinding binding = WorkplaceDialogBinding.inflate(getLayoutInflater());

        Log.d("WorkplaceDialogFragment", "WorkplaceDialogFragment_1");
        binding.workplaceAutocompleteList.setLayoutManager(new LinearLayoutManager(getActivity()));
        Log.d("WorkplaceDialogFragment", "WorkplaceDialogFragment_2");
        binding.workplaceAutocompleteList.setAdapter(workplaceAutocompleteListAdapter);
        Log.d("WorkplaceDialogFragment", "WorkplaceDialogFragment_3");
        binding.workplaceSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() >= 3) {

                    //TODO: test to delete -start
                    Log.d("WorkplaceDialog", charSequence.toString());
                    //TODO: Test to delete -end

                    onTextChanged.onSuccess(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        Log.d("WorkplaceDialogFragment", "WorkplaceDialogFragment_4");

        return new AlertDialog.Builder(getActivity())
                .setView(binding.getRoot())
                .setTitle("Select your actual workplace")
                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .create();
    }

    public void updateWorkplaceDialogList(Autocomplete[] autocompleteList) {
        workplaceAutocompleteListAdapter.updateWorkplaceDialogList(autocompleteList);
        Log.d("WorkplaceDialogFragment", "WorkplaceDialogFragment_U");
    }
}
