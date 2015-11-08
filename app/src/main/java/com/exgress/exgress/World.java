package com.exgress.exgress;

import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Fragment;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
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

public class World extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapView mapView;
    private GoogleApiClient mGoogleApiClient;
    private MapInfoFragment mapInfoFragment
    private Map<String, NodeModel> cachedLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.world);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapInfoFragment = (MapInfoFragment) getFragmentManager().findFragmentById(R.id.ExgressMapInfoFragment);
        mapFragment.getMapAsync(this);

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
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new FetchNearByNodes().execute();
            }
        }, 1000, 20000);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                String key = latLng.latitude + ":" + latLng.longitude;
                mapInfoFragment.updateSelectedLocation(cachedLocations.get(key));
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

    protected class FetchNearByNodes extends AsyncTask<Void, Void, List<NodeModel>> {

        @Override
        protected List<NodeModel> doInBackground(Void... params) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            try {
                URL url = new URL("http://exgress.azurewebsites.net/api/Node?latitude=" + location.getLatitude()
                        + "&longitude" + location.getLongitude());
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
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<NodeModel> nodeModels) {
            for (NodeModel nodeModel : nodeModels) {
                LatLng location = new LatLng(nodeModel.latitude, nodeModel.longitude);
                mMap.addMarker(new MarkerOptions().position(location).title(nodeModel.name));
                String key = nodeModel.latitude + ":" + nodeModel.longitude;
                cachedLocations.put(key, nodeModel);
            }
        }
    }
}

