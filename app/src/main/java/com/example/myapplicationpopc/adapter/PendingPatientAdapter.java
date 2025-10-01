package com.example.myapplicationpopc.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplicationpopc.R;
import com.example.myapplicationpopc.model.PendingPatient;

import java.util.List;

public class PendingPatientAdapter extends RecyclerView.Adapter<PendingPatientAdapter.ViewHolder> {
    private List<PendingPatient> patients;

    public PendingPatientAdapter(List<PendingPatient> patients) {
        this.patients = patients;
    }

    public void updateData(List<PendingPatient> newList) {
        patients = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_surveypatient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PendingPatient patient = patients.get(position);
        holder.tvId.setText(String.valueOf(patient.getId()));
        holder.tvName.setText(patient.getName());
        holder.tvStatus.setText(patient.getStatus());
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvName, tvStatus;
        ImageView btnNext;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvName = itemView.findViewById(R.id.tvName);
            tvStatus = itemView.findViewById(R.id.tvstaus);
            btnNext = itemView.findViewById(R.id.btnNext);
        }
    }
}
