package br.com.galdar.trackerangel.fragment;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import br.com.galdar.trackerangel.R;
import br.com.galdar.trackerangel.activity.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private final static String APP_PACKAGE = "br.com.galdar.trackerangel";
    private final static String APP_CHANEL_ID = APP_PACKAGE + ".APP_CHANNEL";

    private Button trackmeButton;

    private NotificationCompat.Builder notBuilder;
    private static final int MY_NOTIFICATION_ID = 12345;
    private static final int MY_REQUEST_CODE = 100;
    private Boolean notificationActive;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        createNotificationChannel();
        this.notBuilder = new NotificationCompat.Builder( getActivity(), APP_CHANEL_ID);
        // The message will automatically be canceled when the user clicks on Panel
        this.notBuilder.setAutoCancel(true);

        NotificationManager notificationService = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        if( notificationService.getActiveNotifications().toString() != "" ) {
            notificationActive = true;
            // Log.d("XXX notificationService active: ", notificationService.getActiveNotifications().toString() );
        }

        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        // NotificationChannel notificationChannel = notificationManager.getNotificationChannel(APP_CHANEL_ID);
        // NotificationChannel[] notificationChannel = notificationManager.getActiveNotifications();

        for( StatusBarNotification not : notificationManager.getActiveNotifications() ){
            Log.d("XXX", "notificationChannels::: " + not.getId() );
        }

        if( isNotificationVisible() ){
            Log.d("XXX", "ATIVA" );
        } else {
            Log.d("XXX", "NÂO ESTA ATIVA" );
        }

        trackmeButton = view.findViewById(R.id.trackmeButton);
        trackmeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification(v);
            }
        });

        return view;
    }

    private boolean isNotificationVisible() {
        Intent notificationIntent = new Intent( getActivity(), MainActivity.class);
        PendingIntent test = PendingIntent.getActivity( getActivity(), MY_NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_NO_CREATE);
        return test != null;
    }

    public void sendNotification(View view) {

        String stop = "stop";
        getActivity().registerReceiver(stopReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);

        this.notBuilder.setSmallIcon(R.drawable.ic_car_not);
        this.notBuilder.setTicker("This is a ticker");
        // Set the time that the event occurred.
        // Notifications in the panel are sorted by this time.
        this.notBuilder.setWhen(System.currentTimeMillis()+ 10* 1000);
        this.notBuilder.setContentTitle(getString(R.string.app_name));
        this.notBuilder.setContentText(getString(R.string.tracking_enabled_notif));

        // Create Intent
        Intent intent = new Intent(getActivity(), MainActivity.class);

        // PendingIntent.getActivity(..) will start an Activity, and returns PendingIntent object.
        // It is equivalent to calling Context.startActivity(Intent).
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), MY_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        this.notBuilder.setContentIntent(pendingIntent);

        // Get a notification service (A service available on the system).
        NotificationManager notificationService = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        // Builds notification and issue it
        Notification notification =  notBuilder.build();
        notificationService.notify(MY_NOTIFICATION_ID, notification);

    }

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Unregister the BroadcastReceiver when the notification is tapped//
            getActivity().unregisterReceiver(stopReceiver);
            //Stop the Service//
            // getApplicationContext().stopSelf();
        }
    };

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("XXX", "Create channel");
            // CharSequence name = getString(R.string.channel_name);
            CharSequence name = "tracker-angel-app-channel";
            // String description = getString(R.string.channel_description);
            String description = "app-description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(APP_CHANEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        } else {
            Log.d("XXX", "Dont need to create channel");
        }
    }

}
