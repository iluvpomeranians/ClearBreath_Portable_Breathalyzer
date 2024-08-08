package com.example.coen390androidproject_breathalyzerapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
/// Class that displays a dialog fragment when a high BAC level is detected
public class HighBACDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("HIGH LEVEL OF BAC DETECTED")
                .setMessage("PLEASE PROCEED WITH CAUTION AND GET HELP IF NEEDED")
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent = new Intent(getActivity(), EmergencyActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                })
                .setCancelable(false);
        return builder.create();
    }
}
