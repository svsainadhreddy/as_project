package com.simats.popc.adapter;

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
import com.simats.popc.R;
import com.simats.popc.model.RecordsResponse;

import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.PatientViewHolder> {

    private Context context;
    private List<RecordsResponse> patients;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(RecordsResponse patient);
    }

    public RecordAdapter(Context context, List<RecordsResponse> patients, OnItemClickListener listener) {
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
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        RecordsResponse patient = patients.get(position);
        holder.tvId.setText(patient.getId());
        holder.tvName.setText(patient.getName());

        String photoUrl = patient.getPhoto();
        String name = patient.getName() != null ? patient.getName().trim() : "";

        // Debug log
        Log.d("RecordAdapter", "Patient: " + name + ", Photo URL: " + photoUrl);

        if (photoUrl != null && !photoUrl.isEmpty()) {
            // Show image, hide initials
            holder.tvInitials.setVisibility(View.GONE);
            holder.imgPatient.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(photoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop()
                    .placeholder(R.drawable.ic_user) // fallback while loading
                    .error(R.drawable.ic_user)       // fallback if failed
                    .into(holder.imgPatient);
        } else {
            // Show initials if no image
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

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvName, tvInitials;
        ImageView btnNext, imgPatient;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvName = itemView.findViewById(R.id.tvName);
            btnNext = itemView.findViewById(R.id.btnNext);
            imgPatient = itemView.findViewById(R.id.imgPatient);
            tvInitials = itemView.findViewById(R.id.tvInitials);
        }
    }
}
