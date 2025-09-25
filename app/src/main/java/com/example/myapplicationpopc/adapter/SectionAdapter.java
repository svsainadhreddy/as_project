package com.example.myapplicationpopc.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplicationpopc.R;
import com.example.myapplicationpopc.model.SurveyResponse;

import java.util.List;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionVH> {

    public static class SectionData {
        String sectionName;
        List<SurveyResponse.Answer> answers;
        SectionData(String n, List<SurveyResponse.Answer> a){ sectionName=n; answers=a; }
    }

    private List<SectionData> sections;

    public SectionAdapter(List<SectionData> sections) {
        this.sections = sections;
    }

    @NonNull
    @Override
    public SectionVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_section, parent, false);
        return new SectionVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionVH holder, int position) {
        SectionData s = sections.get(position);
        holder.tvSectionName.setText(s.sectionName);

        holder.containerQuestions.removeAllViews();
        LayoutInflater inf = LayoutInflater.from(holder.itemView.getContext());
        for (SurveyResponse.Answer a : s.answers) {
            View row = inf.inflate(R.layout.item_question, holder.containerQuestions, false);
            ((TextView)row.findViewById(R.id.tvQuestion)).setText(a.getQuestion_text());
            ((TextView)row.findViewById(R.id.tvOption)).setText(a.getSelected_option());
            ((TextView)row.findViewById(R.id.tvScore)).setText(String.valueOf(a.getOption_score()));
            holder.containerQuestions.addView(row);
        }
    }

    @Override
    public int getItemCount() { return sections.size(); }

    static class SectionVH extends RecyclerView.ViewHolder {
        TextView tvSectionName;
        LinearLayout containerQuestions;
        SectionVH(View itemView){
            super(itemView);
            tvSectionName = itemView.findViewById(R.id.tvSectionName);
            containerQuestions = itemView.findViewById(R.id.containerQuestions);
        }
    }
}
