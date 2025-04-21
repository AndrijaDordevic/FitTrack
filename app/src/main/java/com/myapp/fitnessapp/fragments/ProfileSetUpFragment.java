package com.myapp.fitnessapp.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.database.DBHelper;

public class ProfileSetUpFragment extends Fragment {

    private ImageView profileImageView;
    private EditText fullNameEditText, ageEditText;
    private Button saveProfileButton;
    private Uri imageUri;
    private String userEmail;    // ← will hold the logged‑in user’s email

    // Launcher for picking from gallery
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

    // Launcher for taking a camera preview (thumbnail)
    private final ActivityResultLauncher<Void> takePictureLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicturePreview(),
                    bitmap -> {
                        if (bitmap != null) {
                            profileImageView.setImageBitmap(bitmap);
                            // If you need a Uri, write the bitmap to a file and set imageUri accordingly
                        }
                    }
            );

    public ProfileSetUpFragment() {
        // Required empty public constructor
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_setup, container, false);

        profileImageView  = view.findViewById(R.id.profile_image_view);
        fullNameEditText  = view.findViewById(R.id.full_name_edit_text);
        ageEditText       = view.findViewById(R.id.age_edit_text);
        saveProfileButton = view.findViewById(R.id.save_profile_button);

        // 1) Grab email (and username) passed in from SignUpFragment
        Bundle args = getArguments();
        if (args != null) {
            userEmail = args.getString("email");
            String username = args.getString("username");
            if (username != null) {
                fullNameEditText.setText(username);
            }
        }

        profileImageView.setOnClickListener(v -> showImageSourceDialog());
        saveProfileButton.setOnClickListener(v -> onSaveProfile());

        return view;
    }

    private void showImageSourceDialog() {
        String[] options = {"Choose from gallery", "Take a photo"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Select profile image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Gallery
                        Intent pick = new Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        );
                        pickImageLauncher.launch(pick);
                    } else {
                        // Camera
                        takePictureLauncher.launch(null);
                    }
                })
                .show();
    }

    private void onSaveProfile() {
        String name = fullNameEditText.getText().toString().trim();
        String age  = ageEditText.getText().toString().trim();

        if (name.isEmpty() || age.isEmpty()) {
            Toast.makeText(requireContext(),
                            "Please complete all fields",
                            Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        // 2) Persist to DB
        DBHelper db = new DBHelper(requireContext());
        boolean success = db.updateProfile(
                userEmail,
                name,
                Integer.parseInt(age),
                (imageUri != null ? imageUri.toString() : null)
        );

        // 3) Feedback + navigate
        if (success) {
            Toast.makeText(requireContext(),
                            "Profile Saved!",
                            Toast.LENGTH_SHORT)
                    .show();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_profile_to_dashboard);
        } else {
            Toast.makeText(requireContext(),
                            "Failed to save profile",
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
