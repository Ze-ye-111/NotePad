package com.example.notepad;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ImagePreviewActivity extends AppCompatActivity {
    public static final String EXTRA_IMAGE_PATH = "image_path";
    private static final int MAX_DECODE_SIZE = 4096;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_preview);

        View root = findViewById(R.id.image_viewer_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            view.setPadding(0, insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    0, insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.image_viewer_toolbar);
        toolbar.setNavigationOnClickListener(view -> finish());

        String imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        ProgressBar progress = findViewById(R.id.image_viewer_progress);
        ZoomableImageView imageView = findViewById(R.id.full_image);
        if (imagePath == null || NoteImageStore.resolve(this, imagePath) == null) {
            progress.setVisibility(View.GONE);
            Toast.makeText(this, R.string.image_load_failed, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        new Thread(() -> {
            Bitmap bitmap = NoteImageStore.decodePreview(
                    this, imagePath, MAX_DECODE_SIZE, MAX_DECODE_SIZE);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                progress.setVisibility(View.GONE);
                if (bitmap == null) {
                    Toast.makeText(this, R.string.image_load_failed, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                imageView.setBitmap(bitmap);
            });
        }, "image-preview-loader").start();
    }
}
