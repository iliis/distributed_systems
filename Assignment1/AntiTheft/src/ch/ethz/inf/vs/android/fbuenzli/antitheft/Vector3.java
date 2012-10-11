package ch.ethz.inf.vs.android.fbuenzli.antitheft;

public class Vector3 {
	public double x,y,z;
	
	
	public Vector3() {
		x = 0; y = 0; z = 0;
	}
	
	public Vector3(double X, double Y, double Z) {
		x = X; y = Y; z = Z;
	}
	
	public double get(int i) {
		switch(i) {
		case 0: return x;
		case 1: return y;
		case 2: return z;
		default: return -1; // In a real application we would use an exception here.
		}
		
	}
	
	public double norm() {
		return Math.sqrt(x*x + y*y + z*z);
	}
	
	public void normalize() {
		double n = norm();
		if(Math.abs(n) < Double.MIN_VALUE*1024) { // prevent NaN's
			x = 0;
			y = 0;
			z = 0;
		}
		else {
			x /= n;
			y /= n;
			z /= n;
		}
	}
	
	public Vector3 plus(Vector3 v) {
		return new Vector3(v.x+x, v.y+y, v.z+z);
	}
	
	public Vector3 minus(Vector3 v) {
		return new Vector3(x-v.x, y-v.y, z-v.z);
	}
	
	public Vector3 mult(Vector3 v) {
		return new Vector3(v.x*x, v.y*y, v.z*z);
	}
	
	public Vector3 mult(double v) {
		return new Vector3(v*x, v*y, v*z);
	}
	
	public double scalarmult(Vector3 v) {
		return v.x*x + v.y*y + v.z*z;
	}
	
	public Vector3 clone() {
		return new Vector3(x,y,z);
	}
	
	// project this vector on to e
	public Vector3 proj(Vector3 e) {
		return e.mult(e.scalarmult(this) / e.scalarmult(e));
	}
}