package com.example.onemed;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PrescriptionAdapter extends RecyclerView.Adapter<PrescriptionAdapter.PrescriptionViewHolder> {

    private Context context;
    private List<prescription> prescriptionList;

    public PrescriptionAdapter(Context context, List<prescription> list) {
        this.context = context;
        this.prescriptionList = list;
    }

    @NonNull
    @Override
    public PrescriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prescription, parent, false);
        return new PrescriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrescriptionViewHolder holder, int position) {
        prescription p = prescriptionList.get(position);

        holder.medicineName.setText(p.getMedicineName());
        holder.dosage.setText(p.getDosage());
        holder.instruction.setText(p.getInstruction());
        holder.markTakenText.setText("Taken: " + p.getCurrentDoseCount() + "/" + p.getDoseCount());

        holder.markTaken.setOnClickListener(v -> {
            if (p.getCurrentDoseCount() >= p.getDoseCount()) {
                Toast.makeText(context, "You've reached your dose limit for today!", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Confirm Dose");
            builder.setMessage("Are you sure you've taken your dose of " + p.getMedicineName() + "?");
            builder.setPositiveButton("Yes", null);
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();

            dialog.setOnShowListener(d -> {
                MaterialButton yesButton = (MaterialButton) dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                yesButton.setEnabled(false);

                // 4-second countdown before enabling "Yes"
                new CountDownTimer(4000, 1000) {
                    int secondsRemaining = 4;

                    public void onTick(long millisUntilFinished) {
                        yesButton.setText("Yes (" + secondsRemaining-- + ")");
                    }

                    public void onFinish() {
                        yesButton.setEnabled(true);
                        yesButton.setText("Yes");
                    }
                }.start();

                // Click listener for Yes after countdown
                yesButton.setOnClickListener(btn -> {
                    int newCount = p.getCurrentDoseCount() + 1;

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    db.collection("users")
                            .document(uid)
                            .collection("prescription")
                            .document(p.getId())
                            .update("currentDoseCount", newCount)
                            .addOnSuccessListener(aVoid -> {
                                p.setCurrentDoseCount(newCount);  // Update local model
                                notifyItemChanged(holder.getAdapterPosition());  // Refresh UI
                                Toast.makeText(context, "Marked as taken: " + p.getMedicineName(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to update dose count. Please try again.", Toast.LENGTH_SHORT).show();
                            });
                });
            });

            dialog.show();
        });

        holder.refill.setOnClickListener(v ->
                Toast.makeText(context, "Refill requested for: " + p.getMedicineName(), Toast.LENGTH_SHORT).show());

        holder.callDoctor.setOnClickListener(v ->{
                Toast.makeText(context, "Calling doctor for: " + p.getMedicineName(), Toast.LENGTH_SHORT).show();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Step 1: Get appointment where istaken == true for current user
            db.collection("users")
                    .document(uid)
                    .collection("appointments")
                    .whereEqualTo("userId", uid)
                    .whereEqualTo("istaken", true)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(context, "No active appointment found.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Assuming only one active appointment is taken
                        String doctorName = queryDocumentSnapshots.getDocuments().get(0).getString("doctorName");

                        // Step 2: Fetch doctor details using doctorName
                        db.collection("doctors")
                                .whereEqualTo("docName", doctorName)
                                .get()
                                .addOnSuccessListener(doctorDocs -> {
                                    if (!doctorDocs.isEmpty()) {
                                        String docNumber = doctorDocs.getDocuments().get(0).getString("docNumber");

                                        // Step 3: Make phone call
                                        if (docNumber != null && !docNumber.isEmpty()) {
                                            Intent callIntent = new Intent(Intent.ACTION_DIAL);
                                            callIntent.setData(Uri.parse("tel:" + docNumber));
                                            context.startActivity(callIntent);
                                        } else {
                                            Toast.makeText(context, "Doctor's number not available.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(context, "Doctor information not found.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to fetch doctor info.", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to fetch appointment info.", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return prescriptionList.size();
    }

    static class PrescriptionViewHolder extends RecyclerView.ViewHolder {
        TextView medicineName, dosage, instruction, markTakenText;
        MaterialButton refill, callDoctor;

        ImageButton markTaken;

        PrescriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            medicineName = itemView.findViewById(R.id.tvMedicineName);
            dosage = itemView.findViewById(R.id.tvDosage);
            instruction = itemView.findViewById(R.id.prescription_instruction);
            markTaken = itemView.findViewById(R.id.checkmarkButton);
            refill = itemView.findViewById(R.id.request_refill);
            callDoctor = itemView.findViewById(R.id.call_doctor);
            markTakenText = itemView.findViewById(R.id.mark_taken_text);
        }
    }

    // helper function to filter in history page
    public void updateList(List<prescription> newList) {
        this.prescriptionList = newList;
        notifyDataSetChanged();
    }

}
