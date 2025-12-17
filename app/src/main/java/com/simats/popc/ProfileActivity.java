package com.simats.popc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.simats.popc.model.DoctorResponse;
import com.simats.popc.network.ApiClient;
import com.simats.popc.network.ApiService;
import com.simats.popc.utils.SharedPrefManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    EditText etName, etPhone, etEmail, etAgeInput;
    Spinner etSpecialization;
    TextView etDoctorId;
    ImageView ivProfile, btn1;
    Button btnUpdate;

    // Gender segmented control
    TextView btnFemale, btnMale, btnOther;

    // Age controls
    TextView btnAgeMinus, btnAgePlus;

    ApiService apiService;
    String token;
    Uri selectedImageUri;
    String selectedGender = "Female";
    int selectedAge = 24;

    ActivityResultLauncher<Intent> galleryLauncher;

    // ----- initial state snapshot -----
    private String initialName = "";
    private String initialPhone = "";
    private String initialEmail = "";
    private String initialSpec = "";
    private String initialGender = "";
    private int initialAge = 0;
    private String initialProfileImageUrl = null;
    private boolean imageChanged = false; // marks if user picked a new image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Initialize Views
        etDoctorId = findViewById(R.id.etDoctorId);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etSpecialization = findViewById(R.id.etSpecialization);
        ivProfile = findViewById(R.id.ivProfile);
        btnUpdate = findViewById(R.id.btnUpdate);
        btn1 = findViewById(R.id.btnBack);
        btnFemale = findViewById(R.id.btnFemale);
        btnMale = findViewById(R.id.btnMale);
        btnOther = findViewById(R.id.btnOther);
        btnAgeMinus = findViewById(R.id.btnAgeMinus);
        btnAgePlus = findViewById(R.id.btnAgePlus);
        etAgeInput = findViewById(R.id.etAgeInput);
        
        ivProfile.setImageResource(R.drawable.ic_person_outline);

        // Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.specialization_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etSpecialization.setAdapter(adapter);

        // Force black text color for selected item when shown
        etSpecialization.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView selected = (TextView) parent.getChildAt(0);
                if (selected != null) selected.setTextColor(getResources().getColor(android.R.color.black));
                onUserChanged(); // selection considered a possible change
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // --- Gender toggle ---
        btnFemale.setOnClickListener(v -> { setGenderSelected("Female"); onUserChanged(); });
        btnMale.setOnClickListener(v -> { setGenderSelected("Male"); onUserChanged(); });
        btnOther.setOnClickListener(v -> { setGenderSelected("Other"); onUserChanged(); });

        // --- Age logic ---
        etAgeInput.setText(String.valueOf(selectedAge));
        btnAgePlus.setOnClickListener(v -> {
            int age = getCurrentAge();
            if (age < 120) {
                etAgeInput.setText(String.valueOf(age + 1));
            }
        });
        btnAgeMinus.setOnClickListener(v -> {
            int age = getCurrentAge();
            if (age > 1) etAgeInput.setText(String.valueOf(age - 1));
        });
        etAgeInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                try { selectedAge = Integer.parseInt(s.toString()); }
                catch (NumberFormatException e) { selectedAge = 0; }
                onUserChanged();
            }
        });

        // --- Real-time Name Validation (only alphabets + space) ---
        etName.addTextChangedListener(new TextWatcher() {
            private String lastValid = "";

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (!input.matches("^[A-Za-z ]*$")) {
                    etName.setText(lastValid);
                    etName.setSelection(etName.getText().length());
                    Toast.makeText(ProfileActivity.this, "Only alphabets allowed in name", Toast.LENGTH_SHORT).show();
                } else {
                    lastValid = input;
                    onUserChanged();
                }
            }
        });

        etPhone.addTextChangedListener(simpleWatcher());
        etEmail.addTextChangedListener(simpleWatcher());

        token = "Token " + SharedPrefManager.getInstance(this).getToken();
        apiService = ApiClient.getClient().create(ApiService.class);

        // disable update until load finishes / user changes
        setUpdateEnabled(false);

        loadProfile();

        // Image picker
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            ivProfile.setImageURI(selectedImageUri);
                            imageChanged = true;
                            onUserChanged();
                        }
                    }
                });

        ivProfile.setOnClickListener(v -> showImagePickerDialog());
        btnUpdate.setOnClickListener(v -> updateProfile());
        btn1.setOnClickListener(v -> {
            Intent i = new Intent(this, DoctorHomeActivity.class);
            i.putExtra("mode", "edit");
            startActivity(i);
        });
    }

    private TextWatcher simpleWatcher() {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { onUserChanged(); }
        };
    }

    private int getCurrentAge() {
        try {
            return Integer.parseInt(etAgeInput.getText().toString());
        } catch (NumberFormatException e) {
            return selectedAge;
        }
    }

    private void setGenderSelected(String gender) {
        selectedGender = gender;
        btnFemale.setBackgroundResource(gender.equals("Female") ? R.drawable.bg_segment_selected : android.R.color.transparent);
        btnMale.setBackgroundResource(gender.equals("Male") ? R.drawable.bg_segment_selected : android.R.color.transparent);
        btnOther.setBackgroundResource(gender.equals("Other") ? R.drawable.bg_segment_selected : android.R.color.transparent);

        btnFemale.setTextColor(gender.equals("Female") ? 0xFFFFFFFF : 0xFF555555);
        btnMale.setTextColor(gender.equals("Male") ? 0xFFFFFFFF : 0xFF555555);
        btnOther.setTextColor(gender.equals("Other") ? 0xFFFFFFFF : 0xFF555555);
    }

    private void showImagePickerDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Choose Option")
                .setItems(new CharSequence[]{"Gallery"}, (dialog, which) -> {
                    if (which == 0) openGallery();
                }).show();
    }

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
                return;
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
                return;
            }
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == 100 || requestCode == 101) &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            Toast.makeText(this, "Permission required to access gallery", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProfile() {
        apiService.getDoctorProfile(token).enqueue(new Callback<DoctorResponse>() {
            @Override
            public void onResponse(Call<DoctorResponse> call, Response<DoctorResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DoctorResponse doctor = response.body();

                    // populate UI
                    etDoctorId.setText(doctor.getDoctorId());
                    etName.setText(doctor.getName());
                    etPhone.setText(doctor.getPhone());
                    etEmail.setText(doctor.getEmail());
                    try { selectedAge = Integer.parseInt(doctor.getAge()); } catch (Exception ignored) {}
                    etAgeInput.setText(String.valueOf(selectedAge));

                    if (doctor.getSpecialization() != null) {
                        int pos = ((ArrayAdapter) etSpecialization.getAdapter()).getPosition(doctor.getSpecialization());
                        if (pos >= 0) etSpecialization.setSelection(pos);
                    }

                    if (doctor.getGender() != null) setGenderSelected(doctor.getGender());
                    if (doctor.getProfileImageUrl() != null) {
                        initialProfileImageUrl = doctor.getProfileImageUrl();
                        // bypass cache to ensure latest image (useful during development / immediately after upload)
                        Glide.with(ProfileActivity.this)
                                .load(initialProfileImageUrl)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(ivProfile);
                    } else {
                        ivProfile.setImageDrawable(null); // clear if no image
                    }

                    // store initial snapshot for change-detection
                    initialName = safe(doctor.getName());
                    initialPhone = safe(doctor.getPhone());
                    initialEmail = safe(doctor.getEmail());
                    initialSpec = doctor.getSpecialization() == null ? "" : doctor.getSpecialization();
                    initialGender = doctor.getGender() == null ? "" : doctor.getGender();
                    try { initialAge = Integer.parseInt(doctor.getAge()); } catch (Exception e) { initialAge = selectedAge; }
                    imageChanged = false;

                    // after load enable check: update button disabled until user changes
                    onUserChanged();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DoctorResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String safe(String s) { return s == null ? "" : s; }

    // Called whenever user might have changed something
    private void onUserChanged() {
        boolean changed = false;

        // compare current vs initial
        if (!safe(etName.getText().toString()).equals(initialName)) changed = true;
        else if (!safe(etPhone.getText().toString()).equals(initialPhone)) changed = true;
        else if (!safe(etEmail.getText().toString()).equals(initialEmail)) changed = true;

        String currentSpec = "";
        if (etSpecialization.getSelectedItem() != null) currentSpec = etSpecialization.getSelectedItem().toString();
        if (!currentSpec.equals(initialSpec)) changed = true;

        if (!safe(selectedGender).equals(initialGender)) changed = true;

        int currentAge = getCurrentAge();
        if (currentAge != initialAge) changed = true;

        if (imageChanged) changed = true;

        setUpdateEnabled(changed);
    }

    private void setUpdateEnabled(boolean enabled) {
        btnUpdate.setEnabled(enabled);
        btnUpdate.setAlpha(enabled ? 1f : 0.5f);
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File tempFile = new File(getCacheDir(), "temp_profile_image.jpg");
            OutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateProfile() {
        selectedAge = getCurrentAge();

        String nameStr = etName.getText().toString().trim();
        String phoneStr = etPhone.getText().toString().trim();
        String emailStr = etEmail.getText().toString().trim();
        String specializationStr = etSpecialization.getSelectedItem() == null ? "" : etSpecialization.getSelectedItem().toString();

        if (nameStr.isEmpty() || phoneStr.isEmpty() || emailStr.isEmpty() ||
                specializationStr.isEmpty() || selectedGender.isEmpty() || selectedAge <= 0) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!nameStr.matches("^[A-Za-z ]+$")) {
            Toast.makeText(this, "Name must contain only alphabets", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!phoneStr.matches("^[0-9]{10}$")) {
            Toast.makeText(this, "Enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody name = RequestBody.create(nameStr, MediaType.parse("text/plain"));
        RequestBody phone = RequestBody.create(phoneStr, MediaType.parse("text/plain"));
        RequestBody age = RequestBody.create(String.valueOf(selectedAge), MediaType.parse("text/plain"));
        RequestBody specialization = RequestBody.create(specializationStr, MediaType.parse("text/plain"));
        RequestBody email = RequestBody.create(emailStr, MediaType.parse("text/plain"));
        RequestBody gender = RequestBody.create(selectedGender, MediaType.parse("text/plain"));

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            File file = uriToFile(selectedImageUri);
            if (file != null) {
                RequestBody reqFile = RequestBody.create(file, MediaType.parse("image/*"));
                imagePart = MultipartBody.Part.createFormData("profile_image", file.getName(), reqFile);
            }
            else {
                ivProfile.setImageResource(R.drawable.ic_person_outline);
            }
        }

        // disable update button while request in flight
        setUpdateEnabled(false);

        apiService.updateDoctorProfile(token, name, email, phone, age, gender, specialization, imagePart)
                .enqueue(new Callback<DoctorResponse>() {
                    @Override
                    public void onResponse(Call<DoctorResponse> call, Response<DoctorResponse> response) {
                        setUpdateEnabled(false);
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();

                            // Use the response body to update UI immediately (faster than reloading)
                            DoctorResponse updated = response.body();

                            etName.setText(updated.getName());
                            etPhone.setText(updated.getPhone());
                            etEmail.setText(updated.getEmail());
                            try { selectedAge = Integer.parseInt(updated.getAge()); } catch (Exception ignored) {}
                            etAgeInput.setText(String.valueOf(selectedAge));

                            if (updated.getSpecialization() != null) {
                                int pos = ((ArrayAdapter) etSpecialization.getAdapter()).getPosition(updated.getSpecialization());
                                if (pos >= 0) etSpecialization.setSelection(pos);
                            }

                            if (updated.getGender() != null) setGenderSelected(updated.getGender());

                            // Update profile image from response (bypass cache to ensure the new image shows)
                            if (updated.getProfileImageUrl() != null) {
                                initialProfileImageUrl = updated.getProfileImageUrl();
                                Glide.with(ProfileActivity.this)
                                        .load(initialProfileImageUrl)
                                        .skipMemoryCache(true)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .into(ivProfile);
                            }else {
                                ivProfile.setImageResource(R.drawable.ic_person_outline);
                            }

                            // reset snapshot to latest server state
                            initialName = safe(updated.getName());
                            initialPhone = safe(updated.getPhone());
                            initialEmail = safe(updated.getEmail());
                            initialSpec = updated.getSpecialization() == null ? "" : updated.getSpecialization();
                            initialGender = updated.getGender() == null ? "" : updated.getGender();
                            try { initialAge = Integer.parseInt(updated.getAge()); } catch (Exception e) { initialAge = selectedAge; }
                            imageChanged = false;
                            onUserChanged(); // re-evaluate button state
                        } else {
                            Toast.makeText(ProfileActivity.this, "Update failed: " + response.code(), Toast.LENGTH_SHORT).show();
                            onUserChanged(); // re-evaluate (allow retry)
                        }
                    }

                    @Override
                    public void onFailure(Call<DoctorResponse> call, Throwable t) {
                        Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        onUserChanged(); // re-enable if needed
                    }
                });
    }
}
