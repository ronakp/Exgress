package com.exgress.exgress;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandPendingResult;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.BandUVEvent;
import com.microsoft.band.sensors.BandUVEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;
import com.microsoft.band.sensors.SampleRate;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MapInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapInfoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private Button actionButton;

    private TextView name, location, faction, hp, heartBeat;
    private ImageView factionIcon, heartBeatIcon;

    BandInfo[] pairedBands;
    BandClient bandClient;

    private boolean sampleHeartRate = true;
    private Timer heartTimer;
    private Timer submitTimer;
    private int collectedHp = 0;
    private boolean activityStop = false;
    private NodeModel node;
    private String userFaction;


    public MapInfoFragment() {
        // Required empty public constructor
    }

    public void setUserFaction(String faction) {
        this.userFaction = faction;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pairedBands = BandClientManager.getInstance().getPairedBands();
        bandClient = BandClientManager.getInstance().create(getActivity(), pairedBands[0]);

        //set up the heartTimer
        heartTimer = new Timer();
        submitTimer = new Timer();

//        new FetchNodeInfoActivity().execute(nodeName); //might have to be moved somewhere else
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map_info, container, false);

        actionButton = (Button)view.findViewById(R.id.ActionButton);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activityStop) {
                    actionButton.setText("Click To Finish");
                    bandConsent();
                    heartTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            sampleHeartRate = true;
                        }
                    }, 1000, 1000);
                    new ActionActivities().execute(node);
                    activityStop = false;
                } else {
                    actionButton.setText("Click To Energize");
                    activityStop = true;
                }
            }
        });

        name = (TextView)view.findViewById(R.id.SpotName);
        location = (TextView)view.findViewById(R.id.SpotCoordinates);
        faction = (TextView)view.findViewById(R.id.SpotFaction);
        hp = (TextView)view.findViewById(R.id.SpotHealth);
        heartBeat = (TextView)view.findViewById(R.id.HeartBeatText);
        heartBeatIcon = (ImageView)view.findViewById(R.id.HeartImage);
        factionIcon = (ImageView)view.findViewById(R.id.SpotImage);

        //might as well set the faction icon to the user's image

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void updateSelectedLocation(NodeModel model) {
        activityStop = true;
        node = model;
        name.setText(model.name);
        name.setVisibility(View.VISIBLE);
        location.setText(model.latitude + " : " + model.longitude);
        location.setVisibility(View.VISIBLE);
        faction.setText(model.faction);
        faction.setVisibility(View.VISIBLE);
        hp.setText(model.hp);
        hp.setVisibility(View.VISIBLE);
        if (model.faction.equals(Constants.BlueFaction)){
            factionIcon.setBackgroundResource(R.drawable.purist);
        } else {
            factionIcon.setBackgroundResource(R.drawable.supremacy);
        }
    }

    private HeartRateConsentListener mHeartRateConsentListener = new HeartRateConsentListener() {
        @Override
        public void userAccepted(boolean b) {
            // handle user's heart rate consent decision
            new Thread(new Runnable() {
                public void run() {
                    try {
                        helloMSBand();
                    } catch (BandException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    };

    public boolean bandConsent() {
        if (bandClient.getSensorManager().getCurrentHeartRateConsent() != UserConsent.GRANTED) {
            bandClient.getSensorManager().requestHeartRateConsent(getActivity(), mHeartRateConsentListener);
            return false;
        } else {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        helloMSBand();
                    } catch (BandException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            return true;
        }
    }

    public void helloMSBand() throws BandException {
        BandPendingResult<ConnectionState> pendingResult = bandClient.connect();

        try {
            ConnectionState result = pendingResult.await();
            if(result == ConnectionState.CONNECTED) {
                try {
                    BandPendingResult<String> pendingVersion = bandClient.getFirmwareVersion();
                    final String fwVersion = pendingVersion.await();
                    pendingVersion = bandClient.getHardwareVersion();
                    final String hwVersion = pendingVersion.await();
                } catch (InterruptedException | BandException ex) {
                    // catch
                }

                BandHeartRateEventListener heartRateListener = new BandHeartRateEventListener() {
                    public void onBandHeartRateChanged(BandHeartRateEvent bandHeartRateEvent) {
                        if (sampleHeartRate && !activityStop) {
                            collectedHp += bandHeartRateEvent.getHeartRate();
                            sampleHeartRate = false;
                        }
                        heartBeat.setText(bandHeartRateEvent.getHeartRate());
                    }
                };


                BandSkinTemperatureEventListener skinTemperatureEventListener = new BandSkinTemperatureEventListener() {
                    @Override
                    public void onBandSkinTemperatureChanged(BandSkinTemperatureEvent bandSkinTemperatureEvent) {
                        final String TempF = String.valueOf(bandSkinTemperatureEvent.getTemperature());
                    }
                };

                BandAccelerometerEventListener accelerometerEventListener = new BandAccelerometerEventListener() {
                    @Override
                    public void onBandAccelerometerChanged(BandAccelerometerEvent bandAccelerometerEvent) {
                        final String AccX = String.format("%.2f", bandAccelerometerEvent.getAccelerationX());
                        final String AccY = String.format("%.2f", bandAccelerometerEvent.getAccelerationY());
                        final String AccZ = String.format("%.2f", bandAccelerometerEvent.getAccelerationZ());
                    }
                };

                BandGyroscopeEventListener gyroscopeEventListener = new BandGyroscopeEventListener() {
                    @Override
                    public void onBandGyroscopeChanged(BandGyroscopeEvent bandGyroscopeEvent) {
                        final String GyrX = String.format("%.2f", bandGyroscopeEvent.getAngularVelocityX());
                        final String GyrY = String.format("%.2f", bandGyroscopeEvent.getAngularVelocityY());
                        final String GyrZ = String.format("%.2f", bandGyroscopeEvent.getAngularVelocityZ());
                    }
                };

                BandUVEventListener uvEventListener = new BandUVEventListener() {
                    @Override
                    public void onBandUVChanged(BandUVEvent bandUVEvent) {
                        final String UVRead = String.valueOf(bandUVEvent.getUVIndexLevel());
                    }
                };

                try {
                    bandClient.getSensorManager().registerUVEventListener(uvEventListener);
                } catch(BandException ex) {
                    //catch
                }

                try {
                    bandClient.getSensorManager().registerAccelerometerEventListener(accelerometerEventListener, SampleRate.MS128);
                } catch(BandException ex) {
                    // catch
                }

                try {
                    bandClient.getSensorManager().registerGyroscopeEventListener(gyroscopeEventListener, SampleRate.MS128);
                } catch(BandException ex) {

                }

                try {
                    bandClient.getSensorManager().registerHeartRateEventListener(heartRateListener);
                } catch(BandException ex) {
                    // catch
                }

                try {
                    bandClient.getSensorManager().registerSkinTemperatureEventListener(skinTemperatureEventListener);
                } catch(BandException ex) {
                }
            } else {
                //BandVersion.setText("Connection failed.. ");
            }
        }
        catch(InterruptedException | BandException ex) {

        }
    }

    private class ActionActivities extends AsyncTask<NodeModel, String, Void> {

        @Override
        protected Void doInBackground(final NodeModel... params) {
            //here, we can connect to the server and send updates... yeah this might be a bit fun
            //we are sending updates once every 10 seconds

            //I am going to hate myself for writing tihs
            submitTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (activityStop) {
                            submitTimer.cancel();
                            submitTimer.purge();
                        }
                        String action = userFaction.equals(params[0].faction) ? Constants.ReinforceSubmission : Constants.DrainSubmission;

                        URL url = new URL("http://exgress.azurewebsites.net/api/Node/Update");
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("POST");
                        urlConnection.setRequestProperty("Content-Type","application/JSON");
                        urlConnection.connect();
                        //Write
                        OutputStream os = urlConnection.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                        writer.write("{\n" +
                                "  \"Name\": \"" + params[0].name + "\",\n" +
                                "  \"Faction\": \"" + params[0].faction + "\",\n" +
                                "  \"Action\": \"" + action + "\"\n" +
                                "  \"HP\": \"" + collectedHp + "\"\n" +
                                "}");
                        writer.close();
                        os.close();
                        //Read
                        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));

                        String line;
                        StringBuilder sb = new StringBuilder();

                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        br.close();
                        String resultr = sb.toString();
                        JSONObject jResultl = new JSONObject(resultr);
                        node.faction = jResultl.getString(Constants.FactionColumn);
                        node.hp = jResultl.getInt(Constants.HPColumn);
                        publishProgress(node.hp + "", node.faction);
                    } catch (Exception e ) {
                        System.out.println(e.getMessage());
                    }
                }
            }, 10000, 10000);


            return null;
        }

        @Override
        protected void onProgressUpdate(String... updateText) {
            collectedHp = 0;
            hp.setText(updateText[0]);
            faction.setText(updateText[1]);
            if (updateText[1].equals(Constants.BlueFaction)) {
                factionIcon.setBackgroundResource(R.drawable.purist);
            } else {
                factionIcon.setBackgroundResource(R.drawable.supremacy);
            }
        }
    }

}
