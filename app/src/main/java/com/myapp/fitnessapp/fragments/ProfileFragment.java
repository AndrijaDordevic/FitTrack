package com.myapp.fitnessapp.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.database.DBHelper;
import com.myapp.fitnessapp.utils.UserSession;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private ImageView profileImageView;
    private EditText fullNameET, ageET, heightET, weightET;
    private RadioGroup heightUnitGroup, weightUnitGroup;
    private Button saveButton;
    private Uri imageUri;
    private String userEmail;

    private double baseHeightCm = 0, baseWeightKg = 0;
    private boolean isTogglingHeight = false, isTogglingWeight = false;

    // Launcher for image picker intent result
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == requireActivity().RESULT_OK &&
                                result.getData() != null &&
                                result.getData().getData() != null) {

                            Uri uri = result.getData().getData();
                            // Persist permission across device restarts
                            requireActivity().getContentResolver()
                                    .takePersistableUriPermission(
                                            uri,
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    );

                            imageUri = uri;
                            // Load selected image into view with Glide
                            Glide.with(this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.profile_icon)
                                    .error(R.drawable.profile_icon)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .circleCrop()
                                    .into(profileImageView);
                        }
                    }
            );

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        // Bind UI elements
        profileImageView = v.findViewById(R.id.profile_image_view);
        fullNameET       = v.findViewById(R.id.full_name_edit_text);
        ageET            = v.findViewById(R.id.age_edit_text);
        heightET         = v.findViewById(R.id.height_edit_text);
        weightET         = v.findViewById(R.id.weight_edit_text);
        heightUnitGroup  = v.findViewById(R.id.height_unit_group);
        weightUnitGroup  = v.findViewById(R.id.weight_unit_group);
        saveButton       = v.findViewById(R.id.save_button);

        // Initialize session and check authenticated user
        UserSession.init(requireContext());
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || firebaseUser.getEmail() == null) {
            // Redirect to welcome if not logged in
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_global_welcomeFragment);
            return v;
        }
        userEmail = firebaseUser.getEmail();

        // Round profile image view corners
        profileImageView.setClipToOutline(true);

        // Load saved profile data, set up listeners and toggles
        loadProfile();
        setupTextWatchers();
        setupUnitToggles();

        // Handle image click to pick new profile photo
        profileImageView.setOnClickListener(t -> {
            Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pick.addCategory(Intent.CATEGORY_OPENABLE);
            pick.setType("image/*");
            pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImageLauncher.launch(pick);
        });

        // Save button persists changes
        saveButton.setOnClickListener(t -> saveProfile());
        return v;
    }

    // Load profile from DB into UI, or reset if none exists
    private void loadProfile() {
        DBHelper db = UserSession.getDbHelper();
        Cursor c = db.getProfile(userEmail);
        if (c != null && c.moveToFirst()) {
            fullNameET.setText(c.getString(c.getColumnIndexOrThrow("full_name")));
            ageET.setText(String.valueOf(c.getInt(c.getColumnIndexOrThrow("age"))));
            baseHeightCm = c.getDouble(c.getColumnIndexOrThrow("height_cm"));
            baseWeightKg = c.getDouble(c.getColumnIndexOrThrow("weight_kg"));

            String uriStr = c.getString(c.getColumnIndexOrThrow("image_uri"));
            if (uriStr != null) {
                // Handle file URI vs content URI
                File f = new File(Uri.parse(uriStr).getPath());
                imageUri = f.exists() ? Uri.fromFile(f) : Uri.parse(uriStr);
            } else {
                imageUri = null;
            }

            // Display profile image or placeholder
            if (imageUri != null) {
                Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.profile_icon)
                        .error(R.drawable.profile_icon)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .circleCrop()
                        .into(profileImageView);
            } else {
                Glide.with(this)
                        .load(R.drawable.profile_icon)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .circleCrop()
                        .into(profileImageView);
            }

            // Initialize height and weight fields in metric units
            heightUnitGroup.check(R.id.height_metric);
            heightET.setHint("cm");
            heightET.setText(baseHeightCm > 0 ? String.valueOf((int) baseHeightCm) : "");
            weightUnitGroup.check(R.id.weight_metric);
            weightET.setHint("kg");
            weightET.setText(baseWeightKg > 0 ?
                    String.format(Locale.US, "%.1f", baseWeightKg) : "");
        } else {
            // No profile found: clear fields
            resetProfileFields();
        }
        if (c != null) c.close();
    }

    // Watcher to update baseHeightCm as user edits height
    private void setupTextWatchers() {
        heightET.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void onTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void afterTextChanged(Editable s) {
                if (isTogglingHeight) return;
                String raw = s.toString().trim();
                if (raw.isEmpty()) return;
                try {
                    if (heightUnitGroup.getCheckedRadioButtonId() == R.id.height_metric) {
                        baseHeightCm = Double.parseDouble(raw);
                    } else {
                        // Imperial input: ft.in (e.g., 5.11)
                        String[] parts = raw.split("\\.");
                        int ft = Integer.parseInt(parts[0]);
                        int in = parts.length>1 ? Integer.parseInt(parts[1]) : 0;
                        baseHeightCm = (ft*12 + in)*2.54;
                    }
                } catch (NumberFormatException ignored){}
            }
        });

        // Similar watcher for weight input
        weightET.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void onTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void afterTextChanged(Editable s) {
                if (isTogglingWeight) return;
                String raw = s.toString().trim();
                if (raw.isEmpty()) return;
                try {
                    if (weightUnitGroup.getCheckedRadioButtonId() == R.id.weight_metric) {
                        baseWeightKg = Double.parseDouble(raw);
                    } else {
                        baseWeightKg = Double.parseDouble(raw)*0.453592;
                    }
                } catch (NumberFormatException ignored){}
            }
        });
    }

    // Handle unit toggle changes to convert and display values
    private void setupUnitToggles() {
        heightUnitGroup.setOnCheckedChangeListener((g,id)->{
            isTogglingHeight = true;
            if (id == R.id.height_metric) {
                heightET.setHint("cm");
                heightET.setText(baseHeightCm>0 ?
                        String.valueOf((int) baseHeightCm) : "");
            } else {
                double inches = baseHeightCm/2.54;
                int ft = (int)(inches/12);
                int in = (int)(inches - ft*12);
                heightET.setHint("ft.in");
                heightET.setText(baseHeightCm>0 ? ft + "." + in : "");
            }
            heightET.setSelection(heightET.getText().length());
            isTogglingHeight = false;
        });

        weightUnitGroup.setOnCheckedChangeListener((g,id)->{
            isTogglingWeight = true;
            if (id == R.id.weight_metric) {
                weightET.setHint("kg");
                weightET.setText(baseWeightKg>0 ?
                        String.format(Locale.US, "%.1f", baseWeightKg) : "");
            } else {
                double lbs = baseWeightKg/0.453592;
                weightET.setHint("lb");
                weightET.setText(baseWeightKg>0 ?
                        String.format(Locale.US, "%.1f", lbs) : "");
            }
            weightET.setSelection(weightET.getText().length());
            isTogglingWeight = false;
        });
    }

    // Reset UI to default placeholder state
    private void resetProfileFields() {
        Glide.with(this)
                .load(R.drawable.profile_icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop()
                .into(profileImageView);

        imageUri = null;
        fullNameET.setText("");
        ageET.setText("");
        baseHeightCm = baseWeightKg = 0;
        heightUnitGroup.check(R.id.height_metric);
        heightET.setHint("cm");
        heightET.setText("");
        weightUnitGroup.check(R.id.weight_metric);
        weightET.setHint("kg");
        weightET.setText("");
    }

    // Validate inputs and save profile to DB
    private void saveProfile() {
        String name = fullNameET.getText().toString().trim();
        String ageS = ageET.getText().toString().trim();
        if (name.isEmpty() || ageS.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Please enter name and age",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageS);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(),
                    "Invalid age format",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Age range validation (15-130)
        if (age < 15 || age > 130) {
            Toast.makeText(requireContext(),
                    "Age must be between 15 and 130",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        DBHelper db = UserSession.getDbHelper();
        boolean ok = db.updateProfile(
                userEmail,
                name,
                age,
                imageUri != null ? imageUri.toString() : null,
                baseHeightCm,
                baseWeightKg
        );

        // Show result and navigate back on success
        Toast.makeText(requireContext(),
                ok ? "Profile updated!" : "Update failed",
                Toast.LENGTH_SHORT).show();

        if (ok) {
            NavHostFragment.findNavController(this).popBackStack();
        }
    }
}
