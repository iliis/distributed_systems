package com.example.sensortest;

import java.util.ArrayList;
import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SensorActivity extends Activity implements SensorEventListener {
	
	TextView sensor_name;
	ListView sensor_values_list;
	final ArrayList<String> sensor_data = new ArrayList<String>();
	ArrayAdapter<String> sensor_data_adapter;
	
	Sensor my_sensor = null;
	SensorManager sensorMgr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        
        // init fields
        sensor_name = (TextView) findViewById(R.id.sensor_name);
        sensor_name.setText("unknown");
        
        sensor_values_list  = (ListView) findViewById(R.id.sensor_values);
        sensor_data_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sensor_data);
        sensor_values_list.setAdapter(sensor_data_adapter);
        
        sensorMgr  = (SensorManager) getSystemService(SENSOR_SERVICE);
        
        // find out which sensor we should display now
        Intent i = getIntent();
        
        int sensor_nr = i.getIntExtra("SensorNr", -1);
        if(sensor_nr < 0 || sensor_nr > 100)
        	Log.e("foo", "Intent failed to deliver sensible sensor id.");
        else
        {
        	List<Sensor> all_sensors = sensorMgr.getSensorList(Sensor.TYPE_ALL);
        	
        	// isn't there a nicer way of doing this? (apart from serializing the sensor object?)
        	my_sensor = all_sensors.get(sensor_nr);
        	
        	// display information about sensor
        	sensor_name.setText(my_sensor.getName());
        	((TextView) findViewById(R.id.sensor_vendor)).setText(my_sensor.getVendor());
        	((TextView) findViewById(R.id.sensor_power )).setText("Power Usage: "+Float.toString(my_sensor.getPower())+"mA");
        	
        	// register for data
        	sensorMgr.registerListener(this, my_sensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_sensor, menu);
        return true;
    }

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent ev) {
		
		// Not very nice way of passing values around. Directly setting the array's fields might be better. 
		sensor_data.clear();
		for(float v: ev.values)
			sensor_data.add(Float.toString(v));
		
		sensor_data_adapter.notifyDataSetChanged();
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	if(my_sensor != null)
    		sensorMgr.registerListener(this, my_sensor, SensorManager.SENSOR_DELAY_UI);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	if(my_sensor != null)
    	{
    		sensorMgr.unregisterListener(this);
    		Log.v("foo", "unregistered sensor");
    	}
    }
}
