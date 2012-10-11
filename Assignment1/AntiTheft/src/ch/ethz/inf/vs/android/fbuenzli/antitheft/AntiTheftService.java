package ch.ethz.inf.vs.android.fbuenzli.antitheft;

import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

public class AntiTheftService extends Service implements SensorEventListener {
	
	// android specific stuff
	NotificationManager notifMgr;
	SensorManager		sensorMgr;
	DatagraphView		graph;
	private boolean running = false;
	
	private static final int NOTIFICATION_ID = 1;
	
	
	// actual implementation
	
	// in seconds:
	private int disarm_time = 20,
			significant_time = 5,
			sampling_time = 5; // don't set this too high, otherwise the whole thing becomes numerically unstable!
	
	private long start_activity = 0; // time when first measured something above threshold
	private boolean activity_detected = false; 
	
	
	private double threshold = 500; // alarm when Mahalanobis distance is bigger than this value
	
	// used to calculate the resting behaviour of the sensor
	private List<Vector3> samples = new ArrayList<Vector3>();
	private long sampling_start_time; // timestamp
	private boolean sampling_done = false;
	private Vector3 mean;
	private Matrix inv_cov; // inverse covariance

	
	
	
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
    		
        	// register for sensor data
        	sensorMgr.registerListener(this, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        	sampling_start_time = 0;
    		
    		running = true;
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
    	notifMgr.notify(NOTIFICATION_ID, n);
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
    		
    		// find a way to tell the activity to disable the togglebutton
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
		double dist = -1; // mahalanobis distance of new value
		
		if(!sampling_done) {
			
			// measure start of sampling period
			if(sampling_start_time == 0)
				sampling_start_time = ev.timestamp;
			
			// is sampling period over?
			if((ev.timestamp - sampling_start_time)/1000000000 >= sampling_time) {
				
				MathHelpers.updateMacheps();
				
				sampling_done = true;
				
				Log.d("foo", "sampling done");
				
				mean = MathHelpers.calculate_mean(samples);
				
				Log.d("foo", "got "+Integer.toString(samples.size())+" samples.");
				/*Log.d("foo", "mean.x: "+Double.toString(mean.x));
				Log.d("foo", "mean.y: "+Double.toString(mean.y));
				Log.d("foo", "mean.z: "+Double.toString(mean.z));*/
				
				
				// invert the covariance matrix
				double[][] cov = MathHelpers.calculate_covariance(samples, mean);
				inv_cov =  MathHelpers.pinv(new Matrix(cov));
				
				
				
				
				/*cov[0] = new double[] {1.1734660799470433E-6,	3.390010528442641E-6,	-7.736200008350378E-6};
				cov[1] = new double[] {3.390010528442641E-6,	9.793356262560716E-6,	-2.2349005162236438E-5};
				cov[2] = new double[] {-7.736200008350378E-6,	-2.2349005162236438E-5,	5.100172181534321E-5};
				
				/*cov[0] = new double[] {1,	 3,	 -8};
				cov[1] = new double[] {3,	10,	-22};
				cov[2] = new double[] {-8, -22,	 51};*/

				
				/*Log.d("foo", "--------------- covariance:");
				Log.d("foo", MathHelpers.matrixToMatlabCode3(cov));
				
				
				Log.d("foo", "--------------- inverted covariance:");
				Log.d("foo", MathHelpers.matrixToString3(inv_cov));
				
				
				
				double[][][] qr = MathHelpers.QR3(cov);
				Log.d("foo", "--------------- Q:");
				Log.d("foo", MathHelpers.matrixToString3(qr[0]));
				
				Log.d("foo", "--------------- R:");
				Log.d("foo", MathHelpers.matrixToString3(qr[1]));
				
				Log.d("foo", "--------------- Q*R:");
				Log.d("foo", MathHelpers.matrixToString3(MathHelpers.mult3(qr[0], qr[1])));
				
				Log.d("foo", "--------------- A-Q*R:");
				Log.d("foo", MathHelpers.matrixToString3(MathHelpers.minus3(cov, MathHelpers.mult3(qr[0], qr[1]))));
				
				
				double[][][] svd = MathHelpers.SVD3(cov);
				Log.d("foo", "--------------- U:");
				Log.d("foo", MathHelpers.matrixToString3(svd[0]));
				
				Log.d("foo", "--------------- S:");
				Log.d("foo", MathHelpers.matrixToString3(svd[1]));
				
				Log.d("foo", "--------------- V:");
				Log.d("foo", MathHelpers.matrixToString3(svd[2]));
				
				Log.d("foo", "--------------- Pseudoinverse:");
				Log.d("foo", MathHelpers.matrixToString3(MathHelpers.pseudo_invert3(cov)));
				
				Log.d("foo", "--------------- real inverse:");
				Log.d("foo", MathHelpers.matrixToString3(MathHelpers.invert3(cov)));
				
				Log.d("foo", "--------------- real inverse * matrix:");
				Log.d("foo", MathHelpers.matrixToString3(MathHelpers.mult3(cov, MathHelpers.invert3(cov))));*/
				
				
				Matrix A = new Matrix(cov);
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
				Log.d("foo", MathHelpers.matrixToString(MathHelpers.pinv(A)));
				
								
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
			
			if(dist > threshold)
				startAlarm();
			
		}
		
		
		if(graph != null)
			// copy values for easier access and because ev.values cannot be passed by reference
			graph.addValue(new Vector3(ev.values[0], ev.values[1], ev.values[2]), dist/threshold);
		else
			Log.d("foo", "no graphview set");
	}
	
	public boolean isSignificant(Vector3 v) {
		
		
		
		return true;
	}
}
