package com.example.coen390androidproject_breathalyzerapp;

import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ConfirmDeleteDialogFragment extends DialogFragment {

    public static ConfirmDeleteDialogFragment newInstance(String username) {
        // Create a new instance of ConfirmDeleteDialogFragment
        ConfirmDeleteDialogFragment fragment = new ConfirmDeleteDialogFragment();
        // Create a bundle to hold the arguments
        Bundle args = new Bundle();
        // Put the username argument into the bundle
        args.putString("username", username);
        // Set the arguments for the fragment
        fragment.setArguments(args);
        // Return the new fragment instance
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Retrieve the username argument from the bundle
        String username = getArguments().getString("username");

        // Build and return an AlertDialog
        return new AlertDialog.Builder(getActivity())
                .setTitle("Confirm Delete") // Set the dialog title
                .setMessage("Are you sure you want to delete the account: " + username + "?") // Set the dialog message
                .setPositiveButton("Yes", (dialog, which) -> {

                    //TO DO, ACTUAL CODE TO DELETE ACCOUNT - 2 MONTHS AGO PROB

                    // NOW, IN THE PRESENT, WE ARE USING A DIFF METHOD TO DELETE THE ACCOUNT, SO THE DIALOG FRAGMENT ISNT BEING CALLED

                })
                .setNegativeButton("No", null) // Handle the negative button click
                .create(); // Create and return the dialog
    }
}
