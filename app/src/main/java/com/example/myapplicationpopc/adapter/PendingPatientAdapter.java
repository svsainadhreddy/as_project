package com.example.myapplicationpopc.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplicationpopc.*;
import com.example.myapplicationpopc.model.PendingPatient;
import com.example.myapplicationpopc.utils.SharedPrefManager;

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


        holder.tvId.setText(String.valueOf(patient.getId()));   // still show custom patient_id
        holder.tvName.setText(patient.getName());
        holder.tvStatus.setText(patient.getStatus());


        holder.btnNext.setOnClickListener(v -> {
            String status = patient.getStatus().toLowerCase();
            Intent intent = null;

            // Decide next activity based on status
            switch (status) {
                case "patient_demographics":
                case "not started":
                    intent = new Intent(context, PatientDemographicsActivity.class);
                    break;
                case "medical_history":
                    intent = new Intent(context, MedicalHistoryActivity.class);
                    break;
                case "preoperative_considerations":
                    intent = new Intent(context, PreoperativeConsiderationsActivity.class);
                    break;
                case "surgery_factors":
                    intent = new Intent(context, SurgeryFactorsActivity.class);
                    break;
                case "planned_anesthesia":
                    intent = new Intent(context, PlannedAnesthesiaActivity.class);
                    break;
                case "postoperative":
                    intent = new Intent(context, PostoperativeActivity.class);
                    break;
            }

            // Inside onBindViewHolder -> btnNext click
            if (intent != null) {
                intent.putExtra("patient_id", patient.getPk());   // ✅ send DB pk
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
        TextView tvId, tvName, tvStatus;
        ImageView btnNext, imgPhoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvName = itemView.findViewById(R.id.tvName);
            tvStatus = itemView.findViewById(R.id.tvstaus);
            btnNext = itemView.findViewById(R.id.btnNext);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
        }
    }
}
