package ch.ethz.inf.vs.android.fbuenzli.antitheft;

import java.util.List;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import android.util.Log;

public class MathHelpers {
	
	public static final double EPS = Double.MIN_VALUE;
	
	// STATISTICAL OPERATIONS
	///////////////////////////////////////////////////////////////////////////
	
	public static Vector3 calculate_mean(List<Vector3> data) {
		
		if(data.size() == 0)
			return new Vector3(0,0,0);
		
		Vector3 mean = new Vector3(0,0,0);
		int count = 0;
		
		for(Vector3 v: data)
		{
			mean.x += v.x;
			mean.y += v.y;
			mean.z += v.z;
			
			count++;
		}
		
		mean.x /= count;
		mean.y /= count;
		mean.z /= count;
		
		return mean;
	}
	
	public static double[][] calculate_covariance(List<Vector3> data, Vector3 mean) {
		
		double[][] cov = new double[3][3];
		
		// calculate covariances (I long for matrices!)
		for(Vector3 v: data)
		{
			for(int i=0; i<3; i++)
				for(int j=0; j<3; j++)
					cov[i][j] = (v.get(i) - mean.get(i)) * (v.get(j) - mean.get(j));
		}
		
		for(int i=0; i<3; i++)
			for(int j=0; j<3; j++)
				cov[i][j] /= (data.size()-1); 
		
		return cov;
	}
	

	
	public static double mahalanobis3(double[][] inv_cov, Vector3 mean, Vector3 v) {
		Vector3 d = new Vector3(v.x - mean.x,
								v.y - mean.y,
								v.z - mean.z);
		Vector3 e = mult3(inv_cov, d);
		
		double prod = d.scalarmult(e);
		
		if(prod > 0)
			return Math.sqrt(prod);
		else
			return 0;
	}
	
	public static double mahalanobis3(Matrix inv_cov, Matrix mean, Matrix v) {
		Matrix d = v.minus(mean);
		
		Matrix r = d.transpose().times(inv_cov.times(d));
		return r.get(0, 0);
		
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
	
	public static double[][] transpose3(double[][] M) {
		double[][] Mt = new double[3][3];
		
		for(int r=0; r<3; ++r)
			for(int c=0; c<3; ++c)
				Mt[c][r] = M[r][c];
		
		return Mt;
	}
	
	// matrix * matrix
	public static double[][] mult3(double[][] A, double [][] B) {
		double[][] R = new double[3][3];
		
		for(int r=0; r<3; ++r)
			for(int c=0; c<3; ++c)
				for(int i=0; i<3; ++i)
					R[r][c] += A[r][i]*B[i][c];
		
		return R;
	}
	
	// matrix + matrix
	public static double[][] plus3(double[][] A, double [][] B) {
		double[][] R = new double[3][3];
		
		for(int r=0; r<3; ++r)
			for(int c=0; c<3; ++c)
					R[r][c] = A[r][c]+B[r][c];
		
		return R;
	}
	
	// matrix - matrix
	public static double[][] minus3(double[][] A, double [][] B) {
		double[][] R = new double[3][3];
		
		for(int r=0; r<3; ++r)
			for(int c=0; c<3; ++c)
					R[r][c] = A[r][c]-B[r][c];
		
		return R;
	}
	
	// matrix * scalar
	public static double[][] mult3(double[][] A, double v) {
		double[][] R = new double[3][3];
		
		for(int r=0; r<3; ++r)
			for(int c=0; c<3; ++c)
				R[r][c] = A[r][c]*v;
		
		return R;
	}
	
	// matrix * vector
	public static Vector3 mult3(double[][] A, Vector3 v) {
		return new Vector3(A[0][0]*v.x + A[0][1]*v.y + A[0][2]*v.z,
						   A[1][0]*v.x + A[1][1]*v.y + A[1][2]*v.z,
						   A[2][0]*v.x + A[2][1]*v.y + A[2][2]*v.z);
	}
	
	// vector * matrix
	public static Vector3 mult3(Vector3 v, double[][] A) {
		return new Vector3(A[0][0]*v.x + A[1][0]*v.y + A[2][0]*v.z,
						   A[0][1]*v.x + A[1][1]*v.y + A[2][1]*v.z,
						   A[0][2]*v.x + A[1][2]*v.y + A[2][2]*v.z);
	}
	
	
	public static double[][] eye3() {
		double[][] I = new double[3][3];
		
		I[0][0] = 1;
		I[1][1] = 1;
		I[2][2] = 1;
		
		return I;
	}
	
	public static double max(double[][] A) {
		double m = Double.NEGATIVE_INFINITY;
		for(int r=0; r<3; r++)
			for(int c=0; c<3; c++)
				if(m < A[r][c])
					m = A[r][c];
		
		return m;
	}
	
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
	
	
	public static double[][] invert3(double[][] M) {
		
		double[][] X = new double[3][3];
		
		// correct inverse
		double det = M[0][0]*(M[1][1]*M[2][2]-M[1][2]*M[2][1])+M[0][1]*(M[1][2]*M[2][0]-M[1][0]*M[2][2])+M[0][2]*(M[1][0]*M[2][1]-M[1][1]*M[2][0]);
		
		// pseudoinverse (moore penrose)
		// doesn't work as espected
		//double det = (M[0][0]*M[1][1]-M[0][1]*M[1][0])*M[2][2]+(M[0][2]*M[1][0]-M[0][0]*M[1][2])*M[2][1]+(M[0][1]*M[1][2]-M[0][2]*M[1][1])*M[2][0];
		
		X[0][0] = M[1][1]*M[2][2]-M[1][2]*M[2][1];   X[0][1] = M[0][2]*M[2][1]-M[0][1]*M[2][2];   X[0][2] = M[0][1]*M[1][2]-M[0][2]*M[1][1];
		X[1][0] = M[1][2]*M[2][0]-M[1][0]*M[2][2];   X[1][1] = M[0][0]*M[2][2]-M[0][2]*M[2][0];   X[1][2] = M[0][2]*M[1][0]-M[0][0]*M[1][2];
		X[2][0] = M[1][0]*M[2][1]-M[1][1]*M[2][0];   X[2][1] = M[0][1]*M[2][0]-M[0][0]*M[2][1];   X[2][2] = M[0][0]*M[1][1]-M[0][1]*M[1][0];
		
		for(int r=0; r<3; ++r)
			for(int c=0; c<3; ++c)
				X[r][c] /= det;
		
		return X;
	}
	
	
	public static Matrix pseudo_invert3(Matrix A) {
		/*double[][][] svd = SVD3(A);
		double[][] U = svd[0];
		double[][] S = svd[1];
		double[][] V = svd[2];
		final double m = max(S);
		
		// invert S
		for(int i=0; i<3; i++)
			if(Math.abs(S[i][i]) < EPS*1024*m*3)
				S[i][i] = 0;
			else
				S[i][i] = 1/S[i][i];
		
		return mult3(mult3(V, S), transpose3(U));*/
		
		
		
		SingularValueDecomposition svd = new SingularValueDecomposition(A);
		Matrix S = svd.getS();
		
		final double m = max(S);
		
		// invert S
		for(int r=0; r<S.getRowDimension(); ++r)
			if(Math.abs(S.get(r, r)) < EPS*m*3)
				S.set(r, r, 0);
			else
				S.set(r, r, 1/S.get(r, r));
		
		return svd.getV().times(S).times(svd.getU().transpose());
	}
	
	
	
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
	
	
	public static double[][][] QR3(double[][] A) {
		
		// QR decomposition with Gram-Schmidt
		
		
		// columns of A
		Vector3 a1 = new Vector3(A[0][0], A[1][0], A[2][0]);
		Vector3 a2 = new Vector3(A[0][1], A[1][1], A[2][1]);
		Vector3 a3 = new Vector3(A[0][2], A[1][2], A[2][2]);
		
		
		
		
		Vector3 u1 = a1.clone();
		Vector3 e1 = u1.clone();
		e1.normalize();
		
		Vector3 u2 = a2.minus(a2.proj(e1));
		Vector3 e2 = u2.clone();
		e2.normalize();
		
		Vector3 u3 = a3.minus(a3.proj(e1));
				//u3 = u3.minus(u3.proj(e2)); // modified Gram-Schmidt
				u3 = u3.minus(a3.proj(e2));
		Vector3 e3 = u3.clone();
		e3.normalize();
		
		
		
		
		
		double[][][] r = new double[2][3][3];
		
		// Q
		r[0][0][0] = e1.x;  r[0][0][1] = e2.x;  r[0][0][2] = e3.x;
		r[0][1][0] = e1.y;  r[0][1][1] = e2.y;  r[0][1][2] = e3.y;
		r[0][2][0] = e1.z;  r[0][2][1] = e2.z;  r[0][2][2] = e3.z;
		
		// R
		r[1][0][0] = e1.scalarmult(a1);  r[1][0][1] = e1.scalarmult(a2);  r[1][0][2] = e1.scalarmult(a3);
		r[1][1][0] =                 0;  r[1][1][1] = e2.scalarmult(a2);  r[1][1][2] = e2.scalarmult(a3);
		r[1][2][0] =                 0;  r[1][2][1] =                 0;  r[1][2][2] = e3.scalarmult(a3);
		
		return r; 
	}
	
	public static double[][][] SVD3(double[][] A) {
		double[][] U = eye3();
		double[][] S = transpose3(A);
		double[][] V = eye3();
		
		// change to your needs:
		final double tol = EPS*1024;
		final int maxiters = 500;
		
		double err = 10000000;
		int iters = 0;
		while(err > tol && iters < maxiters) {
			
			double[][][] qr = QR3(transpose3(S)); U = mult3(U, qr[0]); S = qr[1];
			             qr = QR3(transpose3(S)); V = mult3(V, qr[0]); S = qr[1];
			
			double f = Math.sqrt(S[0][0]*S[0][0] + S[1][1]*S[1][1] + S[2][2]*S[2][2]); if(f==0) break;
			     err = Math.sqrt(S[0][1]*S[0][1] + S[1][2]*S[1][2]) / f;
			
			iters++;
		}
		Log.d("foo", "SVD did "+Integer.toString(iters)+" iterations");
		
		
		// correct signs (make every singular value positive)
		// not really necessary...
		/*for(int i=0; i<3; i++) {
			if(S[i][i] < 0) {
				S[i][i] = -S[i][i];
				
				for(int j=0; j<3; j++)
					U[j][i] = -U[j][i];
			}
		}*/
		
		
		// make sure, S is really tridiagonal
		for(int r=0; r<3; r++)
			for(int c=0; c<3; c++)
				if(r!=c)
					S[r][c] = 0;
		
		
		double[][][] r = new double[3][][];
		r[0] = U;
		r[1] = S;
		r[2] = V;
		return r; 
	}
	
	
	
	
	
	// OUTPUT
	///////////////////////////////////////////////////////////////////////////
	
	public static String matrixToString3(double[][] M) {
		String s = new String();
		
		for(int r=0; r<3; ++r) {
			for(int c=0; c<3; ++c) {
				s += " "+Double.toString(M[r][c])+" ";
			}
			
			s += "\n";
		}
		
		return s;
	}
	
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
	
	
	public static String matrixToMatlabCode3(double[][] M) {
		String s = new String();
		
		for(int r=0; r<3; ++r) {
			for(int c=0; c<3; ++c) {
				s += " "+Double.toString(M[r][c])+" ";
			}
			
			s += ";";
		}
		
		return "["+s+"]";
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
