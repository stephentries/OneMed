package com.example.onemed;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.stream.Collectors;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private Context context;
    private List<doctor> doctorList;
    private DocClickListener listener;

    private int layoutResId;

    private int selectedPosition = RecyclerView.NO_POSITION;

    String[] dayNames = {
            "",            // 0
            "Sunday",      // 1
            "Monday",      // 2
            "Tuesday",     // 3
            "Wednesday",   // 4
            "Thursday",    // 5
            "Friday",      // 6
            "Saturday"     // 7
    };

    public DoctorAdapter(Context context, List<doctor> doctorList, DocClickListener listener, int layoutResId) {
        this.context = context;
        this.doctorList = doctorList;
        this.listener = listener;
        this.layoutResId = layoutResId;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layoutResId, parent, false);
        return new DoctorViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        doctor doc = doctorList.get(position);

        Log.d("DoctorAdapter", "Binding doctor at position " + position + ": " + doc.getDocName());

        holder.doctorName.setText(doc.getDocName());
        holder.specialty.setText(doc.getSpecialty());

        // Hospital info - safe join
        if (doc.getHospitals() != null && !doc.getHospitals().isEmpty()) {
            holder.hospitalInfo.setText(String.join(", ", doc.getHospitals()));
        } else {
            holder.hospitalInfo.setText("N/A");
        }

        // Load doctor image from drawable resources using doctorImg name
        int imageResId = context.getResources().getIdentifier(
                doc.getDoctorImg(), "drawable", context.getPackageName());
        if (imageResId != 0) {
            holder.doctorImage.setImageResource(imageResId);
        } else {
            holder.doctorImage.setImageResource(R.drawable.thomson);  // fallback image
        }

        // Available days converted to names
        if (doc.getAvailableDays() != null && !doc.getAvailableDays().isEmpty()) {
            String days = doc.getAvailableDays().stream()
                    .filter(d -> d >= 1 && d <= 7)
                    .map(d -> dayNames[d])
                    .collect(Collectors.joining(", "));
            holder.availableDays.setText(days);
        } else {
            holder.availableDays.setText("N/A");
        }

        // Available time slots joined
        if (doc.getAvailableTimeSlots() != null && !doc.getAvailableTimeSlots().isEmpty()) {
            holder.availableTimeSlot.setText(String.join("  ", doc.getAvailableTimeSlots()));
        } else {
            holder.availableTimeSlot.setText("N/A");
        }

        // Languages spoken joined or N/A
        if (doc.getLanguages() != null && !doc.getLanguages().isEmpty()) {
            holder.language.setText(String.join(", ", doc.getLanguages()));
        } else {
            holder.language.setText("N/A");
        }

        // Doctor number (docNumber) binding
        holder.docNumber.setText(doc.getDocNumber() != null ? doc.getDocNumber() : "N/A");

        // RatingBar setup
        holder.docRating.setRating(doc.getRating());

        // Card background color for selected item
        if (position == selectedPosition) {
            holder.doctorCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.selected_card));
        } else {
            holder.doctorCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.default_card));
        }

        // Click listener for selection/deselection
        // Set itemView click listener
        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            doctor currentDoc = doctorList.get(currentPosition);

            if (selectedPosition == currentPosition) {
                int previousPosition = selectedPosition;
                selectedPosition = RecyclerView.NO_POSITION;
                notifyItemChanged(previousPosition);
                Toast.makeText(context, "Deselected " + currentDoc.getDocName(), Toast.LENGTH_SHORT).show();
                if (listener != null) listener.onDoctorSelected(null);
            } else {
                int previousPosition = selectedPosition;
                selectedPosition = currentPosition;
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedPosition);
                Toast.makeText(context, "Clicked on " + currentDoc.getDocName(), Toast.LENGTH_SHORT).show();
                if (listener != null) listener.onDoctorSelected(currentDoc);
            }
        });

// Set Book Doctor button click listener independently
        if (holder.bookDoctorBtn != null) {
            holder.bookDoctorBtn.setOnClickListener(view -> {
                Toast.makeText(context, "Book " + doc.getDocName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context,BookAppointment_activity.class);
                intent.putExtra("SelectedDocName", doc.getDocName());
                intent.putExtra("isRescheduleFlow", false);
                context.startActivity(intent);
                if (listener != null) {
                    listener.onDoctorBookClicked(doc);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return doctorList != null ? doctorList.size() : 0;
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView doctorName, specialty, hospitalInfo, availableDays, availableTimeSlot, language, docNumber;
        RatingBar docRating;
        ImageView doctorImage;
        CardView doctorCard;

        MaterialButton bookDoctorBtn;



        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorName = itemView.findViewById(R.id.DoctorName);
            specialty = itemView.findViewById(R.id.specialty);
            hospitalInfo = itemView.findViewById(R.id.hospital_list);
            doctorImage = itemView.findViewById(R.id.DoctorPic);
            doctorCard = itemView.findViewById(R.id.doctor_card);
            availableDays = itemView.findViewById(R.id.availableDays);
            availableTimeSlot = itemView.findViewById(R.id.availableTimeSlot);
            language = itemView.findViewById(R.id.language);
            docNumber = itemView.findViewById(R.id.doc_number);
            docRating = itemView.findViewById(R.id.Doc_rating);
            try {
                bookDoctorBtn = itemView.findViewById(R.id.book_doctor);
            } catch (Exception e) {
                bookDoctorBtn = null;
            }
        }
    }

    public void updateList(List<doctor> newDoctors) {
        this.doctorList = newDoctors;
        notifyDataSetChanged();  // important to refresh RecyclerView UI
    }

    public static class DoctorDiffCallback extends DiffUtil.Callback {
        private final List<doctor> oldList;
        private final List<doctor> newList;

        public DoctorDiffCallback(List<doctor> oldList, List<doctor> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getDocName()
                    .equals(newList.get(newItemPosition).getDocName());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            doctor oldDoc = oldList.get(oldItemPosition);
            doctor newDoc = newList.get(newItemPosition);

            return oldDoc.getDocName().equals(newDoc.getDocName())
                    && oldDoc.getSpecialty().equals(newDoc.getSpecialty())
                    && oldDoc.getHospitals().equals(newDoc.getHospitals())
                    && oldDoc.getDoctorImg().equals(newDoc.getDoctorImg());
        }
    }

    public interface DocClickListener {
        void onDoctorSelected(doctor selectedDoctor);
        void onDoctorBookClicked(doctor doc);
    }
    public void setSelectedDoctor(String doctorName) {
        int previousPosition = selectedPosition;
        selectedPosition = -1;

        for (int i = 0; i < doctorList.size(); i++) {
            if (doctorList.get(i).getDocName().equalsIgnoreCase(doctorName)) {
                selectedPosition = i;
                break;
            }
        }

        if (previousPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousPosition);
        }
        if (selectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPosition);
        }
    }

}
