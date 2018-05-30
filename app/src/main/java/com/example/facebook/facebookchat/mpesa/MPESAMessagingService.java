package com.example.facebook.facebookchat.mpesa;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.facebook.facebookchat.ChatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.example.facebook.facebookchat.R;

import org.json.JSONObject;

/**
 * Created by miles on 20/11/2017.
 */

public class MPESAMessagingService extends FirebaseMessagingService {

    //Searches FireBase
    private DatabaseReference userRef;
    private int payments_counter = -1;
    private FirebaseUser currentUser;

    private static final String TAG = "MPESAMessagingService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.i("GLOBAL STEP NTF 4", "YES");

        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
//        Log.d("GLOBAL STEP FB Message", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            handleNow(remoteMessage.getData().toString());

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow(String result) {
        try {
            Log.i("GLOBAL STEP NTF 3", "J="+result);
            JSONObject json = new JSONObject(result);
            JSONObject data = json.getJSONObject("data");
            JSONObject payload = data.getJSONObject("payload");
            String title = data.getString("title");
            String message = data.getString("message");
            int amount = Integer.parseInt(payload.getString("mpesa_amount"));

            Log.i("GLOBAL STEP AMOUNT", "A="+amount);

            int code = data.getInt("code");
            //TODO read more attributes here
            sendNotification(title, message);

            Intent pushNotification = new Intent(MPesaActivity.NOTIFICATION);
            pushNotification.putExtra("message", message);
            pushNotification.putExtra("title", title);
            pushNotification.putExtra("code", code);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            if (code == 0){

                int searchesAmount = (int) Math.floor(amount / MPesaActivity.PRICE_OF_SEARCH);

                addSearches(searchesAmount);//searchesAmount

            }else {

                //TESTTTTTTTTTTTTTTTTTTTTTTTTT

//                int searchesAmount = (int) Math.floor(amount / MPesa_PaymentsActivity.PRICE_OF_SEARCH);
//
//                addSearches(searchesAmount+5);//amount

            }

        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     */
    private void sendNotification(String title, String message) {

        Log.i("GLOBAL STEP NTF 2", "YES");

        Intent intent = new Intent(this, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_ic_notification)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1 /* ID of notification */, notificationBuilder.build());
    }

    private void addSearches(final int searches){

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        userRef.child(currentUser.getPhoneNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {

                    payments_counter = Integer.parseInt(dataSnapshot.child("search_left").getValue().toString());
                    payments_counter += searches;
                    userRef.child(currentUser.getPhoneNumber()).child("search_left").setValue(payments_counter).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //If added do something...

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(getApplicationContext(),"DatabaseError",Toast.LENGTH_LONG).show();

                        }
                    });

                }   catch (NullPointerException e){
                    userRef.child(currentUser.getPhoneNumber()).child("search_left").setValue(0);
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
}
