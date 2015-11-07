package com.exgress.exgress;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandPendingResult;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;
import android.widget.TextView;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.BandUVEvent;
import com.microsoft.band.sensors.BandUVEventListener;
import com.microsoft.band.sensors.SampleRate;

public class Profile extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener  {

    BandInfo[] pairedBands;
    BandClient bandClient;
    TextView BandVersion;
    TextView BandHR;
    TextView BandTemp;
    TextView BandFVersion;
    TextView BandAccel;
    TextView BandGyro;
    TextView BandUV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        pairedBands = BandClientManager.getInstance().getPairedBands();
        bandClient = BandClientManager.getInstance().create(getApplicationContext(), pairedBands[0]);
        BandVersion = (TextView) findViewById(R.id.BandVer);
        BandHR = (TextView) findViewById(R.id.BandHR);
        BandTemp = (TextView) findViewById(R.id.BandTemp);
        BandFVersion = (TextView) findViewById(R.id.BandFVer);
        BandAccel = (TextView) findViewById(R.id.BandAccel);
        BandGyro = (TextView) findViewById(R.id.BandGyro);
        BandUV = (TextView) findViewById(R.id.BandUV);
        // Note: the BandClient.Connect method must be called from a background thread. An exception
        // will be thrown if called from the UI thread.
        bandConsent(); // new SDK requires consent to read from HR sensor
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
            bandClient.getSensorManager().requestHeartRateConsent(this, mHeartRateConsentListener);
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
                    BandVersion.post(new Runnable() {
                        @Override
                        public void run() { BandVersion.setText(hwVersion);
                        }
                    });
                    BandFVersion.post(new Runnable() {
                        @Override
                        public void run() { BandFVersion.setText(fwVersion);
                        }
                    });
                } catch (InterruptedException ex) {
                    // catch
                } catch(BandException ex) {
                    // catch
                }

                BandHeartRateEventListener heartRateListener = new BandHeartRateEventListener() {
                    public void onBandHeartRateChanged(BandHeartRateEvent bandHeartRateEvent) {
                        final String HR = String.valueOf(bandHeartRateEvent.getHeartRate());
                        BandHR.post(new Runnable() {
                            @Override
                            public void run() {
                                BandHR.setText(HR);
                            }
                        });
                    }
                };


                BandSkinTemperatureEventListener skinTemperatureEventListener = new BandSkinTemperatureEventListener() {
                    @Override
                    public void onBandSkinTemperatureChanged(BandSkinTemperatureEvent bandSkinTemperatureEvent) {
                        final String TempF = String.valueOf(bandSkinTemperatureEvent.getTemperature());
                        BandTemp.post(new Runnable() {
                            @Override
                            public void run() {
                                BandTemp.setText(TempF);
                            }
                        });
                    }
                };

                BandAccelerometerEventListener accelerometerEventListener = new BandAccelerometerEventListener() {
                    @Override
                    public void onBandAccelerometerChanged(BandAccelerometerEvent bandAccelerometerEvent) {
                        final String AccX = String.format("%.2f", bandAccelerometerEvent.getAccelerationX());
                        final String AccY = String.format("%.2f", bandAccelerometerEvent.getAccelerationY());
                        final String AccZ = String.format("%.2f", bandAccelerometerEvent.getAccelerationZ());
                        BandAccel.post(new Runnable() {
                            @Override
                            public void run() {
                                BandAccel.setText("X " + AccX + ", Y " + AccY + ", Z " + AccZ);
                            }
                        });
                    }
                };

                BandGyroscopeEventListener gyroscopeEventListener = new BandGyroscopeEventListener() {
                    @Override
                    public void onBandGyroscopeChanged(BandGyroscopeEvent bandGyroscopeEvent) {
                        final String GyrX = String.format("%.2f", bandGyroscopeEvent.getAngularVelocityX());
                        final String GyrY = String.format("%.2f", bandGyroscopeEvent.getAngularVelocityY());
                        final String GyrZ = String.format("%.2f", bandGyroscopeEvent.getAngularVelocityZ());
                        BandGyro.post(new Runnable() {
                            @Override
                            public void run() {
                                BandGyro.setText("X " + GyrX + ", Y " + GyrY + ", Z " + GyrZ);
                            }
                        });
                    }
                };

                BandUVEventListener uvEventListener = new BandUVEventListener() {
                    @Override
                    public void onBandUVChanged(BandUVEvent bandUVEvent) {
                        final String UVRead = String.valueOf(bandUVEvent.getUVIndexLevel());
                        BandUV.post(new Runnable() {
                            @Override
                            public void run() {
                                BandUV.setText(UVRead);
                            }
                        });
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
                BandVersion.setText("Connection failed.. ");
            }
        }
        catch(InterruptedException ex) {

        }
        catch(BandException ex) {

        }
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
