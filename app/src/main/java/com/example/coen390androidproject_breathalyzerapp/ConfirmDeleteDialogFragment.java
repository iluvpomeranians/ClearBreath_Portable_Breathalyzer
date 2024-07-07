package com.example.coen390androidproject_breathalyzerapp;

import android.app.Dialog;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ConfirmDeleteDialogFragment extends DialogFragment {

    public static ConfirmDeleteDialogFragment newInstance(String username) {
        ConfirmDeleteDialogFragment fragment = new ConfirmDeleteDialogFragment();
        Bundle args = new Bundle();
        args.putString("username", username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String username = getArguments().getString("username");

        return new AlertDialog.Builder(getActivity())
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete the account: " + username + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (getActivity() instanceof AccountActivity) {
                        ((AccountActivity) getActivity()).deleteAccount(username);
                    }
                })
                .setNegativeButton("No", null)
                .create();
    }
}
