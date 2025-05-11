package com.myapp.fitnessapp.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.database.DBHelper;
import com.myapp.fitnessapp.utils.UserSession;

import java.util.Locale;

public class ProfileSetUpFragment extends Fragment {
    private ImageView profileImageView;
    private EditText fullNameEditText, ageEditText, heightEditText, weightEditText;
    private RadioGroup heightUnitGroup, weightUnitGroup;
    private Button saveProfileButton;
    private Uri imageUri;
    private String userEmail;

    // Base metric values
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

    private final ActivityResultLauncher<Void> takePictureLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicturePreview(),
                    bitmap -> {
                        if (bitmap != null) {
                            profileImageView.setImageBitmap(bitmap);
                        }
                    }
            );

    public ProfileSetUpFragment() {}

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_setup, container, false);

        // 1) Bind views
        profileImageView  = view.findViewById(R.id.profile_image_view);
        fullNameEditText  = view.findViewById(R.id.full_name_edit_text);
        ageEditText       = view.findViewById(R.id.age_edit_text);
        heightEditText    = view.findViewById(R.id.heightEditText);
        weightEditText    = view.findViewById(R.id.weightEditText);
        heightUnitGroup   = view.findViewById(R.id.heightUnitGroup);
        weightUnitGroup   = view.findViewById(R.id.weightUnitGroup);
        saveProfileButton = view.findViewById(R.id.save_profile_button);

        // 2) Initialize session & shared DB helper
        UserSession.init(requireContext());

        // 3) Determine the userEmail
        Bundle args = getArguments();
        if (args != null && args.getString("email") != null) {
            userEmail = args.getString("email");
        } else {
            FirebaseUser fb = FirebaseAuth.getInstance().getCurrentUser();
            if (fb != null && fb.getEmail() != null) {
                userEmail = fb.getEmail();
            } else {
                // no email → back to welcome
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_global_welcomeFragment);
                return view;
            }
        }

        // 4) Prefill the username if provided
        if (args != null && args.getString("username") != null) {
            fullNameEditText.setText(args.getString("username"));
        }

        profileImageView.setOnClickListener(v -> showImageSourceDialog());

        // TextWatchers update base metrics when user types
        heightEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c){}
            @Override public void afterTextChanged(Editable s) {
                if (isTogglingHeight) return;
                String raw = s.toString().trim();
                if (raw.isEmpty()) return;
                try {
                    if (heightUnitGroup.getCheckedRadioButtonId() == R.id.heightMetric) {
                        baseHeightCm = Double.parseDouble(raw);
                    } else {
                        String[] p = raw.split("\\.");
                        int ft = Integer.parseInt(p[0]);
                        int in = p.length>1 ? Integer.parseInt(p[1]) : 0;
                        baseHeightCm = (ft*12 + in) * 2.54;
                    }
                } catch (NumberFormatException ignore){}
            }
        });

        weightEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c){}
            @Override public void afterTextChanged(Editable s) {
                if (isTogglingWeight) return;
                String raw = s.toString().trim();
                if (raw.isEmpty()) return;
                try {
                    if (weightUnitGroup.getCheckedRadioButtonId() == R.id.weightMetric) {
                        baseWeightKg = Double.parseDouble(raw);
                    } else {
                        baseWeightKg = Double.parseDouble(raw) * 0.453592;
                    }
                } catch (NumberFormatException ignore){}
            }
        });

        // Unit toggles convert from base metric
        heightUnitGroup.setOnCheckedChangeListener((g, id) -> {
            isTogglingHeight = true;
            if (id == R.id.heightMetric) {
                heightEditText.setHint("cm");
                heightEditText.setText(String.valueOf((int) baseHeightCm));
            } else {
                double inches = baseHeightCm / 2.54;
                int ft = (int)(inches / 12);
                int in = (int)(inches - ft * 12);
                heightEditText.setHint("ft.in");
                heightEditText.setText(ft + "." + in);
            }
            heightEditText.setSelection(heightEditText.getText().length());
            isTogglingHeight = false;
        });

        weightUnitGroup.setOnCheckedChangeListener((g, id) -> {
            isTogglingWeight = true;
            if (id == R.id.weightMetric) {
                weightEditText.setHint("kg");
                weightEditText.setText(String.format(Locale.US, "%.1f", baseWeightKg));
            } else {
                double lbs = baseWeightKg / 0.453592;
                weightEditText.setHint("lb");
                weightEditText.setText(String.format(Locale.US, "%.1f", lbs));
            }
            weightEditText.setSelection(weightEditText.getText().length());
            isTogglingWeight = false;
        });

        saveProfileButton.setOnClickListener(v -> onSaveProfile());
        return view;
    }

    private void showImageSourceDialog() {
        String[] options = {"Choose from gallery", "Take a photo"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Select profile image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        pickImageLauncher.launch(
                                new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        );
                    } else {
                        takePictureLauncher.launch(null);
                    }
                }).show();
    }

    private void onSaveProfile() {
        String name = fullNameEditText.getText().toString().trim();
        String ageS = ageEditText.getText().toString().trim();
        if (name.isEmpty() || ageS.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Please complete name and age",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        int age = Integer.parseInt(ageS);

        // Disable button to prevent double submits
        saveProfileButton.setEnabled(false);

        DBHelper db = UserSession.getDbHelper();
        boolean success = db.updateProfile(
                userEmail,
                name,
                age,
                imageUri != null ? imageUri.toString() : null,
                baseHeightCm,
                baseWeightKg
        );

        if (success) {
            // Seed exercises now that profile is set
            UserSession.getDbHelper().seedUserExercises(userEmail);

            // Navigate to Dashboard, popping this flow off the back stack
            NavOptions opts = new NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_profile_to_dashboard, null, opts);
        } else {
            Toast.makeText(requireContext(),
                    "Failed to save profile",
                    Toast.LENGTH_SHORT).show();
            saveProfileButton.setEnabled(true);
        }
    }
}
