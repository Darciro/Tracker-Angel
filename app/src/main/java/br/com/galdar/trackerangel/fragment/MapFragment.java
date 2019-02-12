package br.com.galdar.trackerangel.fragment;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import br.com.galdar.trackerangel.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment {

    private MapView mapView;
    private GoogleMap map;

    private Double mapLatitude;
    private Double mapLongitude;

    // private final Marker addMarker;


    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        setLocationUpdates();
        getLocationUpdates();

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(-15.952059, -48.2530198), 10);
                map.animateCamera(cameraUpdate);
            }
        });

        return view;
    }

    @Override public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
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
        // Log.d("XXX showLog", "mapLatitude: " + mapLatitude + ", mapLongitude: " + mapLongitude );

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                /*
                LatLng sydney = new LatLng(-33.852, 151.211);
                googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
                map.clear();
                LatLng l = new LatLng(mapLatitude, mapLongitude);
                map.addMarker( new MarkerOptions().position(l).title("Guga está aqui") );
                // CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom( l, 10);
                /// map.animateCamera(cameraUpdate);
            }
        });
    }

    //Initiate the request to track the device's location//
    private void setLocationUpdates() {
        LocationRequest request = new LocationRequest();
        //Specify how often your app should request the device’s location//
        request.setInterval(10000);
        //Get the most accurate location data available//
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient( getActivity().getApplicationContext() );
        final String path = getString(R.string.firebase_path);
        int permission = ContextCompat.checkSelfPermission( getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);

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
                    if (location != null) {
                        //Save the location data to the database//
                        ref.setValue(location);
                        // Log.d( "xxx", "Setting location: " + location );
                        // ref.child("data").setValue(location);
                    }
                }
            }, null);
        } else {
            // Log.d( "xxx", "requestLocationUpdates error!!" );
        }
    }

}
