package com.example.facebook.facebookchat;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements MessageAdapter.loadImageInterface{

//    Button profileBtn;

    //    String chatUserId;
    String chatUserPic;
    String chatUserName;

    private Toolbar mChatToolbar;
    private ProgressBar chatProgressBar;
    private LinearLayout chatInputLayout;

    private TextView nameView;
    private EditText messageView;
    private CircleImageView profileImage;
    private ImageView onlinePic;
    private ImageButton sendBtn,addBtn;
    private RecyclerView mMessagesList;
    private SwipeRefreshLayout swipeLayout;

    private DatabaseReference rootRef;
    private StorageReference mImageStorage;
    private FirebaseUser currentUser;
    private final List<Messages> messageList = new ArrayList<>();

    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private static final int TOTAL_ITEMS_TO_LOAD = 15;

    private int mCurrentPage = 0;
    //private static String chatName,currentName;

    private int itemPos = 0;
    private boolean loaded = false;

    int messageIncrement = 5;
    private String lastKey = "",previousKey="prev";

    private static final int GALLERY_PICK = 1;

    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if(!checkIfUserLoggedIn()) return;

        mUser = FirebaseAuth.getInstance().getCurrentUser();

//        profileBtn = findViewById(R.id.chat_goto_accountBtn);
//        profileBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent profileIntent = new Intent(getApplicationContext(),ProfileActivity.class);
//                startActivity(profileIntent);
//
//            }
//        });

        if(savedInstanceState!=null){
            mCurrentPage = savedInstanceState.getInt("messages");
        }
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser == null)
            setToStart();

        mImageStorage = FirebaseStorage.getInstance().getReference();

        mChatToolbar = findViewById(R.id.chat_app_bar);

        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setElevation(60);

        rootRef = FirebaseDatabase.getInstance().getReference();

//        if(getIntent().hasExtra("user_id")) {
//            chatUserId = getIntent().getStringExtra("user_id");
//            chatUserName = getIntent().getStringExtra("user_name");
//            chatUserPic = getIntent().getStringExtra("user_pic");
//        }

        //actionBar.setTitle(chatUserName);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View acttion_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(acttion_bar_view);

        nameView = findViewById(R.id.custom_bar_name);
        //lastSeenView = findViewById(R.id.custom_bar_lastSeen);
        profileImage = findViewById(R.id.chat_profile_Pic);
        onlinePic = findViewById(R.id.custom_onlinPic);
        onlinePic.setVisibility(View.INVISIBLE);

        messageView = findViewById(R.id.chat_messageView);
        sendBtn = findViewById(R.id.chat_sendBtn);
        addBtn = findViewById(R.id.chat_addBtn);

        mMessagesList = findViewById(R.id.conversation_list);
        swipeLayout = findViewById(R.id.chat_swipe_layout);

        chatInputLayout = findViewById(R.id.chat_input_layout);

        chatProgressBar = findViewById(R.id.chat_progressBar);
        chatProgressBar.setVisibility(View.GONE);

        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        //nameView.setText(chatUserName);

        //Go To Profile
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent profileIntent = new Intent(getApplicationContext(),ProfileActivity.class);
                startActivity(profileIntent);

            }
        });

        rootRef.child("Users").child(AccessToken.getCurrentAccessToken().getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                chatUserName = dataSnapshot.child("name").getValue().toString();
                chatUserPic = dataSnapshot.child("image_url").getValue().toString();
                nameView.setText(chatUserName);

//                String data[] = new String[2];
//                data[0] = chatUserPic;
//                data[1] = chatUserName;

                mAdapter = new MessageAdapter(messageList, ChatActivity.this);
                mMessagesList.setAdapter(mAdapter);
                //loadMessages();
                loadMessagesLast(true);

//                lastSeenView.setText(online);
//                if(online.equals("online")) {
//                    onlinePic.setVisibility(View.VISIBLE);
//                    lastSeenView.setText(online);
//                } else {
//                    onlinePic.setVisibility(View.INVISIBLE);
//                    try {
//                        String timeAgo = GetTimeAgo.getTimeAgo(Long.parseLong(online), getApplicationContext());
//                        if(timeAgo!=null) {
//                            String last_seen = "Last seen: " + timeAgo;
//                            lastSeenView.setText(last_seen);
//                        } else lastSeenView.setText(R.string.seen_recently);
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }

//                loadImageInto("https://graph.facebook.com/599979763700408/picture?width=250&height=250",profileImage);
                loadImageInto(chatUserPic, profileImage);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//        if(currentUser!=null) {
//
//            rootRef.child("Chat").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//
//                    if(!dataSnapshot.hasChild(chatUserId)){
//
//                        Map chatAddMap = new HashMap();
//                        chatAddMap.put("seen",false);
//                        chatAddMap.put("timestamp", ServerValue.TIMESTAMP);
//
//                        Map chatUserMap = new HashMap();
//                        chatUserMap.put("Chat/" + currentUser.getUid() + "/" + chatUserId, chatAddMap);
//                        chatUserMap.put("Chat/" + chatUserId + "/" + currentUser.getUid(),chatAddMap);
//                        rootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
//                            @Override
//                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//
//                                if(databaseError != null){
//
//                                    Log.d("InfoApp","damn" + databaseError.getMessage().toString());
//
//                                }
//
//                            }
//                        });
//
//                    }
//
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//
//        }

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();

            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);

            }
        });

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                itemPos = 0;
                //messageList.clear();

                //loadMoreMessages();
                loadMessagesLast(false);

            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("messages",mCurrentPage);
        super.onSaveInstanceState(outState);
    }

    private void loadMessagesLast(final boolean firstLaunch){

//        DatabaseReference messageRef = rootRef.child("messages").child(currentUser.getUid()).child(chatUserId);
        DatabaseReference messageRef = rootRef.child("messages");

        Query messageQuery;
        if(lastKey.equals(""))
            messageQuery = messageRef.limitToLast(TOTAL_ITEMS_TO_LOAD);
        else
            messageQuery = messageRef.orderByKey().endAt(lastKey).limitToLast(messageIncrement+1);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot!=null){

                    Log.i("InfoApp", "-------------------------------Launched QUERRY-----------------------------= ");

                    Messages message = dataSnapshot.getValue(Messages.class);

                    if(firstLaunch){

                        messageList.add(message);

                        Log.i("InfoApp","first launch Item Pos = " +itemPos+" ------------message:" + message.getMessage());

                        itemPos++;

                        if(itemPos == TOTAL_ITEMS_TO_LOAD) {
                            Log.i("InfoApp","loaded is true now");
                            loaded=true;
                        }

                    } else {
                        if(itemPos<messageIncrement && !lastKey.equals(dataSnapshot.getKey()) && !previousKey.equals(dataSnapshot.getKey())) {
                            Log.i("InfoApp"," Item Pos = " +itemPos+" ------------message:" + message.getMessage());
                            messageList.add(itemPos++, message);
                        }
                        //else itemPos++;

                    }

                    if(itemPos == 1 ){
                        previousKey = lastKey;
                        lastKey = dataSnapshot.getKey();
                    }

                    mAdapter.notifyDataSetChanged();

                    if(firstLaunch)
                        mMessagesList.scrollToPosition(messageList.size() -1);
                    else
                        mLinearLayout.scrollToPositionWithOffset(itemPos, 0);

                    swipeLayout.setRefreshing(false);

                } else {
                    swipeLayout.setRefreshing(false);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

//    public static String getChatName(){
//
//        return chatName;
//
//    }
//
//    public static String getCurrentUserName(){
//
//        return currentName;
//
//    }

    private void sendMessage() {

        if(!checkNetwork())
        return;

        String message = messageView.getText().toString();

        if(!TextUtils.isEmpty(message)){

//            String currentUserRef = "messages/" + currentUser.getUid() + "/" + chatUserId;
//            String chatUserRef = "messages/" + chatUserId + "/" + currentUser.getUid();

            String currentUserRef = "messages/";
//            String chatUserRef = "messages/";

            DatabaseReference user_message_push = rootRef.child("messages").push();

            String pushID = user_message_push.getKey();

            String nameOfUser = chatUserName.trim().split(" ")[0];

//            Log.w("GLOBAL CHAT NAME",""+nameOfUser);

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("name",nameOfUser);
            messageMap.put("profilePic",chatUserPic);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",AccessToken.getCurrentAccessToken().getUserId());

            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef + "/" + pushID,messageMap);
            //messageUserMap.put(chatUserRef + "/" + pushID,messageMap);

            messageView.setText("");

            rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError != null) {
                        Log.d("InfoApp", "message damn " + databaseError.getMessage().toString());
                    }else {
                        if(messageList.size()!=0) {
//                mMessagesList.smoothScrollToPosition(messageList.size() - 1);
//                mMessagesList.scrollToPosition(messageList.size()-1);
                            mLinearLayout.scrollToPositionWithOffset(messageList.size()-1,0);
                        }
                    }

                }
            });

        }

    }

    protected void onStart() {
        super.onStart();

            if(!checkNetwork())
                return;

        checkIfUserLoggedIn();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode ==RESULT_OK){

            showPB(true);

            if(data.getData()!=null) {
                Uri imageUri = data.getData();

                loadMessageImage(imageUri);

            } else {

                if(data.getClipData()!=null){
                    ClipData mClipData=data.getClipData();

                    for(int i=0;i<mClipData.getItemCount();i++){

                        ClipData.Item item = mClipData.getItemAt(i);
                        Uri uri = item.getUri();
                        loadMessageImage(uri);

                    }

                }

            }

        }
    }

    private void loadMessageImage(Uri imageUri) {

                if(!checkNetwork()){
                    return;
            }

//        final String current_user_ref = "messages/" + currentUser.getUid() + "/" + chatUserId;
//        final String chat_user_ref = "messages/" + chatUserId + "/" + currentUser.getUid();

        final String current_user_ref = "messages/";

        DatabaseReference userMessagePush = rootRef.child("messages").push();

        final String push_id = userMessagePush.getKey();

        StorageReference filePath = mImageStorage.child("message_images").child(push_id + ".jpg");

        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()) {

                    String downloadUrl = task.getResult().getDownloadUrl().toString();

                    String nameOfUser = chatUserName.trim().split(" ")[0];

                    Map messageMap = new HashMap();
                    messageMap.put("message", downloadUrl);
                    messageMap.put("name",nameOfUser);
                    messageMap.put("profilePic",chatUserPic);
                    messageMap.put("type", "image");
                    messageMap.put("time", ServerValue.TIMESTAMP);
                    messageMap.put("from", AccessToken.getCurrentAccessToken().getUserId());

                    Map messageUserMap = new HashMap();
                    messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                    //messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                    messageView.setText("");

                    rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {
                                Log.d("InfoApp", databaseError.getMessage().toString());
                            }

                            showPB(false);

                        }
                    });

                } else{
                    showPB(false);
                    Log.e("InfoApp","Error loading image into fb");
                }

            }
        });

    }

    private void showPB(boolean show){

        chatInputLayout.setEnabled(!show);

        if(show){

            chatInputLayout.setVisibility(View.INVISIBLE);
            chatProgressBar.setVisibility(View.VISIBLE);

        } else {

            chatInputLayout.setVisibility(View.VISIBLE);
            chatProgressBar.setVisibility(View.INVISIBLE);

        }

    }

    public void loadImageInto(String image, CircleImageView circleImageView){

                if(!checkNetwork())
                    return;
        //Picasso.with(context).load(image).placeholder(R.drawable.default_user).into(circleImageView);

        Glide.with(getApplicationContext())
                .load(image)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.default_user))
                .into(circleImageView);

    }


    @Override
    public void loadActivity(String image) {

        Log.i("InfoApp", "Interface called");

        Intent fullScreenIntent = new Intent(getApplicationContext(),FullScreenImageActivity.class);
        fullScreenIntent.putExtra("imageUri",image);
        startActivity(fullScreenIntent);

    }

    @Override
    public void loadGalleryImage(File file) {

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        sendBroadcast(intent);

    }

    @Override
    public  boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("InfoApp","Permission is granted");
                return true;
            } else {

                Log.v("InfoApp","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("InfoApp","Permission is granted");
            return true;
        }
    }

    @Override
    public void loadUserProfile(String userID) {

        Intent viewProfileIntent = new Intent(getApplicationContext(),ViewProfile.class);
        viewProfileIntent.putExtra("userID",userID);
        startActivity(viewProfileIntent);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(), "Start loading...", Toast.LENGTH_LONG).show();
            Log.v("InfoApp","Permission: "+permissions[0]+ "was "+grantResults[0]);
            mAdapter.getImage(getApplicationContext());
            //resume tasks needing this permission
        } else{
            Log.e("InfoApp","Permission:error" );
        }
    }

    private boolean checkIfUserLoggedIn() {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser == null || null == AccessToken.getCurrentAccessToken().getUserId()){
            setToStart();
            return false;
        }
        else return true;

    }

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

    private boolean checkNetwork() {
        try {
            boolean wifiDataAvailable = false;
            boolean mobileDataAvailable = false;
            ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] networkInfo = conManager.getAllNetworkInfo();
            for (NetworkInfo netInfo : networkInfo) {
                if (netInfo.getTypeName().equalsIgnoreCase("WIFI"))
                    if (netInfo.isConnected())
                        wifiDataAvailable = true;
                if (netInfo.getTypeName().equalsIgnoreCase("MOBILE"))
                    if (netInfo.isConnected())
                        mobileDataAvailable = true;
            }
            return wifiDataAvailable || mobileDataAvailable;
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_LONG).show();
            return false;
        }
    }

//    @Override
//    public void onBackPressed() {
//
//    }
}
