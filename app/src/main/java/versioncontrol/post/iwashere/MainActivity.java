package versioncontrol.post.iwashere;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    //Variables
    String s_lat, s_lon, s_altitude, s_accuracy, s_speed, s_sensor, s_updates, s_address;


    //Constants
    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FASTEST_UPDATE_INTERVAL = 5;
    private static final int PERMISIONS_FINE_LOCATIONS = 99;

    //Reference UI elements

    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address;
    Switch sw_locationsupdate, sw_gps;

    //Variable to remember if we are tracking or not
    Boolean updateOn = false;


    //Location Request is a config file for FusedLocationProviderClient
    LocationRequest locationRequest;

    //Declare LocationCallback
    LocationCallback locationCallback;

    //Google API for location
    FusedLocationProviderClient fusedLocationProviderClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);

        sw_gps = findViewById(R.id.sw_gps);
        sw_locationsupdate = findViewById(R.id.sw_locationsupdates);


        //set all properties to locationRequest

        locationRequest = new LocationRequest();


        //Default
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);

        //Fastest (no saving mode)
        locationRequest.setFastestInterval(1000 * FASTEST_UPDATE_INTERVAL);

        //Update priority power - accuracy
        locationRequest.setPriority(locationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //event that is triggered whenever the update interval is meet.
        locationCallback = new LocationCallback(){


            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                //Save the location
                Location location = locationResult.getLastLocation();
                updateUIValues(location);
            }
        };

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (sw_gps.isChecked()) {
                    //Most accurate
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Using GPS");

                } else {

                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using GSM and WiFi");
                }
            }
        });

        sw_locationsupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_locationsupdate.isChecked()) {
                    //Turn on Location Tracking
                    startLocationUpdates();

                } else {
                    //Turn of location tracking
                    stopLocationUpdates();

                }
            }
        });


        updateGPS();

    } //End onCreate Method

    private void startLocationUpdates() {
        tv_updates.setText("Tracking");


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        updateGPS();
    }

    private void stopLocationUpdates() {
        tv_updates.setText("Not tracking");
        tv_lat.setText("Not tracking");
        tv_lon.setText("Not tracking");
        tv_accuracy.setText("Not tracking");
        tv_speed.setText("Not tracking");
        tv_altitude.setText("Not tracking");
        tv_sensor.setText("Not tracking");
        tv_address.setText("Not tracking");

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){

            case PERMISIONS_FINE_LOCATIONS:
            if (grantResults [0] == PackageManager.PERMISSION_GRANTED){
                updateGPS();

            } else {
                Toast.makeText(this, "This app requires GPS premission", Toast.LENGTH_SHORT);
                finish();
            }
            break;
        }
    }


    private void updateGPS (){

        //get permisions
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            //User provieded permisions
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //We got permisions. Put the values of location into UI
                    updateUIValues(location);

                }
            });



        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISIONS_FINE_LOCATIONS);

            }
            //permisions not granted yet


        }

    }

    private void updateUIValues(Location location) {

        //update all TextViews

        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        //fer un if a location.hasAccuracy()
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        if(location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAccuracy()));

        } else {
            tv_altitude.setText("Not available");
        }

        if(location.hasSpeed()){
            tv_speed.setText(String.valueOf(location.getAccuracy()));

        } else {
            tv_speed.setText("Not available");
        }


        Geocoder geocoder = new Geocoder(this);

        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_address.setText(addresses.get(0).getAddressLine(0));
            //explorar més paràmetes ex, pais, codi postal...

        } catch (Exception e){
            tv_address.setText("Not available");


        }


    }


}