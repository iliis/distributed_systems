package ch.ethz.inf.vs.android.fbuenzli.antitheft;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.inf.vs.android.fbuenzli.antitheft.AntiTheftService.Vector3;

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
	
	private Paint pt = new Paint(); // text / thick line
	private Paint pl = new Paint(); // thin line
	
	private List<Vector3> data = new ArrayList<Vector3>();
	
	public DatagraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		px.setColor(Color.RED);
		py.setColor(Color.GREEN);
		pz.setColor(Color.BLUE);
		pt.setColor(Color.BLACK);
		pl.setColor(Color.BLACK);
		
		px.setStrokeWidth(2);
		py.setStrokeWidth(2);
		pz.setStrokeWidth(2);
		
		pt.setStrokeWidth(1);
		pt.setTextSize(10);
		pl.setStrokeWidth(1);
		pl.setAlpha(125);
		
	}
	
	@Override
	public void onDraw(Canvas c) {
		c.drawColor(Color.WHITE);
		
		
		if(data == null)
			c.drawText("no data", 10, 30, pt);
		else
		{
			float w = c.getWidth(), h = c.getHeight();
			float m = h/2; // height of center line (y=0)
			
			float max = 6; // y-limit of 6g
			
			// draw y-Axis (0g)
			c.drawLine(0, m, w, m, pt);
			c.drawText("0g", 2, m-8, pt);
			
			c.drawLine(0, m-m/max, w, m-m/max, pt); c.drawText( "1g", 2, m-m/max-8, pt);
			c.drawLine(0, m+m/max, w, m+m/max, pt); c.drawText("-1g", 2, m+m/max-8, pt);
			
			
			int i = 0;
			for(Vector3 v: data)
			{
				c.drawPoint(i*2, m-v.x/max*m, px);
				c.drawPoint(i*2, m-v.y/max*m, py);
				c.drawPoint(i*2, m-v.z/max*m, pz);
				
				i++;
			}
		}
	}
	
	public void addValue(Vector3 v)
	{
		data.add(v);
		this.invalidate(); // force a redraw, because we got new valuess
	}
}
