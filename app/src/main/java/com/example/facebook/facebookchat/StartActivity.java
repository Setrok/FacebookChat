package com.example.facebook.facebookchat;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class StartActivity extends AppCompatActivity {

    CallbackManager callbackManager;
    TextView email,birthday;
    LoginButton loginButton;
    Button logout_btn;
    ProgressBar progressBar;
    ImageView avatar;
    AccessTokenTracker accessTokenTracker;
    LoginManager manager;
    URL profile_picture;
    String nameStr,birthdayStr,emailStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //printKeyHash();
        callbackManager = CallbackManager.Factory.create();
        manager = LoginManager.getInstance();

        avatar = findViewById(R.id.profile_avatar);
        email = findViewById(R.id.profil_email);
        birthday = findViewById(R.id.profil_birthday);
        logout_btn = findViewById(R.id.redirect_button);

        email.setVisibility(View.INVISIBLE);
        birthday.setVisibility(View.INVISIBLE);
        avatar.setVisibility(View.INVISIBLE);

        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile","email","user_birthday"));

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirect();
            }
        });

//        loginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(AccessToken.getCurrentAccessToken()!=null)
//                {
//                    Log.v("User is login","YES");
//
//                }
//                else
//                {
//                    Log.v("User is not logged in","OK");
//                    //LoginManager.getInstance().logInWithReadPermissions(WelcomeActivity1.this, (Arrays.asList("public_profile", "user_friends","user_birthday","user_about_me","email")));
//                }
//            }
//        });

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                progressBar.setVisibility(View.VISIBLE);

                //Log.i("AppInfo","Tapped");

                String accessToken = loginResult.getAccessToken().getToken();

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        progressBar.setVisibility(View.INVISIBLE);

                        Log.i("fb_response",response.toString());

                        getFacebookData(object);

                    }
                });

                //Request Graph API
                Bundle parameters = new Bundle();
                parameters.putString("fields","id,first_name,email,birthday");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {

                if (currentAccessToken == null){
                    Log.i("AppInfo","Not logged in");
                    email.setVisibility(View.INVISIBLE);
                    birthday.setVisibility(View.INVISIBLE);
                    avatar.setVisibility(View.INVISIBLE);

                } else {
                    Log.i("AppInfo","Logged in");
                    email.setVisibility(View.VISIBLE);
                    birthday.setVisibility(View.VISIBLE);
                    avatar.setVisibility(View.VISIBLE);
                    //email.setText(AccessToken.getCurrentAccessToken().getUserId());
                }
            }
        };

        if(AccessToken.getCurrentAccessToken()!=null){
            //Just set User id
            email.setText(AccessToken.getCurrentAccessToken().getUserId());
            //email.setText(AccessToken.getCurrentAccessToken().);
        }


    }

    private void getFacebookData(JSONObject object) {
        try{
            profile_picture = new URL("https://graph.facebook.com/"+object.getString("id")+"/picture?width=250&height=250");
            loadImageInto(profile_picture);

            nameStr = object.getString("first_name");
            birthdayStr = object.getString("birthday");

            try{
                emailStr = object.getString("email");
            } catch (JSONException e){
                Log.i("InfoApp","no email address");
            }

            birthday.setText(nameStr);
            email.setText(birthdayStr);

            //email.setText(object.getString("email"));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            //email.setText();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }


    private void loadImageInto(URL url){
        try{

            Uri uri = Uri.parse(url.toURI().toString());
            Glide.with(getApplicationContext()).load(uri).into(avatar);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void redirect(){

        Intent intent = new Intent(getApplicationContext(),PhoneVerification.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if(null!=nameStr && null!= birthdayStr && null!=profile_picture) {
            intent.putExtra("name", nameStr);
            intent.putExtra("birthday", birthdayStr);
            intent.putExtra("URL", profile_picture.toString());
        }
        if(null != emailStr){
            intent.putExtra("email",emailStr);
        }

        startActivity(intent);

        //manager.logOut();

    }

    public void printKeyHash(){

        try{
            PackageInfo info = getPackageManager().getPackageInfo("com.example.facebook.facebookchat", PackageManager.GET_SIGNATURES);
            for(Signature signature:info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("KeyHash", Base64.encodeToString(md.digest(),Base64.DEFAULT));
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }
}
