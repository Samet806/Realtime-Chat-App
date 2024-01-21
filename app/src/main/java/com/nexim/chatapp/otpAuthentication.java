package com.nexim.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class otpAuthentication extends AppCompatActivity {
 TextView mchangenumber;
 EditText mgetotp;
 Button mverifyotp;
 String enteredopt;

 FirebaseAuth firebaseAuth;
 ProgressBar mprogressbarofotpauth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_authentication);
        mchangenumber=findViewById(R.id.changeNummber);
        mverifyotp=findViewById(R.id.verifyotp);
        mgetotp=findViewById(R.id.getotp);
        mprogressbarofotpauth=findViewById(R.id.progressbarofotpauth);
        firebaseAuth=FirebaseAuth.getInstance();
        mchangenumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(otpAuthentication.this, MainActivity.class);
                startActivity(intent);
            }
        });
        mverifyotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   enteredopt=mgetotp.getText().toString();
                   if(enteredopt.isEmpty())
                   {
                       Toast.makeText(getApplicationContext(), "Enter ypur OTP first", Toast.LENGTH_SHORT).show();
                   }
                   else
                   {
                       mprogressbarofotpauth.setVisibility(View.VISIBLE);
                       String codereceiver=getIntent().getStringExtra("otp");
                       PhoneAuthCredential credential= PhoneAuthProvider.getCredential(codereceiver,enteredopt);
                       signInWithPhoneAuthCredential(credential);

                   }
            }
        });


    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                  mprogressbarofotpauth.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Login success", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(otpAuthentication.this, setProfile.class);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                    {
                        mprogressbarofotpauth.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}