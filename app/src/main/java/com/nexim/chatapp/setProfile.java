package com.nexim.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class setProfile extends AppCompatActivity {
    private TextView mygetusername;
    private CardView mgetuserimage;
    private ImageView mgetuserimageinimageview;
    private static final int PICK_IMAGE=123;
    private Uri imagepath;
    private Button msaveprofile;
    private FirebaseAuth firebaseAuth;
    private String name;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private String imageUriAccessToken;
    private FirebaseFirestore firebaseFirestore;
    private ProgressBar myprogressbarofsetprofile;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();

        mygetusername=findViewById(R.id.getusername);
        mgetuserimage=findViewById(R.id.getuserimage);
        mgetuserimageinimageview=findViewById(R.id.getuserimageinimageview);
        msaveprofile=findViewById(R.id.saveProfile);
        myprogressbarofsetprofile=findViewById(R.id.progressbarofsetProfile);

        mgetuserimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        msaveprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name=mygetusername.getText().toString();
                if(TextUtils.isEmpty(name))
                {
                    Toast.makeText(getApplicationContext(), "Name Boş olamaz", Toast.LENGTH_SHORT).show();
                }
                else if(imagepath==null){
                    Toast.makeText(getApplicationContext(), "Image is empty", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    myprogressbarofsetprofile.setVisibility(View.VISIBLE);
                    sendDataForNewUser();
                    myprogressbarofsetprofile.setVisibility(View.INVISIBLE);
                    Intent intent =new Intent(setProfile.this, chatActivity.class);
                    startActivity(intent);
                    finish();

                }
            }
        });

    }
    private void sendDataForNewUser()
    {
      sendDataToRealTimeDatabase();


    }
    private void sendDataToRealTimeDatabase()
    {
         name=mygetusername.getText().toString().trim();
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        DatabaseReference databaseReference=firebaseDatabase.getReference(firebaseAuth.getUid());

        Userprofile userprofile=new Userprofile(name,firebaseAuth.getUid());
        databaseReference.setValue(userprofile);
        Toast.makeText(getApplicationContext(), "USer profile added succesfully", Toast.LENGTH_SHORT).show();
        sendImagetoStorage();
    }
private void sendImagetoStorage()
{
    StorageReference imageref=storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Pic");
    Bitmap bitmap=null;
    try{
        bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),imagepath);
    }catch(IOException e)
    {
        e.printStackTrace();
    }
    ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
    byte[] data=byteArrayOutputStream.toByteArray();
    // putting image to storage
    UploadTask uploadTask=imageref.putBytes(data);
    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            imageref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    imageUriAccessToken=uri.toString();
                    Toast.makeText(getApplicationContext(), "Uri get success", Toast.LENGTH_SHORT).show();
                    sendDataToCloudFirestore();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "URI get Failed", Toast.LENGTH_SHORT).show();
                }
            });
            Toast.makeText(getApplicationContext(), "Image is uploaded", Toast.LENGTH_SHORT).show();
        }
    }).addOnFailureListener(new OnFailureListener() {
    @Override
    public void onFailure(@NonNull Exception e) {
        Toast.makeText(getApplicationContext(), "Image not  uploaded", Toast.LENGTH_SHORT).show();
    }
});
}
 private void sendDataToCloudFirestore()
 {
     DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
     Map<String, Object> userdata=new HashMap<>();
     userdata.put("name",name);
     userdata.put("image",imageUriAccessToken);
     userdata.put("uid",firebaseAuth.getUid());
     userdata.put("status","Online");
     documentReference.set(userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
         @Override
         public void onSuccess(Void unused) {
             Toast.makeText(getApplicationContext(), "Data on Cloud firestore send success", Toast.LENGTH_SHORT).show();
         }
     });

 }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imagepath = data.getData();
            mgetuserimageinimageview.setImageURI(imagepath);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



}