package ch.ethz.inf.vs.android.fbuenzli.antitheft;

import java.util.List;

import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
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

public class MainActivity extends Activity
						  implements ToggleButton.OnCheckedChangeListener,
						  			 OnSeekBarChangeListener {
	
	
	private AntiTheftService  ATService = null;
	private ServiceConnection ATService_connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			AntiTheftService.ATServiceInterface interf = (AntiTheftService.ATServiceInterface) service;
			ATService = interf.getService();
			
			// not sure if it is intelligent in Java to have circular references
			DatagraphView graph = (DatagraphView) findViewById(R.id.main_graph);
			ATService.setGraph(graph);
			graph.setATService(ATService);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			ATService = null;
		}
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // use this class as a listener for gui events
        ((ToggleButton) findViewById(R.id.enable_alarm))   .setOnCheckedChangeListener(this);
        ((SeekBar)      findViewById(R.id.main_disarm_bar)).setOnSeekBarChangeListener(this);
        
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
    
    /*@Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	// TODO: there might be someting wrong with this:
    	if(ATService != null)
    	{
    		unbindService(ATService_connection); // actually, android should do this automatically
    		ATService = null;
    	}
    }*/

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
	

    
    private void displayStartLearningDialog() {
    	AlertDialog.Builder b = new AlertDialog.Builder(this);
    	b.setMessage("I will now measure the sensors for some time to learn how it feels just lying around. Please don't move me."+
    	 "After around 5 seconds I will be ready and will alert you if anything happens to me!")
    	 .setCancelable(false).setPositiveButton("start", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ATService.start();
			}
		});
    	
    	b.create().show();
    }

	@Override
	public void onProgressChanged(SeekBar b, int value, boolean u) {
		// ibration_ms = (long) (progress*((double) (vibration_ms_max-vibration_ms_min)/b.getMax()) + vibration_ms_min);
		
		TextView disarm_text = (TextView) findViewById(R.id.main_disarm_text);
		disarm_text.setText("disarming time: "+Integer.toString(value)+"s");
		
		if(ATService != null) ATService.setDisarmTime(value);
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// Auto-generated method stub
		
	}
}
