package com.example.onemed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PrescriptionHistoryAdapter extends RecyclerView.Adapter<PrescriptionHistoryAdapter.PrescriptionViewHolder> {

    private Context context;
    private List<prescription> prescriptionList;

    public PrescriptionHistoryAdapter(Context context, List<prescription> prescriptionList) {
        this.context = context;
        this.prescriptionList = prescriptionList;
    }

    @NonNull
    @Override
    public PrescriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.history_item_prescription, parent, false);
        return new PrescriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrescriptionViewHolder holder, int position) {
        prescription p = prescriptionList.get(position);

        holder.tvMedicineName.setText(p.getMedicineName());
        holder.tvDosage.setText(p.getDosage());
        holder.tvInstruction.setText(p.getInstruction());

        holder.requestRefill.setOnClickListener(v ->
                Toast.makeText(context, "Requesting refill for " + p.getMedicineName(), Toast.LENGTH_SHORT).show());

        holder.callDoctor.setOnClickListener(v ->
                Toast.makeText(context, "Calling doctor for " + p.getMedicineName(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return prescriptionList != null ? prescriptionList.size() : 0;
    }

    public void updateList(List<prescription> newList) {
        this.prescriptionList = newList;
        notifyDataSetChanged();
    }

    static class PrescriptionViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicineName, tvDosage, tvInstruction;
        MaterialButton requestRefill, callDoctor;

        public PrescriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvInstruction = itemView.findViewById(R.id.prescription_instruction);
            requestRefill = itemView.findViewById(R.id.request_refill);
            callDoctor = itemView.findViewById(R.id.call_doctor);
        }
    }
}
