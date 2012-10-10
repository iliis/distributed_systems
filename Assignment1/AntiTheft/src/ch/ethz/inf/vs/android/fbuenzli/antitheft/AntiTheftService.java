package ch.ethz.inf.vs.android.fbuenzli.antitheft;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class AntiTheftService extends Service implements SensorEventListener {
	
	// android specific stuff
	NotificationManager notifMgr;
	SensorManager		sensorMgr;
	DatagraphView		graph;
	private boolean running = false;
	
	private static final int NOTIFICATION_ID = 1;
	
	
	// actual implementation
	private int disarm_time = 20, significant_time = 5; // in seconds
	//public List<Vector3> sensor_data = new ArrayList<Vector3>();
	
	public class Vector3 {
		public float x,y,z;
		
		
		public Vector3() {
			x = 0; y = 0; z = 0;
		}
		
		public Vector3(float X, float Y, float Z) {
			x = X; y = Y; z = Z;
		}
	}
	
	
	
    public AntiTheftService() {
    }
    
    @Override
    public void onCreate() {
    	super.onCreate();
    	
    	notifMgr  = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
    	
    	
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	stop(); // to make sure the service is stopped when the user quits the application
    	
    	Log.d("foo", "service destroyed");
    }
    
    
    // Interface for communication with this service:
    public class ATServiceInterface extends Binder {
    	AntiTheftService getService() {
    		return AntiTheftService.this;
    	}
    }

    @Override
    public IBinder onBind(Intent intent) {
    	return new ATServiceInterface();
    }
    
    public void setDisarmTime(int t) {
    	disarm_time = t;
    }
    
    public void setGraph(DatagraphView v) {
    	graph = v;
    }
    
    
    
    // lock the phone and report any suspicious measurements
    public void start() {
    	if(!running) {
    		Notification n = new Notification(R.drawable.ic_launcher, "phone is now locked", System.currentTimeMillis());
    	
	    	// don't allow notification to be cleared directly by user
	    	// deactivate for easier debugging (allowing manual cleanup of notification) 
	    	n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
	    	
	    	// open MainActivity when Notification is clicked
	    	PendingIntent i = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
	    	
	    	n.setLatestEventInfo(this, "AntiTheft", "your device is currently locked and protected against movement", i);
	    	
	    	
	    	
	    	// register for sensor data
	    	sensorMgr.registerListener(this, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
	    	
	    	
	    	    	
	    	// display Notification
	    	notifMgr.notify(NOTIFICATION_ID, n);
	    	
	    	running = true;
    	}
    }
    
    // unlock the phone
    public void stop() {
    	if(running) {
    		sensorMgr.unregisterListener(this);
    		notifMgr.cancel(NOTIFICATION_ID);
    		running = false;
    	}
    }

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent ev) {
		
		Log.d("foo", "got value: "+Float.toString(ev.values[0]));
		
		if(graph != null)
			graph.addValue(new Vector3(ev.values[0], ev.values[1], ev.values[2]));
		else
			Log.d("foo", "no graphview set");
	}
}
