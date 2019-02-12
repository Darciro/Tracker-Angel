package br.com.galdar.trackerangel.activity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.os.IBinder;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.Manifest;
import android.location.Location;
import android.app.Notification;
import android.content.pm.PackageManager;
import android.app.PendingIntent;
import android.app.Service;

import br.com.galdar.trackerangel.R;

public class TrackingService extends Service {

    // private static final String TAG = TrackingService.class.getSimpleName();
    private final static String APP_PACKAGE = "br.com.galdar.trackerangel";
    private final static String APP_CHANEL_ID = APP_PACKAGE + ".APP_CHANNEL";
    private static final String TAG = "XXX";

    private static final int MY_NOTIFICATION_ID = 12345;
    private static final int MY_REQUEST_CODE = 100;

    private Double mapLatitude;
    private Double mapLongitude;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // createNotificationChannel();
        // buildNotification();
        loginToFirebase();

        /*NotificationManager notificationService = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if( notificationService.getActiveNotifications().toString() != "" ) {
            Log.d("XXX notificationService active: ", notificationService.getActiveNotifications().toString() );

            setLocationUpdates();
            // getLocationUpdates();

        }*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(stopReceiver);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("XXX", "Create channel");
            // CharSequence name = getString(R.string.channel_name);
            CharSequence name = "application-channel";
            // String description = getString(R.string.channel_description);
            String description = "app-description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(APP_CHANEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        } else {
            Log.d("XXX", "Dont need to create channel");
        }
    }

    //Create the persistent notification//
    private void buildNotification() {

        String stop = "stop";
        registerReceiver(stopReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the persistent notification//
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.tracking_enabled_notif))
                //Make this notification ongoing so it can’t be dismissed by the user//
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setChannelId(APP_CHANEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name);

        startForeground(1, builder.build());
    }

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Unregister the BroadcastReceiver when the notification is tapped//
            unregisterReceiver(stopReceiver);
            //Stop the Service//
            stopSelf();
        }
    };

    private void loginToFirebase() {
        //Authenticate with Firebase, using the email and password we created earlier//
        String email = getString(R.string.test_email);
        String password = getString(R.string.test_password);

        //Call OnCompleteListener if the user is signed in successfully//
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                //If the user has been authenticated...//
                if (task.isSuccessful()) {
                    //...then call requestLocationUpdates//
                    Log.d(TAG, "Firebase authentication successful");
                    // requestLocationUpdates();
                    setLocationUpdates();
                } else {
                    //If sign in fails, then log the error//
                    Log.d(TAG, "Firebase authentication failed");
                }
            }
        });
    }


    //Initiate the request to track the device's location//
    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        //Specify how often your app should request the device’s location//
        request.setInterval(10000);
        //Get the most accurate location data available//
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        final String path = getString(R.string.firebase_path);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);


        // Log.d( "xxx", "Connecting to database... " + path);

        //If the app currently has access to the location permission...//
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Log.d( "xxx", "requestLocationUpdates ok..." );
            //...then request location updates//
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    //Get a reference to the database, so your app can perform read and write operations//
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
                    Location location = locationResult.getLastLocation();
                    // Log.d( "xxx", "location: " + location );
                    if (location != null) {
                        //Save the location data to the database//
                        ref.setValue(location);
                        // ref.child("data").setValue(location);
                    }
                }
            }, null);
        } else {
            // Log.d( "xxx", "requestLocationUpdates error!!" );
        }
    }

    public void getLocationUpdates () {
        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference();
        dbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren() ) {
                    mapLatitude = (Double) data.child("latitude").getValue();
                    mapLongitude = (Double) data.child("longitude").getValue();
                }

                showLog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void showLog() {
        Log.d("XXX showLog", "mapLatitude: " + mapLatitude + ", mapLongitude: " + mapLongitude );

        /*mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                *//*
                LatLng sydney = new LatLng(-33.852, 151.211);
                googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*//*
                map.clear();
                LatLng l = new LatLng(mapLatitude, mapLongitude);
                map.addMarker( new MarkerOptions().position(l).title("Guga está aqui") );
                // CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom( l, 10);
                /// map.animateCamera(cameraUpdate);
            }
        });*/
    }

    //Initiate the request to track the device's location//
    private void setLocationUpdates() {

        // NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        // NotificationChannel notificationChannel = notificationManager.getNotificationChannel(APP_CHANEL_ID);
        // NotificationChannel[] notificationChannel = notificationManager.getActiveNotifications();

        NotificationManager notificationService = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        for( StatusBarNotification not : notificationService.getActiveNotifications() ){
            Log.d("XXX", "notificationChannels::: " + not.getId() );

            if( not.getId() == MY_NOTIFICATION_ID ){
                Log.d("XXX notificationService active: ", notificationService.getActiveNotifications().toString() );

                LocationRequest request = new LocationRequest();
                //Specify how often your app should request the device’s location//
                request.setInterval(10000);
                //Get the most accurate location data available//
                request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient( getApplicationContext() );
                final String path = getString(R.string.firebase_path);
                int permission = ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);

                Log.d( "xxx", "Connecting to database... " + path);

                //If the app currently has access to the location permission...//
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    Log.d( "xxx", "requestLocationUpdates ok..." );
                    //...then request location updates//
                    client.requestLocationUpdates(request, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            //Get a reference to the database, so your app can perform read and write operations//
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
                            Location location = locationResult.getLastLocation();
                            if (location != null) {
                                //Save the location data to the database//
                                ref.setValue(location);
                                Log.d( "xxx", "Setting location: " + location );
                                // ref.child("data").setValue(location);
                            }
                        }
                    }, null);
                } else {
                    Log.d( "xxx", "requestLocationUpdates error!!" );
                }
            }
        }
    }
}