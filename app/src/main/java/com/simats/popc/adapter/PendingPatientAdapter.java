package com.simats.popc.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.simats.popc.PatientDemographicsActivity;
import com.simats.popc.MedicalHistoryActivity;
import com.simats.popc.PreoperativeConsiderationsActivity;
import com.simats.popc.ScoreActivity;
import com.simats.popc.SurgeryFactorsActivity;
import com.simats.popc.PlannedAnesthesiaActivity;
import com.simats.popc.PostoperativeActivity;
import com.simats.popc.R;
import com.simats.popc.model.PendingPatient;
import com.simats.popc.utils.SharedPrefManager;

import java.util.List;

public class PendingPatientAdapter extends RecyclerView.Adapter<PendingPatientAdapter.ViewHolder> {

    private List<PendingPatient> patients;
    private Context context;

    public PendingPatientAdapter(Context context, List<PendingPatient> patients) {
        this.context = context;
        this.patients = patients;
    }

    public void updateData(List<PendingPatient> newList) {
        patients = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_surveypatient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PendingPatient patient = patients.get(position);
        // Bind status
        String status = patient.getStatus() != null ? patient.getStatus() : "Not Started";
        holder.tvStatus.setText(status);
        holder.tvId.setText(patient.getId());
        holder.tvName.setText(patient.getName());

        String photoUrl = patient.getPhotoUrl();
        String name = patient.getName() != null ? patient.getName().trim() : "";

        // Load image if exists
        if (photoUrl != null && !photoUrl.isEmpty()) {
            holder.imgPhoto.setVisibility(View.VISIBLE);
            holder.tvInitials.setVisibility(View.GONE);

            Glide.with(context)
                    .load(photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_user) // fallback placeholder
                    .error(R.drawable.ic_user)
                    .into(holder.imgPhoto);

        } else {
            // Show initials if no image
            holder.imgPhoto.setVisibility(View.GONE);
            holder.tvInitials.setVisibility(View.VISIBLE);

            String initials = "NA";
            if (!name.isEmpty()) {
                String[] parts = name.split(" ");
                if (parts.length > 0 && parts[0].length() >= 2) {
                    initials = parts[0].substring(0, 2).toUpperCase();
                } else if (name.length() >= 2) {
                    initials = name.substring(0, 2).toUpperCase();
                } else if (name.length() == 1) {
                    initials = name.substring(0, 1).toUpperCase();
                }
            }
            holder.tvInitials.setText(initials);
        }

        // Handle next button click
        holder.btnNext.setOnClickListener(v -> {
            Intent intent = null;

            switch (status) {
                case "patient_Demographics":
                    intent = new Intent(context, MedicalHistoryActivity.class);
                    break;
                case "Not Started":
                    intent = new Intent(context, PatientDemographicsActivity.class);
                    break;
                case "medical_history":
                    intent = new Intent(context, PreoperativeConsiderationsActivity.class);
                    break;
                case "preoperative_considerations":
                    intent = new Intent(context, SurgeryFactorsActivity.class);
                    break;
                case "surgery_Factors":
                    intent = new Intent(context, PlannedAnesthesiaActivity.class);
                    break;
                case "planned_Anesthesia":
                    intent = new Intent(context, PostoperativeActivity.class);
                    break;
                case "postoperative":
                    intent = new Intent(context, ScoreActivity.class);
                    break;
            }

            if (intent != null) {
                intent.putExtra("patient_id", patient.getPk());
                intent.putExtra("auth_token", "Token " + SharedPrefManager.getInstance(context).getToken());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvName, tvInitials,tvStatus;
        ImageView btnNext, imgPhoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvName = itemView.findViewById(R.id.tvName);
            tvStatus = itemView.findViewById(R.id.tvstatus);
            btnNext = itemView.findViewById(R.id.btnNext);
            imgPhoto = itemView.findViewById(R.id.imgPatient);
            tvInitials = itemView.findViewById(R.id.tvInitials);
        }
    }
}
