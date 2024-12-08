package com.example.plantify_user.userSettings;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.plantify_user.R;
import com.example.plantify_user.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class userSettings extends Fragment {

    public userSettings() {
        // Required empty public constructor
    }
    private int PickImageCode = 3;
    private EditText usernameEditText, contactEditText, addressEditText, zipcodeEditText;
    private ImageView profileImage;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private Uri uri;
    private Bitmap bitmap;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private MaterialButton uploadPicture, editButton;
    private ValueEventListener userListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_settings, container, false);
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        String userid = firebaseAuth.getUid();

        usernameEditText = view.findViewById(R.id.usernameEditText);
        contactEditText = view.findViewById(R.id.contactEditText);
        addressEditText = view.findViewById(R.id.addressEditText);
        zipcodeEditText = view.findViewById(R.id.zipcodeEditText);
        profileImage = view.findViewById(R.id.profileImage);
        uploadPicture = view.findViewById(R.id.uploadPicture);
        editButton = view.findViewById(R.id.editButton);

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PickImageCode);
        });

        editButton.setOnClickListener(v -> EdituserBasicInfo(userid));

        uploadPicture.setOnClickListener(v -> {
            if (bitmap != null) {
                uploadImageToFirebase(bitmap);
            } else {
                Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show();
            }
        });

        userListener = firebaseDatabase.getReference("Users").child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) return; // Check fragment lifecycle

                UserModel userModel = snapshot.getValue(UserModel.class);
                if (userModel != null) {
                    usernameEditText.setText(userModel.getUsername());
                    contactEditText.setText(userModel.getContact());
                    addressEditText.setText(userModel.getAddress());
                    zipcodeEditText.setText(userModel.getZipcode());

                    Glide.with(requireContext()) // Safe context
                            .load(userModel.getProfile())
                            .error(R.drawable.plant_logo)
                            .into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PickImageCode && data != null && data.getData() != null) {
            uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToFirebase(Bitmap bitmap) {
        String userid = firebaseAuth.getUid();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();

        String uuid = UUID.randomUUID().toString();
        StorageReference storageReference = firebaseStorage.getReference().child("Images/" + uuid);

        storageReference.putBytes(imageBytes).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    Map<String, Object> profileUpdate = new HashMap<>();
                    profileUpdate.put("Profile", uri.toString());
                    firebaseDatabase.getReference("Users").child(userid).updateChildren(profileUpdate);
                    Toast.makeText(requireContext(), "Profile Uploaded Successfully", Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void EdituserBasicInfo(String userid) {
        Map<String, Object> editUser = new HashMap<>();
        editUser.put("Contact", contactEditText.getText().toString());
        editUser.put("address", addressEditText.getText().toString());
        editUser.put("username", usernameEditText.getText().toString());
        editUser.put("zipcode", zipcodeEditText.getText().toString());

        firebaseDatabase.getReference("Users").child(userid).updateChildren(editUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), "User Info Updated Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to Update User Info", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove Firebase listener to prevent memory leaks
        if (firebaseAuth.getUid() != null && userListener != null) {
            firebaseDatabase.getReference("Users").child(firebaseAuth.getUid()).removeEventListener(userListener);
        }
    }
}
