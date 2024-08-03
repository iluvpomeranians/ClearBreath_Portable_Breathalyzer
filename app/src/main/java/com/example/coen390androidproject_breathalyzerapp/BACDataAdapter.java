package com.example.coen390androidproject_breathalyzerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BACDataAdapter extends RecyclerView.Adapter<BACDataAdapter.ItemViewHolder> {

    private List<BACData> bacDataList;

    public BACDataAdapter(List<BACData> bacDataList) {
        this.bacDataList = bacDataList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bac_data_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        BACData data = bacDataList.get(position);
        holder.bind(data);
    }

    @Override
    public int getItemCount() {
        return bacDataList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView timeTextView;
        TextView bacLevelTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            bacLevelTextView = itemView.findViewById(R.id.bacLevelTextView);
        }

        public void bind(BACData data) {
            dateTextView.setText(data.getDate());
            timeTextView.setText(data.getTime());
            bacLevelTextView.setText(String.valueOf(data.getBacLevel()));
        }
    }
}
