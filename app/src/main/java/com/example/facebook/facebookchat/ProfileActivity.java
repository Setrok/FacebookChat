package com.example.facebook.facebookchat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.AccessTokenManager;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class ProfileActivity extends AppCompatActivity {

    private static final int MAX_LENGTH = 32;
    private TextView profileName,profileBirthday,profileEmail,profilePhone;
    private ImageView profileImage;
    private Button backBtn,logoutBtn;
    private ProgressBar progressBar;

    private DatabaseReference userDatabase;
    private FirebaseUser currentUser;
    private StorageReference mImageStorage;

    private static final int GALLERY_INTENT_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        checkIfUserLoggedIn();

        profileImage = findViewById(R.id.settings_profile_Img);
        profileName = findViewById(R.id.profile_name);
        profileBirthday = findViewById(R.id.profile_birthday);
        profilePhone = findViewById(R.id.profile_phone);
        profileEmail = findViewById(R.id.profile_email);
        profileEmail.setVisibility(View.GONE);

        backBtn = findViewById(R.id.backToChatBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent chatIntent = new Intent(getApplicationContext(),ChatActivity.class);
                chatIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                chatIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(chatIntent);
                finish();

            }
        });


        logoutBtn = findViewById(R.id.profile_logout_btn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();

                Intent startIntent = new Intent(getApplicationContext(),StartActivity.class);
                startActivity(startIntent);
                finish();

            }
        });


//        progressBar = findViewById(R.id.settings_progressBar);


        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mImageStorage = FirebaseStorage.getInstance().getReference();

//        String phoneNumber = currentUser.getPhoneNumber();
        String userID =  AccessToken.getCurrentAccessToken().getUserId();

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        userDatabase.keepSynced(true);

        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {

                    String name = dataSnapshot.child("name").getValue().toString();
                    String birthday = dataSnapshot.child("age").getValue().toString();
                    String email = dataSnapshot.child("email").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();
                    String image_url = dataSnapshot.child("image_url").getValue().toString();

                    profileName.setText(name);
                    profileBirthday.setText("Date of birth: "+birthday);
                    profilePhone.setText("Phone: "+phone);
                    if (!email.isEmpty()){
                        profileEmail.setText("Email: "+email);
                        profileEmail.setVisibility(View.VISIBLE);
                    }
                    if(null!=image_url) {

                        //Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_user).into(profileImage);
                        Glide.with(getApplicationContext())
                                .load(image_url)
                                .apply(new RequestOptions()
                                        .placeholder(R.drawable.ic_amount))
                                .into(profileImage);
                        //.preload().onLoadStarted(getResources().getDrawable( R.drawable.default_user))


                    }
                }catch (Exception e){

                    e.printStackTrace();

                }

            }



            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void checkIfUserLoggedIn() {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser == null || null == AccessToken.getCurrentAccessToken().getUserId()){
            setToStart();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        checkIfUserLoggedIn();

    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        userDatabase.child("online").setValue(false);
//
//    }

    private void setToStart() {

        try {
            LoginManager.getInstance().logOut();
        } catch (Exception e){
            e.printStackTrace();
        }

        Intent startIntent = new Intent(getApplicationContext(),StartActivity.class);
        startActivity(startIntent);
        finish();

    }

//    private void showProgressBar(boolean b) {
//
//        changeImageBtn.setClickable(!b);
//
//        if(!b){
//
//            progressBar.setVisibility(View.INVISIBLE);
//            changeImageBtn.setVisibility(View.VISIBLE);
//
//        }else{
//
//            progressBar.setVisibility(View.VISIBLE);
//            changeImageBtn.setVisibility(View.INVISIBLE);
//
//        }
//
//
//    }

}
