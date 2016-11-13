package com.example.mitchlin.locationapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings.Secure;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;


public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener {
    private TextView myOutput;
    private WebView myWebView;
//    private TextView myReturn;
    private GoogleApiClient mGoogleApiClient;
    private Handler uiHandler;
    Context context;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String urlBase = "http://geo-linker.azurewebsites.net/";
    private static String id;
//    Retrofit retrofit = new Retrofit.Builder()
//            .baseUrl("https://api.github.com/")
//            .build();
//
//    GitHubService service = retrofit.create(GitHubService.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        uiHandler = new Handler();
        context = this;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        myOutput = (TextView)findViewById(R.id.myoutput);
        myOutput.setBackgroundResource(android.R.color.holo_green_light);
//        myReturn = (TextView)findViewById(R.id.myreturn);

        myWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.loadUrl("http://www.facebook.com/groups/1717731545171536/?ref=br_tf");

        id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("ActivityMain", "Button clicked");

                int permissionCheck = ContextCompat.checkSelfPermission(context,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    LocationRequest mLocationRequest = LocationRequest.create()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10_000L)
                            .setFastestInterval(10_000L);

                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.d("MainActivity", location.toString());
                            myOutput.setText("Lat = " + String.valueOf(location.getLatitude()) +
                                    "   Long = " + String.valueOf(location.getLongitude()));
                            final String fullUrl = urlBase /*+ "id" + "/"*/ + String.valueOf(location.getLatitude())
                                    + "/" + String.valueOf(location.getLongitude());
                            Thread t = new Thread(new Runnable()
                            {
                                @Override
                                public void run() {
                                    String text = callAPI(fullUrl);
//                                    guiSetText(myReturn, text);
                                    if(text != null && text.charAt(0) != '{') {
                                        webSetText(myWebView, text);
                                    }
                                }
                            });
                            t.start();

                        }
                    });
                } else {
                    Log.d("ActivityMain", "Permission Denied");
                }

            }
        });

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


    }

    protected void onStart() {
        super.onStart();
        // Connect the client.
        mGoogleApiClient.connect();

    }
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("MainActivity", "onConnected is called");
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);


            if (mLastLocation != null) {
                Log.d("MainActivity", "onConnected: " + mLastLocation.toString());
                myOutput.setText("Lat = " + String.valueOf(mLastLocation.getLatitude()) +
                        "   Long = " + String.valueOf(mLastLocation.getLongitude()));

                //            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                final String fullUrl = urlBase /*+ "id" + "/"*/ + String.valueOf(mLastLocation.getLatitude())
                        + "/" + String.valueOf(mLastLocation.getLongitude());

                Thread t = new Thread(new Runnable()
                {
                    @Override
                    public void run() {
                        String text = callAPI(fullUrl);
                        if(text != null && text.charAt(0) != '{') {
                            webSetText(myWebView, text);
                        }
//                        guiSetText(myReturn, text);
//
                    }

                });
                t.start();
//                uiHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        // This is run on the UI thread.
//                        String retString = callAPI(fullUrl);
//                        myReturn.setText(retString);
//                    }
//                });


            }
        } else {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

    }

    public String callAPI(String urlString) {
        HttpURLConnection httpConnection = null;
        URL url;
        URLConnection connection;
        try {
            url = new URL(urlString);
            connection = url.openConnection();

            httpConnection = (HttpURLConnection)connection;
            Log.d("httpConnection", httpConnection.toString());
            int responseCode = httpConnection.getResponseCode();
//                        boolean good = false;
            Log.d("ActivityMain", "responseCode = " + responseCode);

            InputStream in = new BufferedInputStream(httpConnection.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            Log.d("Data", sb.toString());
            return sb.toString();
//            JSONObject data = new JSONObject((String) httpConnection.get());
//            int lat = data.getInt("lat");
//            Log.d("Data", "Lat = " + lat);

//            Scanner test = new Scanner((FileInputStream) httpConnection.getContent());
//            while (test.hasNext()) {
//                Log.d("Content", test.next());
//            }
//                        if (responseCode == HttpURLConnection.HTTP_OK)
//                            good = true;
//
//                        Log.d("Mine", "Good HttpURLConnection");
        }
        catch (MalformedURLException e) {
            Log.d("Mine", "MalformedURLException "+e);
        } catch (IOException e) {
            Log.d("Mine", "IOException "+e);
//        } catch (JSONException e) {
//            e.printStackTrace();
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
        return null;
    }
//    private void guiSetText(final TextView view, final String text) {
//
//        Runnable work = new Runnable(){
//            public void run() {
//                myReturn.setText(text);
//            }
//        };
//        uiHandler.post(work);
//    }
    private void webSetText(final WebView view, final String text) {

        Runnable work = new Runnable(){
            public void run() {
                view.loadUrl(text);
            }
        };
        uiHandler.post(work);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d("MainActivity", "Request Result" + requestCode);
        HttpURLConnection httpConnection = null;
        URL url;
        URLConnection connection;
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);
                    Log.d("MainActivity", "onConnected: " + mLastLocation.toString());

                    if (mLastLocation != null) {
                        myOutput.setText(String.valueOf(mLastLocation.getLatitude()));
                        //            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
                        // permission was granted, yay! Do the
                        // contacts-related task you need to do.
                    }
                    try {

                        url = new URL(urlBase /*+ "id" + "/"*/ + String.valueOf(mLastLocation.getLatitude())
                                            + "/" + String.valueOf(mLastLocation.getLongitude()));
                        connection = url.openConnection();

                        httpConnection = (HttpURLConnection)connection;
                        int responseCode = httpConnection.getResponseCode();
//                        boolean good = false;
                        Log.d("ActivityMain", "responseCode = " + responseCode);
//                        if (responseCode == HttpURLConnection.HTTP_OK)
//                            good = true;
//
//                        Log.d("Mine", "Good HttpURLConnection");
                    }
                    catch (MalformedURLException e) {
                        Log.d("Mine", "MalformedURLException "+e);
                    } catch (IOException e) {
                        Log.d("Mine", "IOException "+e);
                    }
                    finally {
                        if (httpConnection != null) {
                            httpConnection.disconnect();
                        }
                    }
                }
                else {
                    Log.d("MainActivity", "Permission Denied");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}