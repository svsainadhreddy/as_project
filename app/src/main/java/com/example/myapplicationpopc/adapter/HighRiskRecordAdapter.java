package com.example.myapplicationpopc.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.myapplicationpopc.R;
import com.example.myapplicationpopc.model.RecordsResponse;

import java.util.List;

public class HighRiskRecordAdapter extends RecyclerView.Adapter<HighRiskRecordAdapter.HighRiskViewHolder> {

    private Context context;
    private List<RecordsResponse> patients;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(RecordsResponse patient);
    }

    public HighRiskRecordAdapter(Context context, List<RecordsResponse> patients, OnItemClickListener listener) {
        this.context = context;
        this.patients = patients;
        this.listener = listener;
    }

    public void setList(List<RecordsResponse> list) {
        this.patients = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HighRiskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_surveypatient, parent, false);
        return new HighRiskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HighRiskViewHolder holder, int position) {
        RecordsResponse patient = patients.get(position);
        holder.tvId.setText(patient.getId());
        holder.tvName.setText(patient.getName());
        holder.tvStatus.setText(patient.getRisk_status() != null ? patient.getRisk_status() : "N/A");

        String photoUrl = patient.getPhoto();
        String name = patient.getName() != null ? patient.getName().trim() : "";

        Log.d("HighRiskAdapter", "Patient: " + name + ", Photo URL: " + photoUrl);

        if (photoUrl != null && !photoUrl.isEmpty()) {
            holder.tvInitials.setVisibility(View.GONE);
            holder.imgPatient.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(photoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop()
                    .placeholder(R.drawable.ic_doctor_logo)
                    .error(R.drawable.ic_doctor_logo)
                    .into(holder.imgPatient);
        } else {
            holder.imgPatient.setVisibility(View.GONE);
            holder.tvInitials.setVisibility(View.VISIBLE);

            String initials = "NA";
            if (!name.isEmpty()) {
                String[] parts = name.split(" ");
                if (parts.length >= 2) {
                    initials = (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
                } else {
                    initials = name.length() >= 2
                            ? name.substring(0, 2).toUpperCase()
                            : name.substring(0, 1).toUpperCase();
                }
            }
            holder.tvInitials.setText(initials);
        }

        holder.btnNext.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(patient);
        });
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    static class HighRiskViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvName, tvInitials, tvStatus;
        ImageView btnNext, imgPatient;

        public HighRiskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvName = itemView.findViewById(R.id.tvName);
            tvStatus = itemView.findViewById(R.id.tvstatus); // from item_surveypatient.xml
            btnNext = itemView.findViewById(R.id.btnNext);
            imgPatient = itemView.findViewById(R.id.imgPatient);
            tvInitials = itemView.findViewById(R.id.tvInitials);
        }
    }
}
