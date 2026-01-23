package com.simats.popc;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.simats.popc.model.PatientRef;
import com.simats.popc.model.SurveyDisplayResponse;
import com.simats.popc.network.ApiClient;
import com.simats.popc.network.ApiService;
import com.simats.popc.utils.SharedPrefManager;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SurveyDisplayActivity extends AppCompatActivity {

    private LinearLayout llSections;
    private TextView tvRiskLevel, tvRiskLevelSub, tvRiskBadge;

    private Button btnDone;
    private ApiService apiService;
    private int patientId;
    private SurveyDisplayResponse surveyData;
    private ArrayList<PatientRef> allPatients;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_display);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        llSections = findViewById(R.id.llSections);
        btnDone = findViewById(R.id.btnDone);
        tvRiskLevel = findViewById(R.id.tvRiskLevel);
        tvRiskLevelSub = findViewById(R.id.tvRiskLevelSub);
        tvRiskBadge = findViewById(R.id.tvRiskBadge);

        apiService = ApiClient.getClient().create(ApiService.class);
        patientId = getIntent().getIntExtra("patient_id", -1);
        allPatients = (ArrayList<PatientRef>)
                getIntent().getSerializableExtra("all_patients");

        if (patientId <= 0) {
            Toast.makeText(this, "Invalid patient id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnDone.setOnClickListener(v -> finish());
        checkStoragePermission();
        fetchSurvey();
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1001
                );
            }
        }
    }

    private void fetchSurvey() {
        String savedToken = SharedPrefManager.getInstance(this).getToken();
        String authToken = "Token " + savedToken;

        apiService.getSurveyByPatient(authToken, patientId)
                .enqueue(new Callback<SurveyDisplayResponse>() {
                    @Override
                    public void onResponse(Call<SurveyDisplayResponse> call, Response<SurveyDisplayResponse> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            surveyData = resp.body();
                            renderSurvey(surveyData);
                        } else {
                            showSurveyNotCompleted();
                        }
                    }

                    @Override
                    public void onFailure(Call<SurveyDisplayResponse> call, Throwable t) {
                        showSurveyNotCompleted();
                    }
                });
    }

    private void renderSurvey(SurveyDisplayResponse survey) {
        llSections.removeAllViews();

        // --- Risk level header ---
        String level = getRiskLevel(survey.getTotal_score());
        tvRiskLevel.setText("Risk Level: " + level);

        switch (level) {
            case "High":
                tvRiskBadge.setText("High");
                tvRiskBadge.setBackgroundResource(R.drawable.bg_badge_orange);
                tvRiskLevelSub.setText("Prefer regional if feasible, strict lung-protective strategy, consider postoperative ICU/HDU.");
                break;
            case "Very High":
                tvRiskBadge.setText("Very High");
                tvRiskBadge.setBackgroundResource(R.drawable.bg_card_red);
                tvRiskLevelSub.setText("Strongly consider avoiding GA/ETT if possible; optimize comorbidities pre-op, mandatory ICU planning.");
                break;
            case "Moderate":
                tvRiskBadge.setText("Moderate");
                tvRiskBadge.setBackgroundResource(R.drawable.bg_segment_selected);
                tvRiskLevelSub.setText("Lung-protective ventilation, multimodal analgesia, encourage early mobilization.");
                break;
            default:
                tvRiskBadge.setText(level);
                tvRiskBadge.setBackgroundResource(R.drawable.card_section_bg);
                tvRiskLevelSub.setText("Standard anesthesia; routine monitoring.");
        }

        // --- Render each section ---
        for (SurveyDisplayResponse.SectionScore sec : survey.getSection_scores()) {
            String sectionName = sec.getSection();
            int sectionScore = sec.getScore();

            LinearLayout sectionCard = new LinearLayout(this);
            sectionCard.setOrientation(LinearLayout.VERTICAL);
            sectionCard.setBackgroundResource(R.drawable.bg_risk_card);
            sectionCard.setPadding(16, 16, 16, 30);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 40);
            sectionCard.setLayoutParams(cardParams);

            // Header
            LinearLayout header = new LinearLayout(this);
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvHeader = new TextView(this);
            tvHeader.setText(sectionName);
            tvHeader.setTextSize(17f);
            tvHeader.setTextColor(0xFF000000);
            tvHeader.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvScore = new TextView(this);
            tvScore.setText(String.valueOf(sectionScore));
            tvScore.setTextSize(13f);
            tvScore.setTextColor(0xFF000000);
            tvScore.setPadding(16, 6, 16, 6);
            tvScore.setBackgroundResource(R.drawable.bg_badge_gray);
            tvScore.setGravity(Gravity.CENTER);

            ImageButton btnDownload = new ImageButton(this);
            btnDownload.setImageResource(android.R.drawable.stat_sys_download);
            btnDownload.setColorFilter(Color.BLACK);
            btnDownload.setBackgroundColor(Color.TRANSPARENT);
            LinearLayout.LayoutParams dlParams = new LinearLayout.LayoutParams(80, 80);
            dlParams.setMargins(25, 0, 25, 0);
            btnDownload.setLayoutParams(dlParams);
            btnDownload.setOnClickListener(v -> showDownloadOptions(sectionName, false));

            TextView tvArrow = new TextView(this);
            tvArrow.setText("\u25BC");
            tvArrow.setTextSize(20f);
            tvArrow.setPadding(5, 0, 0, 0);
            tvArrow.setTextColor(0xFF000000);

            header.addView(tvHeader);
            header.addView(tvScore);
            header.addView(btnDownload);
            header.addView(tvArrow);
            sectionCard.addView(header);

            TableLayout table = new TableLayout(this);
            table.setVisibility(View.GONE);
            table.setStretchAllColumns(true);
            table.setPadding(0, 16, 0, 8);

            for (SurveyDisplayResponse.Answer ans : survey.getAnswers()) {
                if (ans != null && ans.getSectionName() != null &&
                        ans.getSectionName().equalsIgnoreCase(sectionName)) {

                    String value = ans.getSelected_option();
                    if (value == null || value.isEmpty()) value = ans.getCustom_text();

                    TableRow row = new TableRow(this);

                    TextView tvQ = new TextView(this);
                    tvQ.setText(ans.getQuestion());
                    tvQ.setPadding(0, 8, 8, 8);
                    tvQ.setTextSize(14f);
                    tvQ.setTextColor(0xFF000000);

                    TextView tvA = new TextView(this);
                    tvA.setText(value);
                    tvA.setPadding(8, 8, 0, 8);
                    tvA.setTextSize(14f);
                    tvA.setTextColor(0xFF000000);

                    row.addView(tvQ);
                    row.addView(tvA);
                    table.addView(row);
                }

            }

            sectionCard.addView(table);
            header.setOnClickListener(v -> {
                boolean expanded = table.getVisibility() == View.VISIBLE;
                table.setVisibility(expanded ? View.GONE : View.VISIBLE);
                tvArrow.setText(expanded ? "\u25BC" : "\u25B2");
            });

            llSections.addView(sectionCard);
        }

        // --- Download All ---
        LinearLayout downloadAllLayout = new LinearLayout(this);
        downloadAllLayout.setOrientation(LinearLayout.HORIZONTAL);
        downloadAllLayout.setGravity(Gravity.CENTER);
        downloadAllLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.next_button_bg));
        downloadAllLayout.setPadding(40, 30, 40, 30);

        LinearLayout.LayoutParams outerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        outerParams.gravity = Gravity.CENTER_HORIZONTAL;
        outerParams.setMargins(0, 60, 0, 60);
        downloadAllLayout.setLayoutParams(outerParams);

        ImageView icon = new ImageView(this);
        icon.setImageResource(android.R.drawable.stat_sys_download_done);
        icon.setColorFilter(Color.WHITE);

        TextView txtDownload = new TextView(this);
        txtDownload.setText("Download All Sections");
        txtDownload.setTextColor(Color.WHITE);
        txtDownload.setTextSize(16f);
        txtDownload.setPadding(20, 0, 0, 0);

        downloadAllLayout.addView(icon);
        downloadAllLayout.addView(txtDownload);
        downloadAllLayout.setOnClickListener(v -> showDownloadOptions(null, true));

        llSections.addView(downloadAllLayout);

        Button btnDownloadAllPatients = new Button(this);
        btnDownloadAllPatients.setText("Download All Patients (Excel)");
        btnDownloadAllPatients.setBackgroundColor(Color.parseColor("#FF9800"));
        btnDownloadAllPatients.setTextColor(Color.WHITE);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, 40, 0, 40);
        btnDownloadAllPatients.setLayoutParams(lp);

        btnDownloadAllPatients.setOnClickListener(v -> {
            if (allPatients == null || allPatients.isEmpty()) {
                Toast.makeText(this, "No patient list available", Toast.LENGTH_SHORT).show();
                return;
            }
            downloadAllPatientsCsv();
        });

        llSections.addView(btnDownloadAllPatients);

    }
    private void downloadAllPatientsCsv() {

        new Thread(() -> {
            try {
                StringBuilder csv = new StringBuilder();
                csv.append("Patient ID,Total Risk Level,Section,Question,Answer,Score\n");

                for (PatientRef p : allPatients) {

                    Response<SurveyDisplayResponse> resp =
                            apiService.getSurveyByPatient(
                                    "Token " + SharedPrefManager.getInstance(this).getToken(),
                                    p.pk
                            ).execute();

                    if (!resp.isSuccessful() || resp.body() == null) continue;

                    SurveyDisplayResponse s = resp.body();
                    String risk = calculateRisk(s.getTotal_score());

                    for (SurveyDisplayResponse.Answer ans : s.getAnswers()) {

                        String answer = ans.getSelected_option();
                        if (answer == null || answer.isEmpty()) {
                            answer = ans.getCustom_text();
                        }

                        csv.append(p.pid).append(",")     // ✅ Patient ID
                                .append(risk).append(",")
                                .append("\"").append(ans.getSectionName()).append("\",")
                                .append("\"").append(ans.getQuestion()).append("\",")
                                .append("\"").append(answer).append("\",")
                                .append(ans.getScore()).append("\n");
                    }

            }

                saveAllPatientsCsv(csv.toString());

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
    private void saveAllPatientsCsv(String csvContent) throws Exception {

        OutputStream os;
        String fileName = "All_Patients_Survey_Report.csv";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
            values.put(MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/SurveyReports");

            Uri uri = getContentResolver()
                    .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            os = getContentResolver().openOutputStream(uri);
        } else {
            File dir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "SurveyReports"
            );
            if (!dir.exists()) dir.mkdirs();
            os = new FileOutputStream(new File(dir, fileName));
        }

        os.write(csvContent.getBytes());
        os.close();

        runOnUiThread(() ->
                Toast.makeText(this,
                        "All patients Excel saved (CSV)",
                        Toast.LENGTH_LONG).show()
        );
    }


    private void showDownloadOptions(String sectionName, boolean all) {

        // ✅ ADD CSV HERE
        String[] formats = {"PDF", "TXT", "CSV"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose format to download");

        builder.setItems(formats, (dialog, which) -> {
            if (surveyData == null) {
                Toast.makeText(this, "No data available", Toast.LENGTH_SHORT).show();
                return;
            }

            String format = formats[which];

            if (format.equals("PDF")) {

                String fileName = all
                        ? "Report.pdf"
                        : sectionName.replace(" ", "_") + ".pdf";

                String content = all
                        ? buildAllContent()
                        : buildSectionContent(sectionName);

                savePdf(fileName, content);

            } else if (format.equals("TXT")) {

                String fileName = all
                        ? "Report.txt"
                        : sectionName.replace(" ", "_") + ".txt";

                String content = all
                        ? buildAllContent()
                        : buildSectionContent(sectionName);

                saveToDownloads(fileName, content);

            } else if (format.equals("CSV")) {

                // 🔥 THIS WAS NEVER REACHED BEFORE
                saveCsv(sectionName, all);
            }
        });

        builder.show();
    }

    private void saveCsv(String sectionName, boolean all) {
        try {
            OutputStream outputStream;

            String fileName = all
                    ? "Survey_Report.csv"
                    : sectionName.replace(" ", "_") + ".csv";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
                values.put(
                        MediaStore.Downloads.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS + "/SurveyReports"
                );

                Uri uri = getContentResolver()
                        .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                outputStream = getContentResolver().openOutputStream(uri);
            } else {
                File dir = new File(
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS),
                        "SurveyReports"
                );
                if (!dir.exists()) dir.mkdirs();
                outputStream = new FileOutputStream(new File(dir, fileName));
            }

            StringBuilder csv = new StringBuilder();
            csv.append("Section,Question,Answer,Score\n");

            for (SurveyDisplayResponse.Answer ans : surveyData.getAnswers()) {
                if (all || ans.getSectionName().equalsIgnoreCase(sectionName)) {

                    String answer = ans.getSelected_option();
                    if (answer == null || answer.isEmpty()) {
                        answer = ans.getCustom_text();
                    }

                    csv.append("\"").append(ans.getSectionName()).append("\",")
                            .append("\"").append(ans.getQuestion()).append("\",")
                            .append("\"").append(answer).append("\",")
                            .append(ans.getScore()).append("\n");
                }
            }

            outputStream.write(csv.toString().getBytes());
            outputStream.close();

            Toast.makeText(this,
                    "CSV saved to Downloads/SurveyReports",
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this,
                    "CSV error: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void savePdf(String fileName, String text) {
        try {
            OutputStream outputStream;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/SurveyReports");
                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                outputStream = getContentResolver().openOutputStream(uri);
            } else {
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SurveyReports");
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dir, fileName);
                outputStream = new FileOutputStream(file);
            }

            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph(text));
            document.close();

            Toast.makeText(this, "PDF saved to Downloads/SurveyReports", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToDownloads(String fileName, String content) {
        try {
            OutputStream outputStream;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/SurveyReports");
                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                outputStream = getContentResolver().openOutputStream(uri);

            } else {
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SurveyReports");
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dir, fileName);
                outputStream = new FileOutputStream(file);
            }

            outputStream.write(content.getBytes());
            outputStream.close();

            Toast.makeText(this, "File saved to Downloads/SurveyReports", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error saving file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String buildSectionContent(String sectionName) {
        StringBuilder content = new StringBuilder();
        content.append("Section: ").append(sectionName).append("\n\n");

        for (SurveyDisplayResponse.Answer ans : surveyData.getAnswers()) {
            if (ans != null && ans.getSectionName() != null &&
                    ans.getSectionName().equalsIgnoreCase(sectionName)) {

                String value = ans.getSelected_option();
                if (value == null || value.isEmpty()) value = ans.getCustom_text();

                content.append("Q: ").append(ans.getQuestion()).append("\nA: ").append(value).append("\n\n");
            }
        }

        return content.toString();
    }

    private String buildAllContent() {
        StringBuilder content = new StringBuilder();
        content.append("Reports\n====================\n\n");

        for (SurveyDisplayResponse.SectionScore sec : surveyData.getSection_scores()) {
            content.append("Section: ").append(sec.getSection())
                    .append(" (Score: ").append(sec.getScore()).append(")\n")
                    .append("----------------------------------------------------\n");

            for (SurveyDisplayResponse.Answer ans : surveyData.getAnswers()) {
                if (ans != null && ans.getSectionName() != null &&
                        ans.getSectionName().equalsIgnoreCase(sec.getSection())) {

                    String value = ans.getSelected_option();
                    if (value == null || value.isEmpty()) value = ans.getCustom_text();

                    content.append("Q: ").append(ans.getQuestion()).append("\nA: ").append(value).append("\n\n");
                }
            }

            content.append("\n\n");
        }

        return content.toString();
    }

    private void showSurveyNotCompleted() {
        llSections.removeAllViews();

        TextView tvMsg = new TextView(this);
        tvMsg.setText("Survey not completed.");
        tvMsg.setTextSize(18f);
        tvMsg.setGravity(Gravity.CENTER);

        llSections.addView(tvMsg);
    }
    private String calculateRisk(int score) {
        if (score <= 20) return "Low";
        else if (score <= 40) return "Moderate";
        else if (score <= 60) return "High";
        else return "Very High";
    }

    private String getRiskLevel(int score) {
        if (score <= 20) return "Low";
        else if (score <= 40) return "Moderate";
        else if (score <= 60) return "High";
        else return "Very High";
    }
}
