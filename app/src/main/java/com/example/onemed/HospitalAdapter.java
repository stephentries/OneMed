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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {

    private Context context;
    private List<Hospital> hospitalList;

    public HospitalAdapter(Context context, List<Hospital> hospitalList) {
        this.context = context;
        this.hospitalList = hospitalList;
    }

    @NonNull
    @Override
    public HospitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hospital_directory, parent, false);
        return new HospitalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HospitalViewHolder holder, int position) {
        Hospital hospital = hospitalList.get(position);

        holder.hospitalName.setText(hospital.getHospitalname());
        holder.hospitalRating.setRating(hospital.getRating());

        // Image loading - replace this with Glide/Picasso if you're loading from URL
        int imageResId = context.getResources().getIdentifier(
                hospital.getHospital_Img(), "drawable", context.getPackageName()
        );

        if (imageResId != 0) {
            holder.hospitalImage.setImageResource(imageResId);
        } else {
            holder.hospitalImage.setImageResource(R.drawable.thomson);
        }
        holder.hospitalImage.setOnClickListener(v->{
            Intent intent = new Intent(context, hospital_directory_activity.class);
            intent.putExtra("hospitalName", hospital.getHospitalname());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return hospitalList.size();
    }

    public static class HospitalViewHolder extends RecyclerView.ViewHolder {
        ImageView hospitalImage;
        TextView hospitalName;
        RatingBar hospitalRating;

        public HospitalViewHolder(@NonNull View itemView) {
            super(itemView);
            hospitalImage = itemView.findViewById(R.id.hospitalImage);
            hospitalName = itemView.findViewById(R.id.HospitalName); // or R.id.hospital_name if renamed
            hospitalRating = itemView.findViewById(R.id.hospital_rating);
        }
    }

}
