package ch.ethz.inf.vs.android.fbuenzli.antitheft;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;


public class DatagraphView extends View
{
	// points
	private Paint px = new Paint();
	private Paint py = new Paint();
	private Paint pz = new Paint();
	// distance
	private Paint pd = new Paint();
	
	private Paint pt = new Paint(); // text / thick line
	private Paint pl = new Paint(); // thin line
	
	private Paint bigtext = new Paint(); // the 'learing' status
	private Paint progress = new Paint(); // background progress bar
	
	private List<DataContainer> data = new ArrayList<DataContainer>();
	
	private class DataContainer {
		public Vector3 v;
		public double  dist;
		
		DataContainer(Vector3 vect, double d) {
			v    = vect;
			dist = d;
		}
	}
	
	private SharedPreferences prefMgr;
	
	private AntiTheftService at_service;
	
	public DatagraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		prefMgr = PreferenceManager.getDefaultSharedPreferences(context);
		
		px.setColor(Color.rgb(0, 155, 200)); px.setAlpha(100);
		py.setColor(Color.rgb(0, 100, 255)); py.setAlpha(100);
		pz.setColor(Color.rgb(0,  55, 255)); pz.setAlpha(100);
		pd.setColor(Color.RED);
		pt.setColor(Color.BLACK);
		pl.setColor(Color.BLACK);
		
		px.setStrokeWidth(2);
		py.setStrokeWidth(2);
		pz.setStrokeWidth(2);
		pd.setStrokeWidth(3);
		
		pt.setStrokeWidth(1);
		pt.setTextSize(10);
		pl.setStrokeWidth(1);
		pl.setAlpha(50);
		
		bigtext.setTextAlign(Align.CENTER);
		bigtext.setTextSize(20);
		
		progress.setColor(Color.rgb(100, 255, 120));
		progress.setStrokeWidth(5);
	}
	
	@Override
	public void onDraw(Canvas c) {
		
		
		while(c.getWidth() < data.size()*2)
			data.remove(0);
		
		c.drawColor(Color.WHITE);
		
		
		
		float w = c.getWidth(), h = c.getHeight();
		float m = h/2; // height of center line (y=0)
		
		
		
		if(data == null || data.isEmpty())
			c.drawText("no data", w/2, h/3, bigtext);
		else
		{
			float max = 1.5f; // y-limit of 6g
			
			
			// draw learing state
			if(at_service != null) {
				if(at_service.isLearning()) {
					//c.drawColor(Color.rgb(255, 255, 200));
					c.drawRect(0, 0, w*at_service.getLearningPercent(), h, progress);
					c.drawText("learning", w/2, h/3, bigtext);
				}
			}
			
			
			// draw y-Axis (0g)
			c.drawLine(0, m, w, m, pd);
			c.drawText("0g", 2, m-2, pl);
			
			c.drawLine(0, m-m/max, w, m-m/max, pl); c.drawText( "1g", 2, m-m/max-2, pl);
			c.drawLine(0, m+m/max, w, m+m/max, pl); c.drawText("-1g", 2, m+m/max-2, pl);
			
			// draw threshold
			c.drawText("threshold", 25, m-2, pd);
			
			double lastdist = -1;
			int i = 0;
			for(DataContainer d: data)
			{
				c.drawPoint(i*2, (float) (m-d.v.x/max*m/10), px);
				c.drawPoint(i*2, (float) (m-d.v.y/max*m/10), py);
				c.drawPoint(i*2, (float) (m-d.v.z/max*m/10), pz);
				
				if(d.dist >= 0)
				{
					c.drawPoint(i*2, (float) (h-d.dist*m), pd);
					
					// display when thershold gets triggered (dist is normalized)
					if(d.dist>1)
						c.drawLine(i*2, 0, i*2, (float) (h-d.dist/m), pd);
					
					lastdist = d.dist;
				}
				
				i++;
			}
			
			
			if(lastdist >= 0 && prefMgr.getBoolean("pref_show_dist", false))
				c.drawText("Dist: "+Double.toString(lastdist), 5, 15, pt);
			
		}
		
		
	}
	
	public void addValue(Vector3 v, double dist)
	{
		data.add(new DataContainer(v, dist));
		
		this.invalidate(); // force a redraw, because we got new valuess
	}
	
	public void setATService(AntiTheftService ats) {
		at_service = ats;
	}
}
