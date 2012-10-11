package ch.ethz.inf.vs.android.fbuenzli.antitheft;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
	
	private List<DataContainer> data = new ArrayList<DataContainer>();
	
	private class DataContainer {
		public Vector3 v;
		public double  dist;
		
		DataContainer(Vector3 vect, double d) {
			v    = vect;
			dist = d;
		}
	}
	
	public DatagraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		px.setColor(Color.rgb(0, 155, 200));
		py.setColor(Color.rgb(0, 100, 255));
		pz.setColor(Color.rgb(0,  55, 255));
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
		
	}
	
	@Override
	public void onDraw(Canvas c) {
		
		// TODO: implement this nicely
		
		
		while(c.getWidth() < data.size()*2)
			data.remove(0);
		
		c.drawColor(Color.WHITE);
		
		
		if(data == null)
			c.drawText("no data", 10, 30, pt);
		else
		{
			float w = c.getWidth(), h = c.getHeight();
			float m = h/2; // height of center line (y=0)
			
			float max = 2.5f; // y-limit of 6g
			
			// draw y-Axis (0g)
			c.drawLine(0, m, w, m, pt);
			c.drawText("0g", 2, m-8, pt);
			
			c.drawLine(0, m-m/max, w, m-m/max, pl); c.drawText( "1g", 2, m-m/max-8, pt);
			c.drawLine(0, m+m/max, w, m+m/max, pl); c.drawText("-1g", 2, m+m/max-8, pt);
			
			
			int i = 0;
			for(DataContainer d: data)
			{
				c.drawPoint(i*2, (float) (m-d.v.x/max*m/10), px);
				c.drawPoint(i*2, (float) (m-d.v.y/max*m/10), py);
				c.drawPoint(i*2, (float) (m-d.v.z/max*m/10), pz);
				
				if(d.dist >= 0)
					c.drawPoint(i*2, (float) (h-d.dist-1), pd);
				
				i++;
			}
		}
	}
	
	public void addValue(Vector3 v, double dist)
	{
		data.add(new DataContainer(v, dist));
		
		this.invalidate(); // force a redraw, because we got new valuess
	}
}
