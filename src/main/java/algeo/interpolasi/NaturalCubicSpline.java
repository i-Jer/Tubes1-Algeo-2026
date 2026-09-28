package algeo.interpolasi;

import algeo.matrix.Matrix;
import algeo.spl.SPLResult;
import algeo.spl.SPLSolver;

public class NaturalCubicSpline {
    public static final int MAX_POINTS = 10;

    private final double[] x;
    private final double[] y;
    private final double[] m;
    private final double[] a, b, c, d;

    public NaturalCubicSpline(double[] xs, double[] ys, StringBuilder steps){
        if(xs == null || ys == null){
            throw new IllegalArgumentException("Data titik tidak boleh ada yang null");
        }
        if(xs.length != ys.length){
            throw new IllegalArgumentException("Jumlah x dan y harus sama");
        }
        if(xs.length < 2){
            throw new IllegalArgumentException("Spline membutuhkan minimal 2 titik");
        }
        if(xs.length > MAX_POINTS){
            throw new IllegalArgumentException("Jumlah titik maksimal " + MAX_POINTS);
        }

        int n = xs.length;
        x = xs.clone();
        y = ys.clone();
        for(int i = 1; i < n; i++){
            double kx = x[i], ky = y[i];
            int j = i-1;
            while(j >= 0 && x[j] > kx){
                x[j+1] = x[j];
                y[j+1] = y[j];
                j--;
            }
            x[j+1] = kx;
            y[j+1] = ky;
        }
        for(int i = 0; i < n-1; i++){
            if(x[i] == x[i+1]){
                throw new IllegalArgumentException("Nilai x tidak boleh ada yang sama");
            }
        }

        double[] h = new double[n-1];
        for(int j = 0; j < n-1; j++){
            h[j] = x[j+1] - x[j];
        }
        if(steps != null){
            steps.append("Lebar interval h_j = x_(j+1) - x_j:\n");
            for(int j = 0; j < n-1; j++){
                steps.append(String.format("h%d = %s%n", j, Matrix.formatNumber(h[j])));
            }
        }

        m = new double[n];
        if(n > 2){
            int k = n - 2;
            Matrix aug = new Matrix(k, k + 1);
            for(int i = 1; i <= k; i++){
                int r = i-1;
                if(i > 1) aug.set(r, r - 1, h[i-1]);
                aug.set(r, r, 2 * (h[i-1] + h[i]));
                if(i < k) aug.set(r, r + 1, h[i]);
                aug.set(r, k, 6 * ((y[i+1] - y[i]) / h[i] - (y[i] - y[i-1]) / h[i-1]));
            }
            if(steps != null){
                steps.append("SPL tridiagonal untuk M1..M").append(k).append(" (M0 = M").append(n-1).append(" = 0):\n");
                steps.append(aug).append('\n');
            }

            SPLResult res = SPLSolver.gauss(aug, steps);
            if(res.getType() != SPLResult.Type.UNIQUE){
                throw new ArithmeticException("SPL tridiagonal tidak memiliki solusi tunggal");
            }
            double[] sol = res.getSolution();
            for(int i = 1; i <= k; i++){
                m[i] = sol[i-1];
            }
        } else if(steps != null){
            steps.append("Hanya 2 titik, tidak ada knot dalam sehingga M0 = M1 = 0\n");
        }

        a = new double[n-1];
        b = new double[n-1];
        c = new double[n-1];
        d = new double[n-1];
        for(int j = 0; j < n-1; j++){
            a[j] = y[j];
            b[j] = (y[j+1] - y[j]) / h[j] - h[j] * (2 * m[j] + m[j+1]) / 6;
            c[j] = m[j] / 2;
            d[j] = (m[j+1] - m[j]) / (6 * h[j]);
        }
    }

    public double[] getSecondDerivatives(){ return m.clone(); }
    public double getDomainMin(){ return x[0]; }
    public double getDomainMax(){ return x[x.length - 1]; }

    public double evaluate(double t){
        if(t < x[0] || t > x[x.length - 1]){
            throw new IllegalArgumentException("x = " + Matrix.formatNumber(t) + " di luar domain ["
                + Matrix.formatNumber(x[0]) + ", " + Matrix.formatNumber(x[x.length - 1]) + "]");
        }
        int j = 0;
        while(j < a.length - 1 && t > x[j+1]){
            j++;
        }
        double u = t - x[j];
        return a[j] + u * (b[j] + u * (c[j] + u * d[j]));
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("Turunan kedua di setiap knot:\n");
        for(int i = 0; i < m.length; i++){
            sb.append(String.format("M%d = %s%n", i, Matrix.formatNumber(m[i])));
        }
        sb.append("Persamaan setiap segmen:\n");
        for(int j = 0; j < a.length; j++){
            sb.append(String.format("S%d(x) = %s, untuk %s <= x <= %s%n", j, segmentText(j),
                Matrix.formatNumber(x[j]), Matrix.formatNumber(x[j+1])));
        }
        return sb.toString();
    }

    private String segmentText(int j){
        String base = Matrix.round3(x[j]) == 0.0 ? "x"
            : "(x " + (x[j] < 0 ? "+ " : "- ") + Matrix.formatNumber(Math.abs(x[j])) + ")";
        double[] coef = {a[j], b[j], c[j], d[j]};
        StringBuilder sb = new StringBuilder();
        for(int p = 0; p < 4; p++){
            double v = Matrix.round3(coef[p]);
            if(v == 0.0) continue;
            if(sb.length() == 0) sb.append(v < 0 ? "-" : "");
            else sb.append(v < 0 ? " - " : " + ");
            if(Math.abs(v) != 1.0 || p == 0) sb.append(Matrix.formatNumber(Math.abs(v)));
            if(p >= 1) sb.append(base);
            if(p >= 2) sb.append("^").append(p);
        }
        return sb.length() == 0 ? "0" : sb.toString();
    }
}