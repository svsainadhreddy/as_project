package com.example.myapplicationpopc.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplicationpopc.R;
import com.example.myapplicationpopc.model.PatientResponse;

import java.util.List;

public class DeletePatientAdapter extends RecyclerView.Adapter<DeletePatientAdapter.PatientViewHolder> {

    private Context context;
    private List<PatientResponse> patientList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(PatientResponse patient);
    }

    public DeletePatientAdapter(Context context, List<PatientResponse> patientList, OnItemClickListener listener) {
        this.context = context;
        this.patientList = patientList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.deleteitem_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        PatientResponse patient = patientList.get(position);
        holder.tvId.setText(patient.getPatientId());
        holder.tvName.setText(patient.getName());

        // Load image or generate initials drawable
        if (patient.getPhotoUrl() != null && !patient.getPhotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(patient.getPhotoUrl())
                    .circleCrop()
                    .placeholder(R.drawable.ic_img)
                    .error(R.drawable.ic_img)
                    .into(holder.imgPatient);
        } else {
            // Generate initials drawable
            holder.imgPatient.setImageBitmap(generateInitialsDrawable(patient.getName()));
        }

        holder.tvDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(patient);
        });
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    public void setList(List<PatientResponse> list) {
        patientList = list;
        notifyDataSetChanged();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPatient;
        TextView tvId, tvName, tvDelete;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPatient = itemView.findViewById(R.id.imgPatient);
            tvId = itemView.findViewById(R.id.tvId);
            tvName = itemView.findViewById(R.id.tvName);
            tvDelete = itemView.findViewById(R.id.tvDelete);
        }
    }

    private Bitmap generateInitialsDrawable(String name) {
        String initials = "NA";
        if (name != null && name.length() >= 2) {
            initials = name.substring(0, 2).toUpperCase();
        } else if (name != null && name.length() == 1) {
            initials = name.substring(0, 1).toUpperCase();
        }

        int size = 150; // image size in px
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Background circle
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#FFBB86FC")); // random pastel color
        canvas.drawCircle(size / 2, size / 2, size / 2, paint);

        // Text paint
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(size / 2);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Draw text in center
        float yPos = (canvas.getHeight() / 2 - (textPaint.descent() + textPaint.ascent()) / 2);
        canvas.drawText(initials, size / 2, yPos, textPaint);

        return bitmap;
    }
}
