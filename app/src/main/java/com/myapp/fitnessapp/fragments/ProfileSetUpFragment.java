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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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

    private double baseHeightCm = 0, baseWeightKg = 0;
    private boolean isTogglingHeight = false, isTogglingWeight = false;

    // Launcher for gallery pick
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == requireActivity().RESULT_OK
                                && result.getData() != null
                                && result.getData().getData() != null) {
                            imageUri = result.getData().getData();
                            // Display the selected image as a circle
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

    // Launcher for camera capture
    private final ActivityResultLauncher<Void> takePictureLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicturePreview(),
                    bitmap -> {
                        if (bitmap != null) {
                            // Display the taken image as a circle
                            Glide.with(this)
                                    .load(bitmap)
                                    .placeholder(R.drawable.profile_icon)
                                    .error(R.drawable.profile_icon)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .circleCrop()
                                    .into(profileImageView);
                            imageUri = null; // No URI, but we could save bitmap if needed
                        }
                    }
            );

    public ProfileSetUpFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_setup, container, false);

        // Bind view components
        profileImageView  = view.findViewById(R.id.profile_image_view);
        fullNameEditText  = view.findViewById(R.id.full_name_edit_text);
        ageEditText       = view.findViewById(R.id.age_edit_text);
        heightEditText    = view.findViewById(R.id.heightEditText);
        weightEditText    = view.findViewById(R.id.weightEditText);
        heightUnitGroup   = view.findViewById(R.id.heightUnitGroup);
        weightUnitGroup   = view.findViewById(R.id.weightUnitGroup);
        saveProfileButton = view.findViewById(R.id.save_profile_button);

        // Initialise session and DB helper
        UserSession.init(requireContext());

        // Determine userEmail from args or Firebase
        Bundle args = getArguments();
        if (args != null && args.getString("email") != null) {
            userEmail = args.getString("email");
        } else {
            FirebaseUser fb = FirebaseAuth.getInstance().getCurrentUser();
            if (fb != null && fb.getEmail() != null) {
                userEmail = fb.getEmail();
            } else {
                // Redirect if no user info
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_global_welcomeFragment);
                return view;
            }
        }

        // Set default profile icon as circular
        Glide.with(this)
                .load(R.drawable.profile_icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop()
                .into(profileImageView);

        // Show chooser for image source
        profileImageView.setOnClickListener(v -> showImageSourceDialog());

        // Watch height input to update baseHeightCm
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
                        baseHeightCm = (ft*12 + in)*2.54;
                    }
                } catch (NumberFormatException ignore){}
            }
        });

        // Watch weight input to update baseWeightKg
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
                        baseWeightKg = Double.parseDouble(raw)*0.453592;
                    }
                } catch (NumberFormatException ignore){}
            }
        });

        // Toggle height units and convert display
        heightUnitGroup.setOnCheckedChangeListener((g,id)->{
            isTogglingHeight = true;
            if (id == R.id.heightMetric) {
                heightEditText.setHint("cm");
                heightEditText.setText(String.valueOf((int) baseHeightCm));
            } else {
                double inches = baseHeightCm/2.54;
                int ft = (int)(inches/12);
                int in = (int)(inches - ft*12);
                heightEditText.setHint("ft.in");
                heightEditText.setText(ft+"."+in);
            }
            heightEditText.setSelection(heightEditText.getText().length());
            isTogglingHeight = false;
        });

        // Toggle weight units and convert display
        weightUnitGroup.setOnCheckedChangeListener((g,id)->{
            isTogglingWeight = true;
            if (id == R.id.weightMetric) {
                weightEditText.setHint("kg");
                weightEditText.setText(String.format(Locale.US,"%.1f", baseWeightKg));
            } else {
                double lbs = baseWeightKg/0.453592;
                weightEditText.setHint("lb");
                weightEditText.setText(String.format(Locale.US,"%.1f", lbs));
            }
            weightEditText.setSelection(weightEditText.getText().length());
            isTogglingWeight = false;
        });

        // Save profile on button click
        saveProfileButton.setOnClickListener(v -> onSaveProfile());
        return view;
    }

    // Show options for selecting or capturing image
    private void showImageSourceDialog() {
        String[] options = {"Choose from gallery", "Take a photo"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Select profile image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        pickImageLauncher.launch(pick);
                    } else {
                        takePictureLauncher.launch(null);
                    }
                }).show();
    }

    // Validate inputs, update DB, seed exercises, and navigate
    private void onSaveProfile() {
        String name = fullNameEditText.getText().toString().trim();
        String ageS = ageEditText.getText().toString().trim();
        if (name.isEmpty() || ageS.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Please complete name and age",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        int age;
        try {
            age = Integer.parseInt(ageS);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid age format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Age range check
        if (age < 15 || age > 130) {
            Toast.makeText(requireContext(),
                    "Age must be between 15 and 130",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        saveProfileButton.setEnabled(false);
        DBHelper db = UserSession.getDbHelper();
        boolean success = db.updateProfile(
                userEmail, name, age,
                imageUri != null ? imageUri.toString() : null,
                baseHeightCm, baseWeightKg
        );

        if (success) {
            // Populate user exercises then navigate
            UserSession.getDbHelper().seedUserExercises(userEmail);
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
