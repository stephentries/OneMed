package com.example.onemed;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private static final int View_type_available = 0;
    private static final int View_type_null = 1;
    private Map<String, Hospital> hospitalCache = new HashMap<>();

    private Context context;
    private List<appointment> appointmentList;

    public AppointmentAdapter(Context context, List<appointment> list) {
        this.context = context;
        this.appointmentList = list;
    }

    @Override
    public int getItemViewType(int position) {
        if (appointmentList == null || appointmentList.isEmpty()) {
            return View_type_null;


        } else {
            return View_type_available;
        }
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == View_type_null) {
            view = LayoutInflater.from(context).inflate(R.layout.no_appointment, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        }
        return new AppointmentViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        if (getItemViewType(position) == View_type_null) {
            holder.bookNewAppointment.setOnClickListener(v ->
                    Toast.makeText(context, "Booking an appointment", Toast.LENGTH_SHORT).show());
            return;
        }

        // Bind appointment data
        appointment a = appointmentList.get(position);
        holder.doctorName.setText(a.getDoctorName());
        holder.dateTime.setText(a.getDate() + " at " + a.getTime());
        holder.specialty.setText(a.getSpecialty());
        holder.hospitalInfo.setText(a.getHospital());
        int imageRes = getHospitalImageResource(a.getHospital());
        holder.hospitalImage.setImageResource(imageRes);

        // Set reschedule button (immediate, since not tied to hospital)
        holder.reschedule.setOnClickListener(v ->{
                Toast.makeText(context, "Rescheduling appointment with " + a.getDoctorName(), Toast.LENGTH_SHORT).show();
                Intent intent= new Intent(context,BookAppointment_activity.class);
                intent.putExtra("SelectedDocName",a.getDoctorName());
                intent.putExtra("isRescheduleFlow", true);
                intent.putExtra("appointmentId",a.getId());

            context.startActivity(intent);
        });

        // Handle hospital info (phone + location)
        if (hospitalCache.containsKey(a.getHospital())) {
            setupHospitalButtons(holder, hospitalCache.get(a.getHospital()));
        } else {
            FirebaseFirestore.getInstance()
                    .collection("Hospital")
                    .whereEqualTo("hospitalname", a.getHospital())
                    .limit(1)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            Hospital hospital = querySnapshot.getDocuments().get(0).toObject(Hospital.class);
                            if (hospital != null) {
                                hospitalCache.put(a.getHospital(), hospital);
                                setupHospitalButtons(holder, hospital);
                            }
                        } else {
                            Toast.makeText(context, "Hospital not found in database", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to load hospital data", Toast.LENGTH_SHORT).show());
        }
    }


    @Override
    public int getItemCount() {
        return (appointmentList == null || appointmentList.isEmpty()) ? 1 : appointmentList.size();
    }


    static class AppointmentViewHolder extends RecyclerView.ViewHolder {

        // Fields for appointment item
        TextView doctorName, dateTime, specialty,hospitalInfo;
        ImageView hospitalImage;
        MaterialButton reschedule, callHospital, viewInMap, viewAllAppointments;

        // Field for empty view
        MaterialButton bookNewAppointment;

        AppointmentViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);

            if (viewType == View_type_available) {
                // Regular appointment item
                doctorName = itemView.findViewById(R.id.tvDoctorName);
                dateTime = itemView.findViewById(R.id.tvDateTime);
                hospitalInfo = itemView.findViewById(R.id.hospital_info);
                specialty=itemView.findViewById(R.id.Doc_specialty);
                hospitalImage = itemView.findViewById(R.id.hospitalImage);
                reschedule = itemView.findViewById(R.id.reschedule);
                callHospital = itemView.findViewById(R.id.call_hospital);
                viewInMap = itemView.findViewById(R.id.view_in_map);

            } else {
                // Empty state layout
                bookNewAppointment = itemView.findViewById(R.id.book_new_appt);
            }
        }
    }

    // helper function to filter in history page
    public void updateList(List<appointment> newList) {
        this.appointmentList = newList;
        notifyDataSetChanged();
    }
    private int getHospitalImageResource(String hospitalName) {
        switch (hospitalName.toLowerCase()) {
            case "thomson hospital kota damansara":
                return R.drawable.thomson;
            case "sunway medical centre":
                return R.drawable.sunway_medical_centre;
            case "pantai hospital kuala lumpur":
                return R.drawable.pantai_hospital_kl;
            case "gleneagles hospital kuala lumpur":
                return R.drawable.gleneagles_hospital_kl;
            default:
                return R.drawable.thomson; // fallback image
        }
    }
        private void setupHospitalButtons(AppointmentViewHolder holder, Hospital hospital) {
            holder.callHospital.setOnClickListener(v -> {
                String phone = hospital.getPhoneNumber();
                Toast.makeText(context, "Calling: " + phone, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                context.startActivity(intent);

            });

            holder.viewInMap.setOnClickListener(v -> {
                GeoPoint location = hospital.getLocation();
                if (location != null) {
                    String uri = "geo:" + location.getLatitude() + "," + location.getLongitude() +
                            "?q=" + Uri.encode(hospital.getHospitalname());
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    mapIntent.setPackage("com.google.android.apps.maps"); // Optional: force Google Maps
                    context.startActivity(mapIntent);
                } else {
                    Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show();
                }
            });
        }



    }
