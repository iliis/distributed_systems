package ch.ethz.inf.vs.android.fbuenzli.antitheft;

import java.util.List;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.content.ComponentName;
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
			
			ATService.setGraph((DatagraphView) findViewById(R.id.main_graph));
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
    public void onStop() {
    	super.onStop();
    	
    	// TODO: there might be someting wrong with this:
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
				ATService.start();
			else
				ATService.stop();
		}
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
