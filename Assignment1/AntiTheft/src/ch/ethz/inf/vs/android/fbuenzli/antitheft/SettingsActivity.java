package ch.ethz.inf.vs.android.fbuenzli.antitheft;

import java.util.ArrayList;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.content.pm.PackageManager;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.main_preferences);
        
        
        
        // check which sensor we can use
        ArrayList<CharSequence> sensor_names  = new ArrayList<CharSequence>();


        PackageManager PM= this.getPackageManager();
        
        /*if(PM.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS))
        	sensor_names.add("GPS");
        
        if(PM.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
        	sensor_names.add("Bluetooth");
        
        if(PM.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK))
        	sensor_names.add("Network Position");
        
        if(PM.hasSystemFeature(PackageManager.FEATURE_WIFI))
        	sensor_names.add("WIFI");
        
        if(PM.hasSystemFeature(PackageManager.FEATURE_MICROPHONE))
        	sensor_names.add("Microphone");
        
        if(PM.hasSystemFeature(PackageManager.FEATURE_NFC))
        	sensor_names.add("NFC");*/
                	
        if(PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER))
        	 sensor_names.add("Accelerometer");

        if(PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_BAROMETER))
        	sensor_names.add("Barometer");
        
        if(PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS))
        	sensor_names.add("Compass");
        
        if(PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE))
        	sensor_names.add("Gyroscope");
        
        if(PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_LIGHT))
        	sensor_names.add("Light");
        
        
        ListPreference lp = (ListPreference) findPreference("pref_active_sensors");
        lp.setEntries(sensor_names.toArray(new CharSequence[sensor_names.size()]));
        lp.setEntryValues(lp.getEntries());
    }
}
