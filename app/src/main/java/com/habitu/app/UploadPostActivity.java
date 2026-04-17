package com.habitu.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UploadPostActivity extends AppCompatActivity {

    ImageView imgPreview;
    EditText etCaption;
    Button btnPickImage, btnPost;
    Spinner spinnerHabit;

    Uri selectedImageUri = null;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseStorage storage;

    String[] habitOptions = {
            "🏃 Running", "📚 Studying", "💪 Gym",
            "🧘 Wellness", "🥗 Nutrition", "💧 Hydration",
            "📖 Reading", "😴 Sleep", "Other"
    };

    ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedImageUri = uri;
                            imgPreview.setImageURI(uri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        mAuth   = FirebaseAuth.getInstance();
        db      = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        imgPreview    = findViewById(R.id.imgPreview);
        etCaption     = findViewById(R.id.etCaption);
        btnPickImage  = findViewById(R.id.btnPickImage);
        btnPost       = findViewById(R.id.btnPost);
        spinnerHabit  = findViewById(R.id.spinnerHabit);

        // Set up habit spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, habitOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHabit.setAdapter(adapter);

        // Pick image
        btnPickImage.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*"));

        // Post
        btnPost.setOnClickListener(v -> uploadPost());
    }

    private void uploadPost() {
        String caption = etCaption.getText().toString().trim();
        String habit   = spinnerHabit.getSelectedItem().toString();

        if (caption.isEmpty()) {
            etCaption.setError("Please write a caption");
            return;
        }

        btnPost.setEnabled(false);
        btnPost.setText("Posting...");

        if (selectedImageUri != null) {
            // Upload image to Firebase Storage first
            String fileName = UUID.randomUUID().toString();
            StorageReference ref = storage.getReference()
                    .child("posts/" + fileName);

            ref.putFile(selectedImageUri)
                    .addOnSuccessListener(snap ->
                            ref.getDownloadUrl().addOnSuccessListener(downloadUri ->
                                    savePostToFirestore(caption, habit, downloadUri.toString(), false)))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Upload failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        btnPost.setEnabled(true);
                        btnPost.setText("Post to Feed 🌱");
                    });
        } else {
            // No image — post text only
            savePostToFirestore(caption, habit, "", false);
        }
    }

    private void savePostToFirestore(String caption, String habit,
                                     String imageUrl, boolean isVideo) {
        String userId    = mAuth.getCurrentUser().getUid();
        String userEmail = mAuth.getCurrentUser().getEmail();

        // Get user's name from Firestore then save post
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    String firstName = doc.getString("firstName");
                    String surname   = doc.getString("surname");
                    String fullName  = firstName + " " + surname;

                    Map<String, Object> post = new HashMap<>();
                    post.put("userId",    userId);
                    post.put("userName",  fullName);
                    post.put("caption",   caption);
                    post.put("habit",     habit);
                    post.put("imageUrl",  imageUrl);
                    post.put("isVideo",   isVideo);
                    post.put("likes",     0);
                    post.put("timestamp", System.currentTimeMillis());

                    db.collection("posts").add(post)
                            .addOnSuccessListener(ref -> {
                                Toast.makeText(this,
                                        "Posted! Your community can see it 🌱",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this,
                                        "Error: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                btnPost.setEnabled(true);
                                btnPost.setText("Post to Feed 🌱");
                            });
                });
    }
}