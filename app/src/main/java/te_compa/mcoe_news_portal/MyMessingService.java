package te_compa.mcoe_news_portal;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import android.util.Log;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyMessingService extends FirebaseMessagingService {

    private  static  final String CHANNEL_id="mcoe_today";
    private  static  final String CHANNEL_NAME="mcoe_today";
    private  static  final String CHANNEL_DISC="mcoe_today";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("messagerec", "From: " + remoteMessage.getFrom());
        if (remoteMessage.getData().size() > 0) {
            Log.d("messagerec", "Message data payload: " + remoteMessage.getData());
        }
        else
            Log.d("messagerec", "No data in payload: " );

        String title=remoteMessage.getNotification().getTitle();
        String body=remoteMessage.getNotification().getBody();
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {

            Log.d("messagerec", "Message Notification Title: " + title);
            Log.d("messagerec", "Message Notification Body: " + body);

        }

        sendNotification(title,body);
    }



        private void sendNotification(String title, String body) {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
            {
                NotificationChannel channel=new NotificationChannel(CHANNEL_id,CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription(CHANNEL_DISC);
                NotificationManager manager=getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            NotificationCompat.Builder mBuilder=new NotificationCompat.Builder(this,CHANNEL_id)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    ;
            NotificationManagerCompat notificationManager=NotificationManagerCompat.from(this);
            notificationManager.notify(1,mBuilder.build());


        }
    }



