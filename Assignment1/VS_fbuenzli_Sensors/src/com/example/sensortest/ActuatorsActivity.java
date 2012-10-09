package com.example.sensortest;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ActuatorsActivity extends Activity implements OnSeekBarChangeListener {
	
	private long vibration_ms = 100;
	private long vibration_ms_min = 10,
				 vibration_ms_max = 1000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actuators);
        
        SeekBar vibrate_seekbar = (SeekBar) findViewById(R.id.actuators_seekbar);
        vibrate_seekbar.setOnSeekBarChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_actuators, menu);
        return true;
    }
    
    public void onClickVibrate(View v) {
    	((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(vibration_ms);
    }
    
    public void onSliderChanged(SeekBar b, int progress, boolean u) {

    }

	@Override
	public void onProgressChanged(SeekBar b, int progress, boolean u) {
		vibration_ms = (long) (progress*((double) (vibration_ms_max-vibration_ms_min)/b.getMax()) + vibration_ms_min);
    	
    	Button vibrate_button = (Button) findViewById(R.id.actuators_button_vibrate);
    	vibrate_button.setText("vibrate ("+Long.toString(vibration_ms)+" ms)");
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// Auto-generated method stub
	}
	
	public void onClickPlaySound(View v) {
		MediaPlayer mp = MediaPlayer.create(this, R.raw.kaptainpolka);
		mp.setVolume(1, 1);
		mp.start();
	}
}
