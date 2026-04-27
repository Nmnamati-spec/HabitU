package com.habitu.app;

import android.widget.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UploadPostActivity extends AppCompatActivity {

    ImageView imgPreview;
    VideoView videoPreview;
    TextView tvMediaType;
    EditText etCaption;
    Button btnPickImage, btnPickVideo, btnPost;
    Spinner spinnerHabit;

    Uri selectedMediaUri = null;
    boolean isVideoSelected = false;
    MediaController mediaController;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseStorage storage;

    List<String> habitOptions = Arrays.asList(
            "Running", "Studying", "Gym",
            "Wellness", "Nutrition", "Hydration",
            "Reading", "Sleep", "Other"
    );

    ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedMediaUri = uri;
                            isVideoSelected = false;
                            imgPreview.setVisibility(View.VISIBLE);
                            imgPreview.setImageURI(uri);
                            videoPreview.setVisibility(View.GONE);
                            videoPreview.stopPlayback();
                            tvMediaType.setText("Photo selected");
                            tvMediaType.setVisibility(View.VISIBLE);
                        }
                    });

    ActivityResultLauncher<String> videoPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedMediaUri = uri;
                            isVideoSelected = true;
                            imgPreview.setVisibility(View.GONE);
                            videoPreview.setVisibility(View.VISIBLE);
                            videoPreview.setVideoURI(uri);
                            videoPreview.start();
                            tvMediaType.setText("Video selected");
                            tvMediaType.setVisibility(View.VISIBLE);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        mAuth   = FirebaseAuth.getInstance();
        db      = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        imgPreview   = findViewById(R.id.imgPreview);
        videoPreview = findViewById(R.id.videoPreview);
        tvMediaType  = findViewById(R.id.tvMediaType);
        etCaption    = findViewById(R.id.etCaption);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnPickVideo = findViewById(R.id.btnPickVideo);
        btnPost      = findViewById(R.id.btnPost);
        spinnerHabit = findViewById(R.id.spinnerHabit);

        HabitSpinnerAdapter adapter = new HabitSpinnerAdapter(this, habitOptions);
        spinnerHabit.setAdapter(adapter);

        mediaController = new MediaController(this);
        mediaController.setAnchorView(videoPreview);
        videoPreview.setMediaController(mediaController);

        btnPickImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnPickVideo.setOnClickListener(v -> videoPickerLauncher.launch("video/*"));
        btnPost.setOnClickListener(v -> uploadPost());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoPreview.isPlaying()) videoPreview.pause();
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

        if (selectedMediaUri != null) {
            String folder   = isVideoSelected ? "posts/videos/" : "posts/";
            String fileName = UUID.randomUUID().toString();
            StorageReference ref = storage.getReference().child(folder + fileName);

            ref.putFile(selectedMediaUri)
                    .addOnSuccessListener(snap ->
                            ref.getDownloadUrl().addOnSuccessListener(downloadUri ->
                                    savePostToFirestore(caption, habit,
                                            downloadUri.toString(), isVideoSelected)))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Upload failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        btnPost.setEnabled(true);
                        btnPost.setText("Post to Feed");
                    });
        } else {
            savePostToFirestore(caption, habit, "", false);
        }
    }

    private void savePostToFirestore(String caption, String habit,
                                     String mediaUrl, boolean isVideo) {
        String userId = mAuth.getCurrentUser().getUid();

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
                    post.put("imageUrl",  mediaUrl);
                    post.put("isVideo",   isVideo);
                    post.put("likes",     0);
                    post.put("timestamp", System.currentTimeMillis());

                    db.collection("posts").add(post)
                            .addOnSuccessListener(ref -> {
                                Toast.makeText(this,
                                        "Posted! Your community can see it",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this,
                                        "Error: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                btnPost.setEnabled(true);
                                btnPost.setText("Post to Feed");
                            });
                });
    }
}
