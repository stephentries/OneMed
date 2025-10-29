package com.example.onemed;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentHistoryAdapter extends RecyclerView.Adapter<AppointmentHistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<appointment> appointmentList;
    private FirebaseFirestore firestore;

    public AppointmentHistoryAdapter(Context context, List<appointment> list) {
        this.context = context;
        this.appointmentList = list;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.history_item_appointment, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        appointment appt = appointmentList.get(position);

        holder.tvDoctorName.setText(appt.getDoctorName());
        holder.tvDateTime.setText(appt.getDate() + " at " + appt.getTime());
        holder.tvSpecialty.setText(appt.getSpecialty());
        holder.hospitalInfo.setText(appt.getHospital());
        holder.docImage.setImageResource(getDoctorImageResource(appt.getDoctorName()));
        holder.reviewContainer.setVisibility(View.GONE);
        TextInputLayout inputLayout = holder.itemView.findViewById(R.id.reviewInputLayout);

// Force hint color to black
        inputLayout.setDefaultHintTextColor(ColorStateList.valueOf(Color.BLACK));


        // Reset button states in case of view recycling
        holder.reviewInput.setEnabled(true);
        holder.ratingBar.setIsIndicator(false);
        holder.sendReviewBtn.setEnabled(true);
        holder.sendReviewBtn.setText("Send Review");
        holder.rateReviewToggle.setText("Rate and Review");


        String apptId = appt.getId();
        String userId = appt.getUserId(); // or get from FirebaseAuth

        firestore.collection("reviews")
                .whereEqualTo("appointmentId", apptId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Existing review found
                        Review review = querySnapshot.getDocuments().get(0).toObject(Review.class);

                        if (review != null) {
                            holder.reviewInput.setText(review.getReview());
                            holder.ratingBar.setRating(review.getRating());

                            // Hide hint if review text exists
                            if (!TextUtils.isEmpty(review.getReview())) {
                                inputLayout.setHint(null);  // hides the hint
                            }

                            // Disable editing
                            holder.reviewInput.setEnabled(false);
                            holder.ratingBar.setIsIndicator(true);
                            holder.sendReviewBtn.setEnabled(false);
                            holder.sendReviewBtn.setText("You Have Submitted a Review");

                            // Toggle to Collapse
                            holder.rateReviewToggle.setText("Collapse Review");

                            // Show review container by default
                            holder.reviewContainer.setVisibility(View.VISIBLE);

                            // Optional: Collapse/expand behavior
                            holder.rateReviewToggle.setOnClickListener(v -> {
                                if (holder.reviewContainer.getVisibility() == View.GONE) {
                                    holder.reviewContainer.setVisibility(View.VISIBLE);
                                    holder.rateReviewToggle.setText("Collapse Review");
                                } else {
                                    holder.reviewContainer.setVisibility(View.GONE);
                                    holder.rateReviewToggle.setText("Your Review");
                                }
                            });
                        }
                    } else {
                        // No review found - allow user to submit
                        holder.rateReviewToggle.setText("Rate and Review");

                        holder.rateReviewToggle.setOnClickListener(v -> {
                            if (holder.reviewContainer.getVisibility() == View.GONE) {
                                holder.reviewContainer.setVisibility(View.VISIBLE);
                                holder.rateReviewToggle.setText("Hide Rate and Review");
                            } else {
                                holder.reviewContainer.setVisibility(View.GONE);
                                holder.rateReviewToggle.setText("Rate and Review");
                            }
                        });

                        holder.sendReviewBtn.setOnClickListener(v -> {
                            float ratingValue = holder.ratingBar.getRating();
                            String reviewText = holder.reviewInput.getText() != null ?
                                    holder.reviewInput.getText().toString().trim() : "";

                            if (ratingValue == 0f) {
                                Toast.makeText(context, "Please provide a rating.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (TextUtils.isEmpty(reviewText)) {
                                Toast.makeText(context, "Please write a review.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Review newReview = new Review(
                                    apptId,
                                    userId,
                                    appt.getDoctorName(),
                                    ratingValue,
                                    reviewText,
                                    System.currentTimeMillis()
                            );

                            firestore.collection("reviews")
                                    .add(newReview)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(context, "Review submitted!", Toast.LENGTH_SHORT).show();
                                        notifyItemChanged(position); // Re-bind to reflect review UI
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to submit review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to load review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Book Again button
        holder.reschedule.setOnClickListener(v -> {
            Toast.makeText(context, "Booking another appointment with Dr. " + appt.getDoctorName(), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(context, BookAppointment_activity.class);
            intent.putExtra("SelectedDocName", appt.getDoctorName());
            intent.putExtra("isRescheduleFlow", true);

            // Needed if using application context (e.g., in Adapter)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
        });

    }


    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public void updateList(List<appointment> newList) {
        this.appointmentList = newList;
        notifyDataSetChanged();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {

        TextView tvDoctorName, tvDateTime, tvSpecialty, hospitalInfo;
        ImageView docImage;
        MaterialButton reschedule, rateReviewToggle, sendReviewBtn;
        View reviewContainer;
        RatingBar ratingBar;
        TextInputEditText reviewInput;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvSpecialty = itemView.findViewById(R.id.Doc_specialty);
            hospitalInfo = itemView.findViewById(R.id.hospital_info);
            docImage = itemView.findViewById(R.id.doc_image);
            reschedule = itemView.findViewById(R.id.reschedule2);
            rateReviewToggle = itemView.findViewById(R.id.RateReview_toggle);
            reviewContainer = itemView.findViewById(R.id.review_container);
            ratingBar = itemView.findViewById(R.id.rate_appt);
            reviewInput = itemView.findViewById(R.id.review_input);
            sendReviewBtn = itemView.findViewById(R.id.btn_send_review);
        }
    }

    private int getDoctorImageResource(String doctorName) {
        switch (doctorName.toLowerCase()) {
            case "dr. al fazir omar":
                return R.drawable.dr_al_fazir_omar;
            case "dr. bong jan ling":
                return R.drawable.dr_bong_jan_ling;
            case "dr. balraj singh":
                return R.drawable.dr_balraj_singh_jatkaram_singh;
            case "dr. anita kaur ahluwalia":
                return R.drawable.dr_anita_kaur;
            default:
                return R.drawable.thomson; // fallback doctor image
        }
    }
}
