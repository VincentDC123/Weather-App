package com.example.webservices;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
    private String Key = BuildConfig.OpenWeatherMap_API_KEY;
    String json;

    EditText CityText, CountryText;
    TextView temp, description;
    ImageView picture;

    private boolean F = true;
    private double temperature = 0;

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

        // Hiding keyboard
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.inputLayout);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


        Button button = findViewById(R.id.checkTemp);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());

                imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);

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
                                temperature = parser.getTemperatureK();
                                if(F) {
                                    temp.setText(String.valueOf(parser.getTemperatureF()) + DEGREE + "F");
                                } else {
                                    temp.setText(String.valueOf(parser.getTemperatureC()) + DEGREE + "C");
                                }
                            }
                        });
                    }
                });

            }
        });

        CityText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    // Hiding keyboard
                    imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
                    CityText.performClick();
                }
                return false;
            }

        });
        CountryText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    // Hiding keyboard
                    imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
                    CityText.performClick();
                }
                return false;
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
                                        temperature = parser.getTemperatureK();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.main_activity:
                Log.w("MainActivity", "Already in Main Activity");
//                Intent mainIntent = new Intent(this, MainActivity.class);
//                this.startActivity(mainIntent);
                return true;
            case R.id.forecast_activity:
                Log.w("MainActivity", "Going to Forecast Activity");
                return true;
            case R.id.FC:
                final double ZERO_K = -273.15;
                temp = findViewById(R.id.label_temp);
                if(F) {
                    item.setTitle("C");
                    double tempTemperature = Math.floor(temperature + ZERO_K + 0.5);
                    temp.setText(String.valueOf((int)tempTemperature) + DEGREE + "C");
                    F = false;
                } else {
                    item.setTitle("F");
                    double tempTemperature = Math.floor((temperature + ZERO_K) * 9 / 5 + 32 + 0.5);
                    temp.setText(String.valueOf((int)tempTemperature) + DEGREE + "F");
                    F = true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}