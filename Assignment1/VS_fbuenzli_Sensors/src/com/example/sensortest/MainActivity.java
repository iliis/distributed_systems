package com.example.sensortest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private Context main_ctx;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        main_ctx = this;
        
        ListView mainlist = (ListView) findViewById(R.id.main_sensorlist);
        
        final ArrayList<String> mainlist_data = new ArrayList<String>();
        mainlist.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mainlist_data));
       
        
        
        SensorManager sensorMgr  = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> all_sensors = sensorMgr.getSensorList(Sensor.TYPE_ALL);
        
        for(Iterator<Sensor> s = all_sensors.iterator(); s.hasNext();)
        	mainlist_data.add(s.next().getName());
        
        //mainlist_adapter.notifyDataSetChanged();
        mainlist.setOnItemClickListener(new OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> adapter, View v, int pos, long l) {
        		/*String s = mainlist_data.get(pos);
        		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();*/
        		
        		Intent i = new Intent(main_ctx, SensorActivity.class);
        		i.putExtra("SensorNr", pos);
        		startActivity(i);
        	}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return false; // do not create a menu
    }
    
    public void switchToActuators(View v) {
    	startActivity(new Intent(this, ActuatorsActivity.class));
    }
}