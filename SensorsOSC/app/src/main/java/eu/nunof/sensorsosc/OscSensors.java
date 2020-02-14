package eu.nunof.sensorsosc;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.transport.udp.OSCPortOut;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OscSensors extends Activity implements SensorEventListener {

	private int lastX = 0;
	private int lastY = 0;
	private int lastZ = 0;

	private SensorManager sensorManager;
    private Sensor gr_sensor;

	private TextView currentX, currentY, currentZ;
	private EditText oscAddress;
	private TextView infoBox;
	private Button btStart, btStop;

    private String osc_ip = "10.10.10.12";
    private int osc_port = 8891;

    private OSCPortOut osc_client;


    public static boolean isIpAddress(String ipAddress) {
        String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }

    public void startOsc(View view) {
        displayCleanValues();
        if (isIpAddress(oscAddress.getText().toString())) {
            btStart.setEnabled(false);
            btStop.setEnabled(true);
            osc_ip = oscAddress.getText().toString();
            if (oscThread.isAlive()) {
                oscThread.interrupt();
                oscThread.start();
            }
            else oscThread.start();
        }
        else {
            if (oscThread.isAlive()) oscThread.interrupt();
        }
    }

    public void stopOsc(View view) {

        btStart.setEnabled(true);
        btStop.setEnabled(false);
        if (oscThread.isAlive()) oscThread.interrupt();
    }


        @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_osc_sensors);
		initializeViews();

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR) != null) {

            gr_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
            sensorManager.registerListener(this, gr_sensor, 50 * 1000);

        } else {
            Log.e("OSCSensors", "PANIC, can't find virtual sensor!");
        }
	}

	public void initializeViews() {
		currentX = (TextView) findViewById(R.id.currentX);
		currentY = (TextView) findViewById(R.id.currentY);
		currentZ = (TextView) findViewById(R.id.currentZ);
        oscAddress = (EditText) findViewById(R.id.server_address);
        infoBox = (TextView) findViewById(R.id.status_info);
        btStart = (Button) findViewById(R.id.start_osc);
        btStop = (Button) findViewById(R.id.stop_osc);

        displayCleanValues();
	}

	protected void onResume() {
		super.onResume();
        sensorManager.registerListener(this, gr_sensor, 50 * 1000);
	}

	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {

        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) return;

        int vX = java.lang.Math.round(event.values[2] * -5);
        int vY = java.lang.Math.round(event.values[0] * 5);
        int vZ = java.lang.Math.round(event.values[1] * 5);
        //Log.d("OSCSensors", "X="+vX+":Y="+vY+":Z="+vZ);
/*
	    if (Math.abs(vX) > lastX) lastX = vX;
        //Log.d("OSCSensors", "X="+lastX);
        if (Math.abs(vY) > lastY) lastY = vY;
        //Log.d("OSCSensors", "Y="+lastY);
        if (Math.abs(vZ) > lastZ) lastZ = vZ;
*/

        lastX = vX;
        lastY = vY;
        lastZ = vZ;

        displayCurrentValues();

	}

	public void displayCleanValues() {
		currentX.setText("0.0");
		currentY.setText("0.0");
		currentZ.setText("0.0");
	}

	// display the current x,y,z accelerometer values
	public void displayCurrentValues() {
        currentX.setText(Float.toString(lastX));
		currentY.setText(Float.toString(lastY));
		currentZ.setText(Float.toString(lastZ));

	}

    private Thread oscThread = new Thread() {
        @Override
        public void run() {

            Log.d("OSCSensors", "started oscThread");
            try {
                osc_client = new OSCPortOut(InetAddress.getByName(osc_ip), osc_port);
            } catch (UnknownHostException e) {
                Log.d("OSCSensors", e.getMessage());
                return;
            } catch (Exception e) {
                Log.d("OSCSensors", e.getMessage());
                return;
            }

            while (true) {

                //Log.d("OSCSensors", "X="+lastX+":Y="+lastY+":Z="+lastZ);

                if (lastX != 0 || lastY != 0 || lastZ!= 0) {
                    if (osc_client != null) {

                        Object[] osc_msg = new Object[4];
                        // bug in OSC lib. First object needs to be a string
                        osc_msg[0] = "inputmov";
                        osc_msg[1] = lastX;
                        osc_msg[2] = lastY;
                        osc_msg[3] = lastZ;
                        lastX = lastY = lastZ = 0;
                        java.util.List<Object> osc_struct = Arrays.asList(osc_msg);
                        OSCMessage message = new OSCMessage("/inputmov", osc_struct);

                        try {
                            osc_client.send(message);
                            //Log.d("OSCSensors", "osc sent " + osc_struct.toString());
                        } catch (Exception e) {
                            Log.e("OSCSensors", "error sending");
                        }
                    }
                }

                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    lastX = lastY = lastZ = 0;
                    return;
                }
            }
        }
    };
}

