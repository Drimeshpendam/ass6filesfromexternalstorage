package com.example.ass6filesfromexternalstorage;


import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.content.pm.PackageManager;

import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 100;
    private static final int REQUEST_MANAGE_STORAGE_PERMISSION = 101;

    private EditText editText;
    private TextView feedbackTextView;
    private ListView filesListView;

    private static final String DIRECTORY_NAME = "MyAppFiles";
    private static final String FILE_NAME = "user_input.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        feedbackTextView = findViewById(R.id.feedbackTextView);
        filesListView = findViewById(R.id.filesListView);

        Button createDirectoryButton = findViewById(R.id.createDirectoryButton);
        Button saveButton = findViewById(R.id.saveButton);
        Button listFilesButton = findViewById(R.id.listFilesButton);
        Button checkPermissionsButton = findViewById(R.id.checkPermissionsButton);

        createDirectoryButton.setOnClickListener(v -> createDirectory());
        saveButton.setOnClickListener(v -> saveFile());
        listFilesButton.setOnClickListener(v -> listFiles());
        checkPermissionsButton.setOnClickListener(v -> checkPermissions());
    }

    // Check if the app has necessary permissions
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_CODE);
        } else {
            Toast.makeText(this, "Permissions already granted.", Toast.LENGTH_SHORT).show();
        }

        // For Android 11 and above, check if MANAGE_EXTERNAL_STORAGE permission is granted
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                // If permission not granted, request permission
                requestManageStoragePermission();
            }
        }
    }

    private void requestManageStoragePermission() {
        // Ask the user to grant permission to manage external storage (Android 11 and higher)
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        startActivityForResult(intent, REQUEST_MANAGE_STORAGE_PERMISSION);
    }

    // Handle the permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Create a directory in external storage
    private void createDirectory() {
        if (isExternalStorageWritable()) {
            File directory = new File(Environment.getExternalStorageDirectory(), DIRECTORY_NAME);
            if (!directory.exists()) {
                if (directory.mkdirs()) {
                    feedbackTextView.setText("Directory created: " + directory.getPath());
                    Toast.makeText(this, "Directory created successfully", Toast.LENGTH_SHORT).show();
                } else {
                    feedbackTextView.setText("Failed to create directory.");
                    Toast.makeText(this, "Failed to create directory", Toast.LENGTH_SHORT).show();
                }
            } else {
                feedbackTextView.setText("Directory already exists.");
            }
        }
    }

    // Save the file with user input
    private void saveFile() {
        String userInput = editText.getText().toString().trim();
        if (userInput.isEmpty()) {
            Toast.makeText(this, "Please enter some text.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isExternalStorageWritable()) {
            File directory = new File(Environment.getExternalStorageDirectory(), DIRECTORY_NAME);
            File file = new File(directory, FILE_NAME);

            try (FileOutputStream fos = new FileOutputStream(file);
                 OutputStreamWriter writer = new OutputStreamWriter(fos)) {
                writer.write(userInput);
                feedbackTextView.setText("File saved to: " + file.getAbsolutePath());
                Toast.makeText(this, "File saved successfully.", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                feedbackTextView.setText("Error saving file: " + e.getMessage());
                Toast.makeText(this, "Error saving file.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Check if external storage is writable
    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    // List all files in the directory
    private void listFiles() {
        if (isExternalStorageWritable()) {
            File directory = new File(Environment.getExternalStorageDirectory(), DIRECTORY_NAME);
            File[] files = directory.listFiles();

            if (files != null && files.length > 0) {
                String[] fileNames = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                    fileNames[i] = files[i].getName();
                }
                // Create an ArrayAdapter to display file names in ListView
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileNames);
                filesListView.setAdapter(adapter);
            } else {
                feedbackTextView.setText("No files found.");
                Toast.makeText(this, "No files found.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
