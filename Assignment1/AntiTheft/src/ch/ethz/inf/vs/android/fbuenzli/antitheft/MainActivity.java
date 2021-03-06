package ch.ethz.inf.vs.android.fbuenzli.antitheft;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

public class MainActivity extends Activity
						  implements ToggleButton.OnCheckedChangeListener {
	
	
	private AntiTheftService  ATService = null;
	private ServiceConnection ATService_connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			AntiTheftService.ATServiceInterface interf = (AntiTheftService.ATServiceInterface) service;
			ATService = interf.getService();
			
			// not sure if it is intelligent in Java to have circular references
			DatagraphView graph = (DatagraphView) findViewById(R.id.main_graph);
			ATService.setGraph(graph);
			ATService.setMainActivity(MainActivity.this);
			graph.setATService(ATService);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			ATService = null;
		}
	};
	
	AlertDialog disarm_dialog;
	
	private Handler  delayed_handler = new Handler();
	private Runnable delayed_alarm   = new Runnable() {
		public void run() {
			if(ATService != null)
				ATService.startAlarm();
			
			disarm_dialog.cancel();
		}
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        MathHelpers.updateMacheps();
        
        // use this class as a listener for gui events
        ((ToggleButton) findViewById(R.id.enable_alarm))   .setOnCheckedChangeListener(this);
        
        // create service which does all the work for us
        bindService(new Intent(this, AntiTheftService.class), ATService_connection, BIND_AUTO_CREATE);

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu m) {
    	MenuInflater inf = getMenuInflater();
    	inf.inflate(R.menu.activity_main, m);
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.menu_settings:
    		startActivity(new Intent(this, SettingsActivity.class));
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    	
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	if(ATService != null)
    	{
    		unbindService(ATService_connection); // actually, android should do this automatically
    		ATService = null;
    	}
    }

	@Override
	public void onCheckedChanged(CompoundButton b, boolean checked) {
		if(ATService != null)
		{
			if(checked)
				displayStartLearningDialog();
			else
				ATService.stop();
		}
	}
	
	public void setToggleButtonState(boolean checked) {
		((ToggleButton) findViewById(R.id.enable_alarm)).setChecked(checked);
	}

    
    private void displayStartLearningDialog() {
    	// only display the dialog once
    	if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("start_dialog_displayed", false))
    		ATService.start();
    	else
    		PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("start_dialog_displayed", true).commit();
    	
    	
    	AlertDialog.Builder b = new AlertDialog.Builder(this);
    	b.setMessage("I will now measure the sensors for some time to learn how it feels just lying around. Please don't move me. "+
    	 "After "+Integer.toString(ATService.getLearnigTime())+" seconds I will be ready and will alert you if anything happens to me!")
    	 .setCancelable(false).setPositiveButton("start", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ATService.start();
			}
		});
    	
    	b.create().show();
    }
    
    
    // we have a confirmed alarm, but the user still has time to disarm it
    public void startCountdown() {
    	
    	if(ATService != null) {
	    	// stop listening, the alarm already happened
    		ATService.stop();
	    	
	    	// start delayed alarm
	    	delayed_handler.postDelayed(delayed_alarm, ATService.getDisarmTime()*1000);
	    	
	    	// provide user with option to disarm it
	    	AlertDialog.Builder b = new AlertDialog.Builder(this);
	    	b.setTitle("ALERT");
	    	b.setMessage("The phone was moved, but have still "+Integer.toString(ATService.getDisarmTime())+" seconds time to disarm it.")
	    	 .setPositiveButton("disarm", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					delayed_handler.removeCallbacks(delayed_alarm);
				}
			});
	    	
	    	disarm_dialog = b.create();
	    	disarm_dialog.show();
    	}
    }
}
