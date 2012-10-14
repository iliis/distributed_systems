package ch.ethz.inf.vs.android.fbuenzli.antitheft;

import java.util.List;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import android.util.Log;

public class MathHelpers {
	
	public static final double EPS = Double.MIN_VALUE;
	
	// STATISTICAL OPERATIONS
	///////////////////////////////////////////////////////////////////////////
	
	public static Matrix calculate_mean(List<Matrix> data) {
		
		if(data.size() == 0)
			return null;
		
		assert(data.get(0).getColumnDimension() == 1);
		
		Matrix mean = new Matrix(data.get(0).getRowDimension(), data.get(0).getColumnDimension(), 0);
		int count = 0;
		
		for(Matrix v: data)
		{
			mean.plusEquals(v);	
			count++;
		}
		
		
		mean.timesEquals(1/((double)count));
		
		return mean;
	}
	
	public static Matrix calculate_covariance(List<Matrix> data, Matrix mean) {
		assert(mean.getColumnDimension() == 1);
		
		
		int s = mean.getRowDimension();
		Matrix cov = new Matrix(s, s);
		
		// calculate covariances (I long for matrices!)
		for(Matrix v: data)
		{
			for(int i=0; i<s; i++)
				for(int j=0; j<s; j++)
					cov.set(i, j, (v.get(i,0) - mean.get(i,0)) * (v.get(j,0) - mean.get(j,0)));
		}
		
		cov.timesEquals(1/((double)data.size()-1)); 
		
		return cov;
	}
	

	
	
	public static double mahalanobis(Matrix inv_cov, Matrix mean, Matrix v) {
		Matrix d = v.minus(mean);
		
		Matrix r = d.transpose().times(inv_cov.times(d));
		return Math.sqrt(r.get(0, 0));
		
		/*Vector3 d = new Vector3(v.x - mean.x,
								v.y - mean.y,
								v.z - mean.z);
		Vector3 e = mult3(inv_cov, d);
		
		double prod = d.scalarmult(e);
		
		
		
		
		if(prod > 0)
			return Math.sqrt(prod);
		else
			return 0;*/
	}
	
	
	// SIMPLE OPERATIONS
	///////////////////////////////////////////////////////////////////////////
	

	
	public static double max(Matrix A) {
		double m = Double.NEGATIVE_INFINITY;
		for(int r=0; r<A.getRowDimension(); r++)
			for(int c=0; c<A.getColumnDimension(); c++)
				if(m < A.get(r,c))
					m = A.get(r,c);
		
		return m;
	}
	
	
	// COMPLEX CALCULATIONS
	///////////////////////////////////////////////////////////////////////////
	
	
	 /**
	  * The difference between 1 and the smallest exactly representable number
	  * greater than one. Gives an upper bound on the relative error due to
	  * rounding of floating point numbers.
	  */
	 public static double MACHEPS = 2E-16;

	 /**
	  * Updates MACHEPS for the executing machine.
	  */
	 public static void updateMacheps() {
	  MACHEPS = 1;
	  do
	   MACHEPS /= 2;
	  while (1 + MACHEPS / 2 != 1);
	 }
	
	
	 /**
	  * Computes the Moore–Penrose pseudoinverse using the SVD method.
	  * 
	  * Modified version of the original implementation by Kim van der Linde.
	  * 
	  * http://the-lost-beauty.blogspot.ch/2009/04/moore-penrose-pseudoinverse-in-jama.html
	  */
	 public static Matrix pinv(Matrix x) {
	  if (x.rank() < 1)
	   return null;
	  if (x.getColumnDimension() > x.getRowDimension())
	   return pinv(x.transpose()).transpose();
	  SingularValueDecomposition svdX = new SingularValueDecomposition(x);
	  double[] singularValues = svdX.getSingularValues();
	  double tol = Math.max(x.getColumnDimension(), x.getRowDimension()) * singularValues[0] * MACHEPS;
	  double[] singularValueReciprocals = new double[singularValues.length];
	  for (int i = 0; i < singularValues.length; i++)
	   singularValueReciprocals[i] = Math.abs(singularValues[i]) < tol ? 0 : (1.0 / singularValues[i]);
	  double[][] u = svdX.getU().getArray();
	  double[][] v = svdX.getV().getArray();
	  int min = Math.min(x.getColumnDimension(), u[0].length);
	  double[][] inverse = new double[x.getColumnDimension()][x.getRowDimension()];
	  for (int i = 0; i < x.getColumnDimension(); i++)
	   for (int j = 0; j < u.length; j++)
	    for (int k = 0; k < min; k++)
	     inverse[i][j] += v[i][k] * singularValueReciprocals[k] * u[j][k];
	  return new Matrix(inverse);
	 }
	
	
	
	
	// OUTPUT
	///////////////////////////////////////////////////////////////////////////

	
	public static String matrixToString(Matrix M) {
		String s = new String();
		
		for(int r=0; r<M.getRowDimension(); ++r) {
			for(int c=0; c<M.getColumnDimension(); ++c) {
				s += " "+Double.toString(M.get(r, c))+" ";
			}
			
			s += "\n";
		}
		
		return s;
	}
	
	
	public static String matrixToMatlabCode(Matrix M) {
		String s = new String();
		
		for(int r=0; r<M.getRowDimension(); ++r) {
			for(int c=0; c<M.getColumnDimension(); ++c) {
				s += " "+Double.toString(M.get(r, c))+" ";
			}
			
			if(r < M.getRowDimension()-1)
				s += ";";
		}
		
		return "["+s+"]";
	}
}
