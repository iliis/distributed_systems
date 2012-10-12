package ch.ethz.inf.vs.android.fbuenzli.antitheft;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

public class AntiTheftService extends Service
							  implements SensorEventListener, OnSharedPreferenceChangeListener {
	
	// android specific stuff
	NotificationManager notifMgr;
	SharedPreferences	prefMgr;
	SensorManager		sensorMgr;
	DatagraphView		graph;
	MainActivity		mainActivity;
	private boolean running = false;
	
	private static final int NOTIFICATION_ID = 1;
	
	private static final double NS_TO_SECONDS = 1000000000;
	
	// actual implementation
	
	// in seconds:
	private int disarm_time = 20,
			significant_time = 5,
			sampling_time = 5; // don't set this too high, otherwise the whole thing becomes numerically unstable!
	private double significant_percent = 0.1; // amount of samples inside sampling_time interval which must be above threshold
	
	public double dist = -1; // last measured distance -> for access in DatagraphView
	
	//private long start_activity = 0; // time when first measured something above threshold
	//private boolean activity_detected = false; 
	
	
	private double threshold = 500; // alarm when Mahalanobis distance is bigger than this value
	
	// used to calculate the resting behaviour of the sensor
	private List<Vector3> samples = new ArrayList<Vector3>();
	private long sampling_start_time; // timestamp
	private boolean sampling_done = false;
	private Vector3 mean;
	private Matrix inv_cov; // inverse covariance

	private int sensor_speed = SensorManager.SENSOR_DELAY_NORMAL;
	private boolean alarm_enabled = true; // you can disable firing the alarm
	private boolean alarm_immediately = false; // override the Significant Time Window
	
	private List<Event> events = new LinkedList<Event>();
	
	private class Event {
		public boolean value;
		public double timestamp; // in seconds
		
		Event(boolean v, double ts) {
			value = v;
			timestamp = ts;
		}
	}
	
	
	
	@Override
    public void onCreate() {
    	super.onCreate();
    	
    	notifMgr  = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
    	prefMgr   = PreferenceManager.getDefaultSharedPreferences(this);
    	prefMgr.registerOnSharedPreferenceChangeListener(this);
    	
    	readSettings();
    	
    	Log.d("foo", "AntiTheftService CREATED");
    }
    
    public void readSettings() {
    	significant_time    = Integer.parseInt(prefMgr.getString("pref_sig_time",     Integer.toString(significant_time)));
    	sampling_time       = Integer.parseInt(prefMgr.getString("pref_learing_time", Integer.toString(sampling_time)));
    	sensor_speed        = Integer.parseInt(prefMgr.getString("pref_sensorspeed",  "1"));
    	
    	significant_percent = Double.parseDouble(prefMgr.getString("pref_sig_percent", Double.toString(significant_percent*100)))/100;
    	threshold           = Double.parseDouble(prefMgr.getString("pref_threshold",   Double.toString(threshold)));
    	
    	alarm_enabled       = prefMgr.getBoolean("pref_alarm_enabled", true);
    	alarm_immediately   = prefMgr.getBoolean("pref_activate_immediately", false);
    	
    	// check inputs
    	
    	if(significant_time < 1) significant_time = 1;
    	if(sampling_time < 1) sampling_time = 1;
    	
    	switch(sensor_speed) {
    	case 0:
    		sensor_speed = SensorManager.SENSOR_DELAY_NORMAL; break;
    	case 1:
    		sensor_speed = SensorManager.SENSOR_DELAY_UI; break;
    	case 2:
    		sensor_speed = SensorManager.SENSOR_DELAY_GAME; break;
    	case 3:
    		sensor_speed = SensorManager.SENSOR_DELAY_FASTEST; break;
    	
    	default:
    		sensor_speed = SensorManager.SENSOR_DELAY_NORMAL; break;
    	}
    	
    	if(running) {
    		sensorMgr.unregisterListener(this);
    		sensorMgr.registerListener(this, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensor_speed);
    	}
    	
    	if(significant_percent > 1) significant_percent = 1;
    	if(significant_percent < 0) significant_percent = 0;
    }
    
    @Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		readSettings();
	}
    
    @Override
    public int onStartCommand(Intent i, int flags, int id) {
    	
		return START_STICKY; // let it run until it is explixitly killed
    	
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	stop(); // to make sure the service is stopped when the user quits the application
    	
    	Log.d("foo", "AntiTheftService KILLED");
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
    
    public void setMainActivity(MainActivity ma) {
    	mainActivity = ma;
    }
    
    public boolean isLearning() {
    	return !sampling_done && running;
    }
    
    public float getLearningPercent() {
    	if(isLearning())
    		return (float) (((System.nanoTime() - sampling_start_time)/NS_TO_SECONDS) / sampling_time);
    	else
    		return 0;
    }
    
    public int getLearnigTime() {
    	return sampling_time;
    }
    
    public int getDisarmTime() {
    	return disarm_time;
    }
    
    // lock the phone and report any suspicious measurements
    public void start() {
    	if(!running) {
    		
        	// register for sensor data
        	sensorMgr.registerListener(this, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensor_speed);
        	sampling_start_time = 0;
    		
    		running = true;
    		
    		Log.d("foo", "AntiTheftService STARTED");
    	}
    }
    
    private void displayNotification() {
    	Notification n = new Notification(R.drawable.ic_launcher, "phone is now locked", System.currentTimeMillis());
    	
    	// don't allow notification to be cleared directly by user
    	// deactivate for easier debugging (allowing manual cleanup of notification) 
    	n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
    	
    	// open MainActivity when Notification is clicked
    	PendingIntent i = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
    	
    	n.setLatestEventInfo(this, "AntiTheft", "your device is currently locked and protected against movement", i);
    	
    	    	
    	// display Notification
    	//notifMgr.notify(NOTIFICATION_ID, n);
    	startForeground(NOTIFICATION_ID, n);
    }
    
    // unlock the phone
    public void stop() {
    	if(running) {
    		sensorMgr.unregisterListener(this);
    		notifMgr.cancel(NOTIFICATION_ID);
    		running = false;
    		samples.clear();
    		sampling_done = false;
    		sampling_start_time = 0;
    		
    		Log.d("foo", "AntiTheftService STOPPED");
    		
    		if(mainActivity != null)
    			mainActivity.setToggleButtonState(false);
    	}
    }
    
    public void startAlarm() {
    	this.stop(); // we don't need to watch it anymore, it has already happened
    	
		final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
		tg.startTone(ToneGenerator.TONE_PROP_BEEP);
		
		((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(500);
    }

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent ev) {
		
		Vector3 v = new Vector3(ev.values[0], ev.values[1], ev.values[2]);
		dist = -1; // mahalanobis distance of new value
		
		// SAMPLING LOGIC
		// measure the base distribution of an undisturbed phone
		///////////////////////////////////////////////////////////////////////
				
		if(!sampling_done) {
			
			// measure start of sampling period
			if(sampling_start_time == 0)
				sampling_start_time = ev.timestamp;
			
			// is sampling period over?
			if((ev.timestamp - sampling_start_time)/NS_TO_SECONDS >= sampling_time) {
				
				MathHelpers.updateMacheps();
				
				sampling_done = true;
				
				Log.d("foo", "sampling done");
				
				mean = MathHelpers.calculate_mean(samples);
				
				Log.d("foo", "got "+Integer.toString(samples.size())+" samples.");
				/*Log.d("foo", "mean.x: "+Double.toString(mean.x));
				Log.d("foo", "mean.y: "+Double.toString(mean.y));
				Log.d("foo", "mean.z: "+Double.toString(mean.z));*/
				
				
				// invert the covariance matrix
				//double[][] cov = MathHelpers.calculate_covariance(samples, mean);
				inv_cov =  MathHelpers.pinv(new Matrix(MathHelpers.calculate_covariance(samples, mean)));
				
				
				
				/*Matrix A = new Matrix(cov);
				SingularValueDecomposition A_svd = new SingularValueDecomposition(A);
				
				Log.d("foo", "--------------- Matrix (array):");
				Log.d("foo", MathHelpers.matrixToMatlabCode3(cov));
				
				Log.d("foo", "--------------- Matrix:");
				Log.d("foo", MathHelpers.matrixToMatlabCode(A));
				
				Log.d("foo", "--------------- Jama SVD: U:");
				Log.d("foo", MathHelpers.matrixToString(A_svd.getU()));
				
				Log.d("foo", "--------------- Jama SVD: S:");
				Log.d("foo", MathHelpers.matrixToString(A_svd.getS()));
				
				Log.d("foo", "--------------- Jama SVD: V:");
				Log.d("foo", MathHelpers.matrixToString(A_svd.getV()));
				
				Log.d("foo", "--------------- Jama SVD: Pseudoinv:");
				Log.d("foo", MathHelpers.matrixToString(MathHelpers.pseudo_invert3(A)));
				
				Log.d("foo", "--------------- Jama SVD: inv:");
				Log.d("foo", MathHelpers.matrixToString(MathHelpers.pinv(A)));*/
				
								
				// alert the user that the sampling is done and the phone is now locked
				displayNotification();
				
			} else
			{
				//Log.d("foo","Sampled point.");
				samples.add(v);
			}
		}
		else {
			
			dist = MathHelpers.mahalanobis3(inv_cov, mean.getColumnVector(), v.getColumnVector());
			Log.d("foo","Got point. Distance = "+Double.toString(dist));
		}
		
		
		if(graph != null)
			// copy values for easier access and because ev.values cannot be passed by reference
			graph.addValue(new Vector3(ev.values[0], ev.values[1], ev.values[2]), dist/threshold);
		else
			Log.w("foo", "no graphview set");
		
		
		if(sampling_done) {
				
			// ALARM LOGIC
			// did we get a significant event?
			///////////////////////////////////////////////////////////////////////
			
			if(dist > 0 && dist > threshold) {
				events.add(new Event(true, ev.timestamp/NS_TO_SECONDS));
				
				// immediate alarm fires as soon as an event is over the threshold
				if(alarm_enabled && alarm_immediately)
					startAlarm();
			}
			else
				events.add(new Event(false, ev.timestamp/NS_TO_SECONDS));
			
			// clean up events which are older
			while(!events.isEmpty() && events.get(0).timestamp < ev.timestamp/NS_TO_SECONDS-significant_time)
				events.remove(0);
			
			
			// check if our significant time window reaches the needed percentage of events
			if(alarm_enabled && !alarm_immediately) {
				
				float count_t = 0, count_f = 0;
				for(Event e: events) {
					if(e.value)
						count_t ++;
					else
						count_f ++;
				}
				
				Log.d("foo", "percentage: "+Float.toString(count_t/(count_t+count_f))+" > "+Double.toString(significant_percent));
				Log.d("foo", "count: "+Float.toString(count_t+count_f));
				if(count_t/(count_t+count_f) >= significant_percent && mainActivity != null)
					mainActivity.startCountdown(); // do not immediately alarm the user, instead give him time to disarm it first
			}
		} else
			events.add(new Event(false, ev.timestamp/NS_TO_SECONDS)); // 'zero padding', so that we can calculate a somewhat reasonable percentage
														// (otherwise, we would get a very high percentage shortly after finishing sampling,
														//  because we have almost no events in our window)
	}
}
