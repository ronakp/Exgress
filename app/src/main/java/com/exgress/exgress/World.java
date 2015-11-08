package com.exgress.exgress;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Fragment;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class World extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private MapView mapView;
    private GoogleApiClient mGoogleApiClient;
    private MapInfoFragment mapInfoFragment;
    private Map<String, NodeModel> cachedLocations;
    private String userFaction;

    private Timer refreshTimer;
    private boolean attemptingRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.world);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapInfoFragment = (MapInfoFragment) getFragmentManager().findFragmentById(R.id.ExgressMapInfoFragment);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();

        userFaction = intent.getExtras().getString("faction");

        if (userFaction.equals(Constants.BlueFaction)) {
            LinearLayout view = (LinearLayout) findViewById(R.id.world);
            view.setBackgroundResource(R.drawable.purist_background);
            ImageView image = (ImageView) findViewById(R.id.SpotImage);
            image.setBackgroundResource(R.drawable.purist);
        }
        else {
            LinearLayout view = (LinearLayout) findViewById(R.id.world);
            view.setBackgroundResource(R.drawable.supremacy_background);
            ImageView image = (ImageView) findViewById(R.id.SpotImage);
            image.setBackgroundResource(R.drawable.supremacy);
        }

        mapInfoFragment.setUserFaction(userFaction);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        cachedLocations = new HashMap<>();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng location = marker.getPosition();
                String key = (location.latitude + "").substring(0, 7);
                mapInfoFragment.updateSelectedLocation(cachedLocations.get(key));
                return true;
            }
        });
    }

    private boolean initMap(){
        if (mMap== null){
           // SupportMapFragment mapFrag = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.nav_world);
           // mMap = mapFrag.getMap();
        }
        return (mMap!=null);
    }

    @Override
    public void onConnected(Bundle bundle) {
        refreshTimer = new Timer();
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!attemptingRequest) {
                    Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    new FetchNearByNodes().execute(location);
                    attemptingRequest = true;
                }
            }
        }, 1000, 20000);
    }

    @Override
    public void onConnectionSuspended(int i) {
        refreshTimer.cancel();
        refreshTimer.purge();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    protected class FetchNearByNodes extends AsyncTask<Location, Void, List<NodeModel>> {

        @Override
        protected List<NodeModel> doInBackground(Location... params) {

            try {
                URL url;
                if (params[0] == null) {
                    url = new URL("http://exgress.azurewebsites.net/api/Node?latitude=" + 41.319076
                            + "&longitude=" + -72.915259);
                } else {
                    url = new URL("http://exgress.azurewebsites.net/api/Node?latitude=" + params[0].getLatitude()
                            + "&longitude=" + params[0].getLongitude());
                }
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));

                String line;
                StringBuilder sb = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                String resultr = sb.toString();

                JSONArray result = new JSONArray(resultr);
                List<NodeModel> toReturn = new ArrayList<>();
                for (int i = 0; i < result.length(); i++) {
                    JSONObject jsonObject = result.getJSONObject(i);
                    NodeModel model = new NodeModel(
                            jsonObject.getString(Constants.NameColumn),
                            (float)jsonObject.getDouble(Constants.LatitudeColumn),
                            (float)jsonObject.getDouble(Constants.LongitudeColumn),
                            jsonObject.getString(Constants.FactionColumn),
                            jsonObject.getInt(Constants.HPColumn)
                    );
                    toReturn.add(model);
                }
                return toReturn;
            }
            catch (Exception e) {
                return new ArrayList<>();
            }
            finally {
                attemptingRequest = false;
            }
        }

        @Override
        protected void onPostExecute(List<NodeModel> nodeModels) {
            for (NodeModel nodeModel : nodeModels) {
                LatLng location = new LatLng(nodeModel.latitude, nodeModel.longitude);
                if (nodeModel.faction.equals(Constants.BlueFaction)) {
                    mMap.addMarker(new MarkerOptions().position(location).title(nodeModel.name).icon(BitmapDescriptorFactory.fromResource(R.drawable.purist_small)));
                } else {
                    mMap.addMarker(new MarkerOptions().position(location).title(nodeModel.name).icon(BitmapDescriptorFactory.fromResource(R.drawable.supremacy_small)));
                }
                String key = (nodeModel.latitude + "").substring(0, 7);
                cachedLocations.put(key, nodeModel);
            }
        }
    }
}

