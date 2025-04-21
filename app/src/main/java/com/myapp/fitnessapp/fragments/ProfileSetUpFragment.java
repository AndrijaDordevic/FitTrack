package com.myapp.fitnessapp.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myapp.fitnessapp.R;

public class ProfileSetUpFragment extends Fragment {

    private ImageView profileImageView;
    private EditText fullNameEditText, ageEditText;
    private Button saveProfileButton;
    private Uri imageUri;

    // New ActivityResultLauncher
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    profileImageView.setImageURI(imageUri);
                }
            });

    public ProfileSetUpFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_setup, container, false);

        profileImageView = view.findViewById(R.id.profile_image_view);
        fullNameEditText = view.findViewById(R.id.full_name_edit_text);
        ageEditText = view.findViewById(R.id.age_edit_text);
        saveProfileButton = view.findViewById(R.id.save_profile_button);

        // Get data from SignUpFragment
        Bundle args = getArguments();
        if (args != null) {
            String email = args.getString("email");
            String username = args.getString("username");
            fullNameEditText.setText(username);
        }

        profileImageView.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(pickPhoto);  // ✅ Launch using new launcher
        });

        saveProfileButton.setOnClickListener(v -> {
            String name = fullNameEditText.getText().toString().trim();
            String age = ageEditText.getText().toString().trim();

            if (name.isEmpty() || age.isEmpty()) {
                Toast.makeText(getContext(), "Please complete all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Save profile data here

            Toast.makeText(getContext(), "Profile Saved!", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
