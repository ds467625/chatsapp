package com.example.chatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText phone;
    private EditText otp;
    private Button submit;
    private String verificationId;
    private boolean manualCode = false;
    private Toolbar toolbar;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private boolean codeSent = false;
    private Dialog verficationDialog;
    private Button verifyBtn;
    private TextView verifyText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phone = findViewById(R.id.phone);
        submit = findViewById(R.id.submit);
        toolbar = findViewById(R.id.toolbar);

        verficationDialog = new Dialog(LoginActivity.this);
        verficationDialog.setContentView(R.layout.verfication_dialog);
        verficationDialog.setCancelable(true);
        verficationDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        verficationDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_background));
        otp = verficationDialog.findViewById(R.id.dialog_code);
        verifyBtn = verficationDialog.findViewById(R.id.dialog_btn);
        verifyText = verficationDialog.findViewById(R.id.veri_text);

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!otp.getText().toString().equals("")){
                    verifyCode(otp.getText().toString());
                    verficationDialog.dismiss();
                } else{
                    Toast.makeText(LoginActivity.this, "Invalid code", Toast.LENGTH_SHORT).show();
                }

            }
        });



        getSupportActionBar();
        toolbar.setTitle("Login");


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(phone.getText()) && phone.getText().toString().length() == 10) {

                   sendVerificationCode(phone.getText().toString());
                   verifyText.setText("Please enter verification code which has been sent successfully +91 "+phone.getText().toString());
                   verficationDialog.show();

                }
                else {
                    Toast.makeText(LoginActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                        Toast.makeText(LoginActivity.this, "New user", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, UserInformation.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Exitsing User", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void sendVerificationCode(String number) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber("+91" + number, 60, TimeUnit.SECONDS, TaskExecutors.MAIN_THREAD, mCallBacks);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            Toast.makeText(LoginActivity.this, "Successfully sent verification code to +91 " + phone.getText().toString(), Toast.LENGTH_SHORT).show();
            verificationId = s;
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };
}
