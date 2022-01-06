package com.example.webservices;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    public static final char DEGREE = '\u00B0';
    public static final String STARTING_URL = "http:/api.openweathermap.org/data/2.5/weather?q=";
    public static final String KEY_NAME = "&appid=";
    private String Key = "**************************";
    String json;

    EditText CityText, CountryText;
    TextView temp, description;
    ImageView picture;

    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CityText = findViewById(R.id.data_city);
        CountryText = findViewById(R.id.data_country);
        temp = findViewById(R.id.label_temp);
        picture = findViewById(R.id.picture);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if(ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        Button button = findViewById(R.id.checkTemp);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());
                executor.execute(new Runnable () {
                    @Override
                    public void run() {
                        // Do background here
                        String baseUrl = STARTING_URL;
                        String cityString = CityText.getText().toString();
                        cityString += ",";
                        cityString += CountryText.getText().toString();
                        String keyName = KEY_NAME;
                        String key = Key;
                        // Create an object RemoteDataReader
                        RemoteDataReader rdr = new RemoteDataReader(baseUrl, cityString, keyName, key);
                        // Get the JSON string
                        json = rdr.getData();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                TemperatureParser parser = new TemperatureParser(json);

                                String iconUrl = "http://openweathermap.org/img/w/" + parser.getIcon() + ".png";
                                Log.w("MainActivity", iconUrl);
                                Picasso.get().load(iconUrl).into(picture);

                                description = findViewById(R.id.data_description);
                                description.setText(parser.getIconDesc());


                                Log.w("MainActivity", String.valueOf(parser.getTemperatureK()) + DEGREE + "K");
                                temp.setText(String.valueOf(parser.getTemperatureF()) + DEGREE + "F");
                            }
                        });
                    }
                });

            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if(location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(),location.getLongitude(), 1);
                        Log.w("MainActivity", "Latitude: " + addresses.get(0).getLatitude());
                        Log.w("MainActivity", "Longtitude: " + addresses.get(0).getLongitude());
                        Log.w("MainActivity", "Country: " + addresses.get(0).getCountryName());
                        Log.w("MainActivity", "Locality: " + addresses.get(0).getLocality());
                        Log.w("MainAcitivity", "Address: " + addresses.get(0).getAddressLine(0));
                        CityText.setText(addresses.get(0).getLocality());
                        CountryText.setText(addresses.get(0).getCountryName());

                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        Handler handler = new Handler(Looper.getMainLooper());
                        executor.execute(new Runnable () {
                            @Override
                            public void run() {
                                // Do background here
                                String baseUrl = STARTING_URL;
                                String cityString = addresses.get(0).getLocality();
                                cityString += ",";
                                cityString += addresses.get(0).getCountryName();
                                String keyName = KEY_NAME;
                                String key = Key;
                                // Create an object RemoteDataReader
                                RemoteDataReader rdr = new RemoteDataReader(baseUrl, cityString, keyName, key);
                                // Get the JSON string
                                json = rdr.getData();

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        TemperatureParser parser = new TemperatureParser(json);

                                        String iconUrl = "http://openweathermap.org/img/w/" + parser.getIcon() + ".png";
                                        Log.w("MainActivity", iconUrl);
                                        Picasso.get().load(iconUrl).into(picture);

                                        description = findViewById(R.id.data_description);
                                        description.setText(parser.getIconDesc());


                                        Log.w("MainActivity", String.valueOf(parser.getTemperatureK()) + DEGREE + "K");
                                        temp.setText(String.valueOf(parser.getTemperatureF()) + DEGREE + "F");
                                    }
                                });
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}