package com.example.android.testnearby;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private FusedLocationProviderClient client;
    public double latitude;
    public double longitude;
    GoogleMap mGoogleMap;
    private TextToSpeech obj;

    private final int REQUEST_SPEECH_RECOGNIZER = 3000;
    private final String mQuestion = "Welcome! Tell us where you want to go and we can guide you there!";
    private String mAnswer = "";
    Map<String, String> keys = new HashMap<>();
    String toPass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        requestPermission();
        client = LocationServices.getFusedLocationProviderClient(this);

        obj = new TextToSpeech(this, this);
        speakWelcome();
        // end of text to speech

        new CountDownTimer(6500, 1000) {

            public void onTick(long millisUntilFinished) {
                //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                startSpeechRecognizer();
            }
        }.start();


    }
    private void startSpeechRecognizer() {
        Intent intent = new Intent
                (RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, mQuestion);
        startActivityForResult(intent, REQUEST_SPEECH_RECOGNIZER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SPEECH_RECOGNIZER) {
            if (resultCode == RESULT_OK) {
                List<String> results = data.getStringArrayListExtra
                        (RecognizerIntent.EXTRA_RESULTS);
                mAnswer = results.get(0);
                //Toast.makeText(this, mAnswer, Toast.LENGTH_SHORT).show();
                keys.put("restaurant", "restaurant");
                keys.put("restaurants", "restaurant");
                keys.put("department store", "department_store");
                keys.put("department stores", "department_store");
                keys.put("bank", "bank");
                keys.put("banks", "bank");
                keys.put("gas station", "gas_station");
                keys.put("gas stations", "gas_station");
                keys.put("gym", "gym");
                keys.put("gyms", "gym");
                keys.put("shopping mall", "shopping_mall");
                keys.put("shopping malls", "shopping_mall");
                keys.put("malls", "shopping_mall");
                keys.put("mall", "shopping_mall");
                keys.put("supermarket", "supermarket");
                keys.put("supermarkets", "supermarket");


                mAnswer = mAnswer.toLowerCase();
                if (!(keys.containsKey(mAnswer))) {
                    /*finish();
                    super.onDestroy();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);*/
                    toPass = "gas_station";
                    /*Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    intent.putExtra("latitude", 0);
                    intent.putExtra("longitude", 0);
                    intent.putExtra("type", toPass);
                    startActivity(intent);*/

                } else {
                    toPass = keys.get(mAnswer);
                }
                /*if (mAnswer.toUpperCase().indexOf("AMAZON") > -1)
                    mTextView.setText("\n\nQuestion: " + mQuestion +
                            "\n\nYour answer is '" + mAnswer +
                            "' and it is correct!");
                else
                    mTextView.setText("\n\nQuestion: " + mQuestion +
                            "\n\nYour answer is '" + mAnswer +
                            "' and it is incorrect!");*/

                //Intent intent = new Intent(MainActivity.this, GetLocationActivity.class);
                //startActivity(intent);

                if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        if (location != null) {
                            //TextView textView = findViewById(R.id.location);
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Double l = new Double(location.getLongitude());
                            Double doubl = new Double(latitude);
                            //textView.setText("Latitiude = " + doubl.toString() + " Longitude = " + l.toString());
                            /*StringBuilder sbValue = new StringBuilder(sbMethod());
                            PlacesTask placesTask = new PlacesTask();
                            placesTask.execute(sbValue.toString());*/
                            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                            intent.putExtra("latitude", doubl);
                            intent.putExtra("longitude", l);
                            intent.putExtra("type", toPass);
                            startActivity(intent);
                        }

                    }
                });
            }
        }
    }



    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown!
        if (obj != null) {
            obj.stop();
            obj.shutdown();
        }
        super.onDestroy();
    }
    @Override
    public void onInit(int status) {
        // TODO Auto-generated method stub

        if (status == TextToSpeech.SUCCESS) {

            int result = obj.setLanguage(Locale.US);

            // tts.setPitch(5); // set pitch level

            obj.setSpeechRate(1); // set speech speed rate

            speakWelcome();

        } else {
            Log.e("TTS", "Initilization Failed");
        }

    }

    private void speakWelcome() {
        String text = "Welcome to Hands-Free navigation. Please tell us where you want to go";
        obj.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

}