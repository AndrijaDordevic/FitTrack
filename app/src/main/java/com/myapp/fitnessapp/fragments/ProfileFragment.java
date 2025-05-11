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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.database.DBHelper;
import com.myapp.fitnessapp.utils.UserSession;

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

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == requireActivity().RESULT_OK
                                && result.getData() != null
                                && result.getData().getData() != null) {
                            imageUri = result.getData().getData();
                            profileImageView.setImageURI(imageUri);
                        }
                    }
            );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        // 1) Bind views
        profileImageView = v.findViewById(R.id.profile_image_view);
        fullNameET       = v.findViewById(R.id.full_name_edit_text);
        ageET            = v.findViewById(R.id.age_edit_text);
        heightET         = v.findViewById(R.id.height_edit_text);
        weightET         = v.findViewById(R.id.weight_edit_text);
        heightUnitGroup  = v.findViewById(R.id.height_unit_group);
        weightUnitGroup  = v.findViewById(R.id.weight_unit_group);
        saveButton       = v.findViewById(R.id.save_button);

        // 2) Init session and shared DB helper
        UserSession.init(requireContext());

        // 3) Determine current user via FirebaseAuth
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || firebaseUser.getEmail() == null) {
            // not logged in → back to Welcome
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_global_welcomeFragment);
            return v;
        }
        userEmail = firebaseUser.getEmail();

        // 4) Load profile data (or reset fields if none)
        loadProfile();

        // 5) Set up text watchers for height/weight toggling logic
        heightET.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (isTogglingHeight) return;
                String raw = s.toString().trim();
                if (raw.isEmpty()) return;
                try {
                    if (heightUnitGroup.getCheckedRadioButtonId() == R.id.height_metric) {
                        baseHeightCm = Double.parseDouble(raw);
                    } else {
                        String[] parts = raw.split("\\.");
                        int ft    = Integer.parseInt(parts[0]);
                        int inch  = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                        baseHeightCm = (ft * 12 + inch) * 2.54;
                    }
                } catch (NumberFormatException ignore) {}
            }
        });

        weightET.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (isTogglingWeight) return;
                String raw = s.toString().trim();
                if (raw.isEmpty()) return;
                try {
                    if (weightUnitGroup.getCheckedRadioButtonId() == R.id.weight_metric) {
                        baseWeightKg = Double.parseDouble(raw);
                    } else {
                        baseWeightKg = Double.parseDouble(raw) * 0.453592;
                    }
                } catch (NumberFormatException ignore) {}
            }
        });

        // 6) Unit toggle listeners
        heightUnitGroup.setOnCheckedChangeListener((g, id) -> {
            isTogglingHeight = true;
            if (id == R.id.height_metric) {
                heightET.setHint("cm");
                heightET.setText(baseHeightCm > 0
                        ? String.valueOf((int) baseHeightCm) : "");
            } else {
                double inches = baseHeightCm / 2.54;
                int ft    = (int)(inches / 12);
                int inch  = (int)(inches - ft * 12);
                heightET.setHint("ft.in");
                heightET.setText(baseHeightCm > 0
                        ? (ft + "." + inch) : "");
            }
            heightET.setSelection(heightET.getText().length());
            isTogglingHeight = false;
        });

        weightUnitGroup.setOnCheckedChangeListener((g, id) -> {
            isTogglingWeight = true;
            if (id == R.id.weight_metric) {
                weightET.setHint("kg");
                weightET.setText(baseWeightKg > 0
                        ? String.format(Locale.US, "%.1f", baseWeightKg) : "");
            } else {
                double lbs = baseWeightKg / 0.453592;
                weightET.setHint("lb");
                weightET.setText(baseWeightKg > 0
                        ? String.format(Locale.US, "%.1f", lbs) : "");
            }
            weightET.setSelection(weightET.getText().length());
            isTogglingWeight = false;
        });

        // 7) Image picker
        profileImageView.setOnClickListener(t -> {
            Intent pick = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            );
            pickImageLauncher.launch(pick);
        });

        // 8) Save button
        saveButton.setOnClickListener(t -> saveProfile());

        return v;
    }

    private void loadProfile() {
        if (userEmail == null) {
            resetProfileFields();
            return;
        }

        DBHelper db = UserSession.getDbHelper();
        Cursor c = db.getProfile(userEmail);
        if (c != null && c.moveToFirst()) {
            // Image
            String uriStr = c.getString(c.getColumnIndexOrThrow("image_uri"));
            if (uriStr != null) {
                imageUri = Uri.parse(uriStr);
                profileImageView.setImageURI(imageUri);
            } else {
                profileImageView.setImageResource(R.drawable.profile_icon);
            }

            // Name & age
            fullNameET.setText(
                    c.getString(c.getColumnIndexOrThrow("full_name"))
            );
            ageET.setText(String.valueOf(
                    c.getInt(c.getColumnIndexOrThrow("age"))
            ));

            // Base metrics
            baseHeightCm = c.getDouble(
                    c.getColumnIndexOrThrow("height_cm")
            );
            baseWeightKg = c.getDouble(
                    c.getColumnIndexOrThrow("weight_kg")
            );

            // Initialize UI
            heightUnitGroup.check(R.id.height_metric);
            heightET.setHint("cm");
            heightET.setText(String.valueOf((int) baseHeightCm));

            weightUnitGroup.check(R.id.weight_metric);
            weightET.setHint("kg");
            weightET.setText(
                    String.format(Locale.US, "%.1f", baseWeightKg)
            );
        } else {
            resetProfileFields();
        }
        if (c != null) c.close();
    }

    private void resetProfileFields() {
        profileImageView.setImageResource(R.drawable.profile_icon);
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

    private void saveProfile() {
        String name = fullNameET.getText().toString().trim();
        String ageS = ageET.getText().toString().trim();
        if (name.isEmpty() || ageS.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Please enter name and age",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }
        int age = Integer.parseInt(ageS);

        DBHelper db = UserSession.getDbHelper();
        boolean ok = db.updateProfile(
                userEmail,
                name,
                age,
                imageUri != null ? imageUri.toString() : null,
                baseHeightCm,
                baseWeightKg
        );

        Toast.makeText(requireContext(),
                ok ? "Profile updated!" : "Update failed",
                Toast.LENGTH_SHORT
        ).show();
        if (ok) {
            NavHostFragment.findNavController(this).popBackStack();
        }
    }
}
