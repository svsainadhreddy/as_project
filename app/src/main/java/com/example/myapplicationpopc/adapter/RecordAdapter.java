package com.example.myapplicationpopc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplicationpopc.R;
import com.example.myapplicationpopc.model.RecordsResponse;

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
        holder.tvId.setText(patient.getId());        // display patient ID
        holder.tvName.setText(patient.getName());

        holder.btnNext.setOnClickListener(v -> listener.onItemClick(patient));
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvName;
        ImageView btnNext;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvName = itemView.findViewById(R.id.tvName);
            btnNext = itemView.findViewById(R.id.btnNext);
        }
    }
}
