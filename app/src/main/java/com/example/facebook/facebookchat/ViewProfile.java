package com.example.facebook.facebookchat;

import android.content.Intent;
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
import com.example.facebook.facebookchat.mpesa.MPesaActivity;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ViewProfile extends AppCompatActivity {

    private TextView profileName,profileBirthday,profileEmail,profilePhone;
    private ImageView profileImage;
    private Button backBtn,viewPhoneBtn;
    private ProgressBar progressBar;

    private DatabaseReference userDatabase;
    private DatabaseReference userRef;
    private int payments_counter;
//    private FirebaseUser currentUser;
//    private StorageReference mImageStorage;

    private static final int GALLERY_INTENT_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        checkIfUserLoggedIn();

        if(!getIntent().hasExtra("userID")){
            Intent chatIntent = new Intent(getApplicationContext(),ChatActivity.class);
            startActivity(chatIntent);
            finish();
        }

        profileImage = findViewById(R.id.view_profile_Img);
        profileName = findViewById(R.id.view_profile_name);
        profileBirthday = findViewById(R.id.view_profile_birthday);
        profileEmail = findViewById(R.id.view_profile_email);
        profilePhone = findViewById(R.id.view_profile_phone);
        profileEmail.setVisibility(View.GONE);
        profilePhone.setVisibility(View.GONE);

        backBtn = findViewById(R.id.view_backToChatBtn);
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

//        progressBar = findViewById(R.id.settings_progressBar);


        //currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //mImageStorage = FirebaseStorage.getInstance().getReference();

//        String phoneNumber = currentUser.getPhoneNumber();
        String userID =  getIntent().getStringExtra("userID");

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        userDatabase.keepSynced(true);

        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {

                    String image_url = dataSnapshot.child("image_url").getValue().toString();
                    String name = dataSnapshot.child("name").getValue().toString();
                    String birthday = dataSnapshot.child("age").getValue().toString();
                    String email = dataSnapshot.child("email").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();

                    profileName.setText(name);
                    profileBirthday.setText(birthday);
                    profilePhone.setText(phone);
                    if(!email.isEmpty()) {
                        profileEmail.setText(email);
                        profileEmail.setVisibility(View.VISIBLE);
                    }
                    //profileEmail.setText(email);
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

        viewPhoneBtn = findViewById(R.id.view_showPhoneBtn);
        viewPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getSearchesLeft();

                search();

            }
        });

    }


    private void getSearchesLeft() {

        final String currentUserID =  AccessToken.getCurrentAccessToken().getUserId();

        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {

                    if(!dataSnapshot.hasChild("search_left")) {
                        dataSnapshot.getRef().child("search_left").setValue(0);
                        payments_counter = 0;
                    }
                    else
                        payments_counter = Integer.parseInt(dataSnapshot.child("search_left").getValue().toString());

                    //searches_left.setText("Searches left: " + payments_counter);

                }catch (NumberFormatException | NullPointerException e){
                    userRef.child(currentUserID).child("search_left").setValue(0);
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void search(){

        final String currentUserID =  AccessToken.getCurrentAccessToken().getUserId();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                //final String searchPhone = searchFld.getText().toString();


                try{

                    payments_counter = Integer.parseInt(dataSnapshot.child(currentUserID).child("search_left").getValue().toString());

                } catch (NumberFormatException e){

                    userRef.child(currentUserID).child("search_left").setValue(0);

                } catch (NullPointerException e){

                    userRef.child(currentUserID).child("search_left").setValue(0);

                } catch (Exception e){

                    Toast.makeText(getApplicationContext(),"Could't get payment data",Toast.LENGTH_LONG).show();

                }

                if(payments_counter<=0){

                    Toast.makeText(getApplicationContext(),"Payment required",Toast.LENGTH_LONG).show();

                    Intent mpesaIntent = new Intent(getApplicationContext(), MPesaActivity.class);
                    startActivity(mpesaIntent);

                    //noMatchFld.setVisibility(View.INVISIBLE);

                }
                else {

//                    if (dataSnapshot.hasChild(searchPhone)) {

                        payments_counter = Integer.parseInt(dataSnapshot.child(currentUserID).child("search_left").getValue().toString());
                        payments_counter -= 1;
                        userRef.child(currentUserID).child("search_left").setValue(payments_counter).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                profilePhone.setVisibility(View.VISIBLE);

//                                Intent searchProfileIntent = new Intent(SearchActivity.this,SearchPrifileActivity.class);
//                                searchProfileIntent.putExtra("search_phone",searchPhone);
//                                startActivity(searchProfileIntent);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(getApplicationContext(),"Search error",Toast.LENGTH_LONG).show();

                            }
                        });

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
        } else {
            getSearchesLeft();
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
}
