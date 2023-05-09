package com.lucario.gpt;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class SettingsDialogFragment extends DialogFragment {

    private EditText apiKeyEditText;
    private Button saveButton;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create a new AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate the custom layout for the dialog
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_settings, null);

        // Find the EditText and Button views in the layout
        apiKeyEditText = view.findViewById(R.id.api_key_edit_text);
        saveButton = view.findViewById(R.id.save_button);
        String api = requireContext().getSharedPreferences("apiKey", MODE_PRIVATE).getString("api", "not");
        if(!api.equals("not")){
            apiKeyEditText.setText(api.substring(7));
        }
        // Set an onClickListener for the save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the save button click event
                requireContext().getSharedPreferences("apiKey", MODE_PRIVATE).edit().putString("api", "Bearer " + apiKeyEditText.getText().toString()).apply();
                MessageView.api_key = "Bearer " + apiKeyEditText.getText().toString();
                dismiss();
            }
        });

        // Set the custom layout for the dialog and return it
        builder.setView(view);
        return builder.create();
    }

}

