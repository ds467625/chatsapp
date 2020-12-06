package com.example.chatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatsapp.Model.UserModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserInformation extends AppCompatActivity {

    private EditText name;
    private Button submit;
    private DatabaseReference reference;
    private CircleImageView circleImageView;
    private TextView username;

    private FirebaseUser user;
    private int FROM_MAIN;

    private static final int IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST = 2;
    StorageReference storageReference;
    private Uri imageUri;
    private StorageTask uploadTask;
    private Toolbar toolbar;

    private Dialog lodingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_information);


        name = findViewById(R.id.inf_user_name);
        submit = findViewById(R.id.inf_submit);
        circleImageView = findViewById(R.id.profile);
        username = findViewById(R.id.username);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Profile");

        lodingDialog = new Dialog(UserInformation.this);
        lodingDialog.setCancelable(false);


        FROM_MAIN = getIntent().getIntExtra("FROM_MAIN", -1);
        storageReference = FirebaseStorage.getInstance().getReference("Uploads");

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());


        if (FROM_MAIN == 0) {

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
                    username.setText(userModel.getTitle());
                    if (userModel.getImage().equals("default")) {
                        circleImageView.setImageResource(R.drawable.ic_action_name);
                    } else {
                        Glide.with(UserInformation.this).load(userModel.getImage()).into(circleImageView);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        openImageIntent();
                    } else {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
                    }
                } else {
                    openImageIntent();
                }

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(name.getText())) {
                    username.setText(name.getText().toString());

                    if (FROM_MAIN == 0) {
                    } else {
                        registerUser();
                    }
                } else {
                    Toast.makeText(UserInformation.this, "Invalid name", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImageIntent();
            } else {
                Toast.makeText(UserInformation.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registerUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("title", name.getText().toString());
        map.put("phone", user.getPhoneNumber());
        map.put("image", "default");
        map.put("status", "online");

        reference.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(UserInformation.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(UserInformation.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void openImageIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {

        final ProgressDialog dialog = new ProgressDialog(UserInformation.this);
        dialog.setMessage("Updating");
        dialog.setCancelable(false);
        dialog.show();
        if (imageUri != null) {
            final StorageReference filereference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            uploadTask = filereference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filereference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloaduri = task.getResult();
                        String mUri = downloaduri.toString();
                        DatabaseReference mreference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                        HashMap<String, Object> map = new HashMap();
                        map.put("image", mUri);
                        mreference.updateChildren(map);
                        Glide.with(UserInformation.this).load(mUri).into(circleImageView);
                        //todo:loding dis,miss
                        dialog.dismiss();
                    } else {
                        Toast.makeText(UserInformation.this, "Faild to upload", Toast.LENGTH_SHORT).show();
                        //todo:loding dismiss
                        dialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UserInformation.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    //todo:loding dismiss
                    dialog.dismiss();
                }
            });
        } else {
            Toast.makeText(UserInformation.this, "No image found", Toast.LENGTH_SHORT).show();
            //todo:loding dismiss
            dialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(this, "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();

            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return false;
    }
}
