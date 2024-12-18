package com.example.m08_imagenes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private String lastAction = ""; // "THUMBNAIL" o "FULLSIZE"
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int CAMERA_INTENT_CODE = 123;
    private static final int REQUEST_GALLERY = 1;
    private static final int REQUEST_THUMBNAIL = 2;
    private static final int REQUEST_FULLSIZE = 3;

    private ImageView imageView;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnGallery = findViewById(R.id.btn_gallery);
        Button btnThumbnail = findViewById(R.id.btn_thumbnail);
        Button btnFullSize = findViewById(R.id.btn_fullsize);
        imageView = findViewById(R.id.image_view);

        btnGallery.setOnClickListener(v -> openGallery());

        btnThumbnail.setOnClickListener(v -> {
            lastAction = "THUMBNAIL";
            if (checkAndRequestPermissions()) {
                captureThumbnail();
            }
        });

        btnFullSize.setOnClickListener(v -> {
            lastAction = "FULLSIZE";
            if (checkAndRequestPermissions()) {
                captureFullSize();
            }
        });

    }

    private boolean checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, CAMERA_REQUEST_CODE);
            return false; // Aún no se han concedido los permisos.
        }
        return true; // Permisos ya concedidos.
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show();

                // Llama a la acción correspondiente según el botón pulsado
                if (lastAction.equals("THUMBNAIL")) {
                    captureThumbnail();
                } else if (lastAction.equals("FULLSIZE")) {
                    captureFullSize();
                }
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private void captureThumbnail() {
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera_intent, CAMERA_REQUEST_CODE);
    }

    private void captureFullSize() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.m08_imagenes.fileprovider",
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, CAMERA_INTENT_CODE);
            }
        } else {
            Toast.makeText(this, "No se puede abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(imageFileName, ".jpg", storageDir);
            currentPhotoPath = image.getAbsolutePath();
        } catch (IOException e) {
            Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show();
        }
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GALLERY:
                    Uri selectedImage = data.getData();
                    imageView.setImageURI(selectedImage);
                    break;
                case REQUEST_THUMBNAIL:
                    Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                    imageView.setImageBitmap(thumbnail);
                    break;
                case CAMERA_REQUEST_CODE:
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    imageView.setImageBitmap(photo);
                    break;
                case CAMERA_INTENT_CODE:
                    selectedImage = data.getData();
                    imageView.setImageURI(selectedImage);
                    break;
            }
        }
    }
}
