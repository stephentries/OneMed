package com.example.onemed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class MedicalRecordAdapter extends RecyclerView.Adapter<MedicalRecordAdapter.ViewHolder> {

    private Context context;
    private List<MedicalRecord> recordList;

    public MedicalRecordAdapter(Context context, List<MedicalRecord> recordList) {
        this.context = context;
        this.recordList = recordList;
    }

    public void filterList(List<MedicalRecord> filteredList) {
        this.recordList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MedicalRecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medical_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicalRecordAdapter.ViewHolder holder, int position) {
        MedicalRecord record = recordList.get(position);
        holder.title.setText(record.getRecordtitle());
        holder.description.setText(record.getDescription());

        if(record.getIsOfficial()){
            holder.date.setText("Date Received:" + record.getRecordDate());
        }
        else{
            holder.date.setText("Date Uploaded:" + record.getRecordDate());
        }
        if (record.getVerified()) {
            holder.verified.setText("Verified by " + record.getVerifiedBy());
            holder.request_verification.setVisibility(View.GONE);
        } else {
            holder.checkmarkButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, date, verified;
        ImageButton checkmarkButton;
        MaterialButton request_verification,download_files;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.RecordTitle);
            description = itemView.findViewById(R.id.recordDescription);
            date = itemView.findViewById(R.id.recordDate);
            verified = itemView.findViewById(R.id.verified_button_text);
            checkmarkButton=itemView.findViewById(R.id.checkmarkButton);
            request_verification=itemView.findViewById(R.id.request_verification);
            download_files=itemView.findViewById(R.id.download_files);
        }
    }
}
