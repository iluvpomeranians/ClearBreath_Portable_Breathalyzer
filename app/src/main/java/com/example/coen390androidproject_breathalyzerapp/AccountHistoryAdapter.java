package com.example.coen390androidproject_breathalyzerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AccountHistoryAdapter extends RecyclerView.Adapter<AccountHistoryAdapter.ViewHolder> {

    private List<BACRecord> bacRecordList;

    public AccountHistoryAdapter(List<BACRecord> bacRecordList) {
        this.bacRecordList = bacRecordList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for BAC records
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bac_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the BAC record at the specified position
        BACRecord record = bacRecordList.get(position);
        // Set the BAC value and timestamp in the corresponding TextViews
        holder.bacTextView.setText(String.format("BAC: %.2f", record.getBacValue()));
        holder.timestampTextView.setText(String.format("Timestamp: %s", record.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return bacRecordList.size();
    }

    // ViewHolder class to hold the views for each item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // TextViews to display BAC value and timestamp
        TextView bacTextView;
        TextView timestampTextView;

        // Constructor to initialize the TextViews
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bacTextView = itemView.findViewById(R.id.bacTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}