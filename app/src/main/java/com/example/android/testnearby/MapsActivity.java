package com.example.android.testnearby;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public GoogleMap mGoogleMap;
    double lat;
    double lng;
    double clat, clng;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // build the URL to make the API request
        makeURL();
        Intent i1 = getIntent();
        lat = i1.getDoubleExtra("latitude", 0);
        lng = i1.getDoubleExtra("longitude", 0);
        type = i1.getStringExtra("type");

        /*StringBuilder sbValue = new StringBuilder(sbMethod());
        PlacesTask placesTask = new PlacesTask();
        placesTask.execute(sbValue.toString());*/
    }

    public StringBuilder makeURL() {

        //use your current location here
        double mLatitude = lat;
        double mLongitude = lng;

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + mLatitude + "," + mLongitude);
        sb.append("&radius=50000");
        sb.append("&types=" + type);
        sb.append("&key=");
        sb.append("AIzaSyDrGUQuAkAJ4PInwLn6TEv42BI2kEc_ph4");

        //Log.d("Map", "api: " + sb.toString());

        return sb;
    }

    private class Place extends AsyncTask<String, Integer, String> {

        String data = null;

        @Override
        protected String doInBackground(String... url) {
            try {
                data = getURL(url[0]);
            } catch (Exception e) {
                //Log.d("Background Task", e.toString());
                e.printStackTrace();
            }
            return data;
        }


        @Override
        protected void onPostExecute(String result) {
            MapsActivity.Parser parserTask = new MapsActivity.Parser();

            // Begin parsing places JSON
            // Invokes the "doInBackground()" method of the class Place
            parserTask.execute(result);
        }
    }


    private String getURL(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // HTTP Connection
            urlConnection = (HttpURLConnection) url.openConnection();

            // URL connection
            urlConnection.connect();

            // Reading data
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    public class Place_JSON_List {

        // getting a JSON object and returning a list
        public List<HashMap<String, String>> parse(JSONObject jObject) {

            JSONArray jPlaces = null;
            try {
                /** get all the elements in the 'places' array */
                jPlaces = jObject.getJSONArray("results");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            /** Invoking getPlaces with the json object array
             */
            return getPlaces(jPlaces);
        }

        private List<HashMap<String, String>> getPlaces(JSONArray jPlaces) {
            int placesCount = jPlaces.length();
            List<HashMap<String, String>> placesList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> place = null;

            /** adding places to list */
            for (int i = 0; i < placesCount; i++) {
                try {
                    /** Call getPlace with place JSON object to parse the place */
                    place = getPlace((JSONObject) jPlaces.get(i));
                    placesList.add(place);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return placesList;
        }

        /**
         * JSON parsing
         */
        private HashMap<String, String> getPlace(JSONObject jPlace) {

            HashMap<String, String> place = new HashMap<String, String>();
            String placeName = "-NA-";
            String vicinity = "-NA-";
            String latitude = "";
            String longitude = "";
            String reference = "";

            try {
                if (!jPlace.isNull("name")) {
                    placeName = jPlace.getString("name");
                }

                if (!jPlace.isNull("vicinity")) {
                    vicinity = jPlace.getString("vicinity");
                }

                latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
                longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");
                reference = jPlace.getString("reference");

                place.put("place_name", placeName);
                place.put("vicinity", vicinity);
                place.put("lat", latitude);
                place.put("lng", longitude);
                place.put("reference", reference);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return place;
        }
    }

    public class Parser extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            MapsActivity.Place_JSON_List placeJson = new MapsActivity.Place_JSON_List();

            try {
                jObject = new JSONObject(jsonData[0]);

                places = placeJson.parse(jObject);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return places;
        }

        List<Integer> distanceArray = new ArrayList<Integer>();
        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {

            Log.d("Map", "list size: " + list.size());
            // Clears all the existing markers;
            mGoogleMap.clear();
            Toast.makeText(MapsActivity.this, "hellooooo", Toast.LENGTH_LONG).show();

            double closestDistance;

            // Getting a place from the places list
            HashMap<String, String> h = list.get(0);


            // Getting latitude of the place
            double lati = Double.parseDouble(h.get("lat"));

            // Getting longitude of the place
            double longi = Double.parseDouble(h.get("lng"));

            LatLng placeNow = new LatLng(lati, longi);
            LatLng currentPlace = new LatLng(lat, lng);

            double x = CalculationByDistance(placeNow, currentPlace);
            closestDistance = x;
            int closestIndex = 0;

            for (int i = 0; i < list.size(); i++) {

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);


                // Getting latitude of the place
                double lat1 = Double.parseDouble(hmPlace.get("lat"));

                // Getting longitude of the place
                double lng1 = Double.parseDouble(hmPlace.get("lng"));

                LatLng place = new LatLng(lat1, lng1);
                double distance = CalculationByDistance(place, currentPlace);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestIndex = i;
                }
                //Log.d(f.toString(), "distance");
                //Toast.makeText(MapsActivity.this, f.toString(), Toast.LENGTH_LONG).show();

                // Getting name
                String name = hmPlace.get("place_name");

                Log.d("Map", "place: " + name);

                // Getting vicinity
                String vicinity = hmPlace.get("vicinity");

                LatLng latLng = new LatLng(lat1, lng1);

                // Setting the position for the marker
                markerOptions.position(latLng);

                markerOptions.title(name + " : " + vicinity);

                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

                // Placing a marker on the touched position
                Marker m = mGoogleMap.addMarker(markerOptions);


            }

            MarkerOptions m1 = new MarkerOptions();
            // Getting a place from the places list
            HashMap<String, String> m2 = list.get(closestIndex);
            // Getting latitude of the place
            double l1 = Double.parseDouble(m2.get("lat"));
            // Getting longitude of the place
            double l2 = Double.parseDouble(m2.get("lng"));
            String name = m2.get("place_name");
            Log.d("Closest place is = ", name);
            Log.d("Map", "place: " + name);
            // Getting vicinity
            String vicinity = m2.get("vicinity");
            clat = l1;
            clng = l2;
            LatLng latilongi = new LatLng(l1, l2);
            // Setting the position for the marker
            m1.position(latilongi);
            m1.title("closest" + " : " + vicinity);
            m1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            // Placing a marker on the touched position
            Marker m = mGoogleMap.addMarker(m1);

            mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lng))
                    .title("Current location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

            Uri gmmIntentUri = Uri.parse("google.navigation:q="+clat+","+clng);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);

        }

        // calculating distances and finding the closest place

        public double CalculationByDistance(LatLng StartP, LatLng EndP) {
            int Radius = 6371;// radius of earth in Km
            double lat1 = StartP.latitude;
            double lat2 = EndP.latitude;
            double lon1 = StartP.longitude;
            double lon2 = EndP.longitude;
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(lat1))
                    * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                    * Math.sin(dLon / 2);
            double c = 2 * Math.asin(Math.sqrt(a));
            double valueResult = Radius * c;
            double km = valueResult / 1;
            //DecimalFormat newFormat = new DecimalFormat("####");
            //int kmInDec = Integer.valueOf(newFormat.format(km));
            double meter = valueResult % 1000;
            //int meterInDec = Integer.valueOf(newFormat.format(meter));
            //Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
            //      + " Meter   " + meterInDec);

            return Radius * c;
        }

        public class Place_JSON {

            /**
             * Receives a JSONObject and returns a list
             */
            public List<HashMap<String, String>> parse(JSONObject jObject) {

                JSONArray jPlaces = null;
                try {
                    /** Retrieves all the elements in the 'places' array */
                    jPlaces = jObject.getJSONArray("results");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /** Invoking getPlaces with the array of json object
                 * where each json object represent a place
                 */
                return getPlaces(jPlaces);
            }

            private List<HashMap<String, String>> getPlaces(JSONArray jPlaces) {
                if (jPlaces == null) {
                    Toast.makeText(MapsActivity.this, "No places found", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(MapsActivity.this, MainActivity.class);
                }
                int placesCount = jPlaces.length();
                List<HashMap<String, String>> placesList = new ArrayList<HashMap<String, String>>();
                HashMap<String, String> place = null;

                /** Taking each place, parses and adds to list object */
                for (int i = 0; i < placesCount; i++) {
                    try {
                        /** Call getPlace with place JSON object to parse the place */
                        place = getPlace((JSONObject) jPlaces.get(i));
                        placesList.add(place);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return placesList;
            }

            /**
             * Parsing the Place JSON object
             */
            private HashMap<String, String> getPlace(JSONObject jPlace) {

                HashMap<String, String> place = new HashMap<String, String>();
                String placeName = "-NA-";
                String vicinity = "-NA-";
                String latitude = "";
                String longitude = "";
                String reference = "";

                try {
                    // Extracting Place name, if available
                    if (!jPlace.isNull("name")) {
                        placeName = jPlace.getString("name");
                    }

                    // Extracting Place Vicinity, if available
                    if (!jPlace.isNull("vicinity")) {
                        vicinity = jPlace.getString("vicinity");
                    }

                    latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
                    longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");
                    reference = jPlace.getString("reference");

                    place.put("place_name", placeName);
                    place.put("vicinity", vicinity);
                    place.put("lat", latitude);
                    place.put("lng", longitude);
                    place.put("reference", reference);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return place;
            }
        }
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        map.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title("Current location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        mGoogleMap = map;
        StringBuilder sbValue = new StringBuilder(makeURL());
        Place placesTask = new Place();
        placesTask.execute(sbValue.toString());
        CameraUpdate center=CameraUpdateFactory.newLatLng(new LatLng(lat, lng));
        CameraUpdate zoom= CameraUpdateFactory.zoomTo(13);
        mGoogleMap.moveCamera(center);
        mGoogleMap.animateCamera(zoom);

    }
}