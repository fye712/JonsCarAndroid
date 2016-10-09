package com.example.franklinye.jonsandroidcar;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.openxc.VehicleManager;
import com.openxc.measurements.AcceleratorPedalPosition;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.FuelConsumed;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.Odometer;
import com.openxc.measurements.TransmissionGearPosition;
import com.openxc.measurements.TurnSignalStatus;
import com.openxc.measurements.VehicleSpeed;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // 42.337018, -83.051045

    private VehicleManager mVehicleManager;
    private DatabaseReference mFirebaseDatabaseReference;
    private TextView speedView;
    private Map<String, Object> carData;


    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the VehicleManager service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i("openxc", "Bound to VehicleManager");
            // When the VehicleManager starts up, we store a reference to it
            // here in "mVehicleManager" so we can call functions on it
            // elsewhere in our code.
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();

            // We want to receive updates whenever the EngineSpeed changes. We
            // have an EngineSpeed.Listener (see above, mSpeedListener) and here
            // we request that the VehicleManager call its receive() method
            // whenever the EngineSpeed changes
            mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener);
            mVehicleManager.addListener(AcceleratorPedalPosition.class, mAccelListener);
            mVehicleManager.addListener(BrakePedalStatus.class, mBrakeListener);
            mVehicleManager.addListener(FuelConsumed.class, mFuelListener);
            mVehicleManager.addListener(Odometer.class, mOdometerListener);
            mVehicleManager.addListener(TransmissionGearPosition.class, mTransmissionListener);
            mVehicleManager.addListener(TurnSignalStatus.class, mTurnListener);
            mVehicleManager.addListener(Latitude.class, mLatitudeListener);
            mVehicleManager.addListener(Longitude.class, mLongitudeListener);
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.w("openxc", "VehicleManager Service  disconnected unexpectedly");
            mVehicleManager = null;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        // When the activity starts up or returns from the background,
        // re-connect to the VehicleManager so we can receive updates.
        if(mVehicleManager == null) {
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
        public void receive(Measurement measurement) {
            // When we receive a new EngineSpeed value from the car, we want to
            // update the UI to display the new value. First we cast the generic
            // Measurement back to the type we know it to be, an EngineSpeed.
            final VehicleSpeed speed = (VehicleSpeed) measurement;
            // In order to modify the UI, we have to make sure the code is
            // running on the "UI thread" - Google around for this, it's an
            // important concept in Android.
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    // Finally, we've got a new value and we're running on the
                    // UI thread - we set the text of the EngineSpeed view to
                    // the latest value
                    carData.put("speed", speed.getValue().doubleValue());
                    mFirebaseDatabaseReference.setValue(carData);
                }
            });
             try {
                 Thread.sleep(500);
             } catch (Exception e) {}
        }
    };

    AcceleratorPedalPosition.Listener mAccelListener = new AcceleratorPedalPosition.Listener() {
        public void receive(Measurement measurement) {
            final AcceleratorPedalPosition position = (AcceleratorPedalPosition) measurement;
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    carData.put("accel", position.getValue().doubleValue());
                    mFirebaseDatabaseReference.setValue(carData);
                }
            });
        }
    };

    BrakePedalStatus.Listener mBrakeListener = new BrakePedalStatus.Listener() {
        public void receive(Measurement measurement) {
            final BrakePedalStatus position = (BrakePedalStatus) measurement;
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    carData.put("brake", position.getValue().booleanValue());
                    mFirebaseDatabaseReference.setValue(carData);
                }
            });
        }
    };

    FuelConsumed.Listener mFuelListener = new FuelConsumed.Listener() {
        public void receive(Measurement measurement) {
            final FuelConsumed position = (FuelConsumed) measurement;
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    carData.put("fuel_consumed", position.getValue().doubleValue());
                    mFirebaseDatabaseReference.setValue(carData);
                }
            });
        }
    };

    Odometer.Listener mOdometerListener = new Odometer.Listener() {
        public void receive(Measurement measurement) {
            final Odometer position = (Odometer) measurement;
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    carData.put("odometer", position.getValue().doubleValue());
                    mFirebaseDatabaseReference.setValue(carData);
                }
            });
        }
    };

    TransmissionGearPosition.Listener mTransmissionListener = new TransmissionGearPosition.Listener() {
        public void receive(Measurement measurement) {
            final TransmissionGearPosition position = (TransmissionGearPosition) measurement;
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    carData.put("transmission", position.getValue().enumValue().name());
                    mFirebaseDatabaseReference.setValue(carData);
                }
            });
        }
    };

    TurnSignalStatus.Listener mTurnListener = new TurnSignalStatus.Listener() {
        public void receive(Measurement measurement) {
            final TurnSignalStatus position = (TurnSignalStatus) measurement;
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    carData.put("turn_signal", position.getValue().enumValue().name());
                    mFirebaseDatabaseReference.setValue(carData);
                }
            });
        }
    };

    Latitude.Listener mLatitudeListener = new Latitude.Listener() {
        public void receive(Measurement measurement) {
            final Latitude position = (Latitude) measurement;
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    carData.put("latitude", position.getValue().doubleValue());
                    mFirebaseDatabaseReference.setValue(carData);
                }
            });
        }
    };

    Longitude.Listener mLongitudeListener = new Longitude.Listener() {
        public void receive(Measurement measurement) {
            final Longitude position = (Longitude) measurement;
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    carData.put("longitude", position.getValue().doubleValue());
                    mFirebaseDatabaseReference.setValue(carData);
                }
            });
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        carData = new HashMap<>();
    }
}
