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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bac_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BACRecord record = bacRecordList.get(position);
        holder.bacTextView.setText(String.format("BAC: %.2f", record.getBacValue()));
        holder.timestampTextView.setText(String.format("Timestamp: %s", record.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return bacRecordList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView bacTextView;
        TextView timestampTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bacTextView = itemView.findViewById(R.id.bacTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}
