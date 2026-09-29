package algeo.regression;

import algeo.matrix.Matrix;
import algeo.spl.SPLResult;
import algeo.spl.SPLSolver;
import java.util.Arrays;

public class SplineRegression {
    public static final int MAX_POINTS = 10;

    private final double[] knots;
    private final double[] beta;

    public SplineRegression(double[] x, double[] y, double[] knotsInput, StringBuilder steps){
        if(x == null || y == null || knotsInput == null){
            throw new IllegalArgumentException("Data titik dan knot tidak boleh null");
        }
        if(x.length != y.length){
            throw new IllegalArgumentException("Jumlah x dan y harus sama");
        }
        if(x.length > MAX_POINTS){
            throw new IllegalArgumentException("Jumlah titik maksimal " + MAX_POINTS);
        }

        knots = knotsInput.clone();
        Arrays.sort(knots);
        for(int k = 0; k < knots.length - 1; k++){
            if(knots[k] == knots[k + 1]){
                throw new IllegalArgumentException("Posisi knot tidak boleh ada yang sama");
            }
        }

        int n = x.length;
        int basis = 4 + knots.length;
        if(n <= basis){
            throw new IllegalArgumentException("Jumlah titik (" + n + ") harus lebih banyak dari jumlah basis M = 4 + "
                + knots.length + " = " + basis);
        }

        Matrix xMat = new Matrix(n, basis);
        Matrix yMat = new Matrix(n, 1);
        for(int i = 0; i < n; i++){
            for(int j = 0; j < basis; j++){
                xMat.set(i, j, basisValue(x[i], j));
            }
            yMat.set(i, 0, y[i]);
        }

        Matrix xt = xMat.transpose();
        Matrix aug = Matrix.augment(xt.multiply(xMat), xt.multiply(yMat));
        if(steps != null){
            steps.append("Matriks desain X:\n").append(xMat).append('\n');
            steps.append("Persamaan normal [X^T X | X^T y]:\n").append(aug).append('\n');
            steps.append("(variabel x1..x").append(basis).append(" pada SPL = beta0..beta").append(basis - 1).append(")\n");
        }

        SPLResult res = SPLSolver.gauss(aug, steps);
        if(res.getType() != SPLResult.Type.UNIQUE){
            throw new ArithmeticException("X^T X singular atau hampir singular, koefisien tidak dapat ditentukan."
                + " Periksa posisi knot (harus di antara data) dan jumlah titik dengan nilai x berbeda");
        }
        beta = res.getSolution();
    }

    private double basisValue(double t, int j){
        if(j == 0) return 1.0;
        if(j == 1) return t;
        if(j == 2) return t * t;
        if(j == 3) return t * t * t;
        double u = t - knots[j - 4];
        return u > 0 ? u * u * u : 0.0;
    }

    public double[] getCoefficients(){ return beta.clone(); }
    public double[] getKnots(){ return knots.clone(); }

    public double predict(double t){
        double sum = 0.0;
        for(int j = 0; j < beta.length; j++){
            sum += beta[j] * basisValue(t, j);
        }
        return sum;
    }

    public String toEquation(){
        StringBuilder sb = new StringBuilder();
        for(int j = 0; j < beta.length; j++){
            double v = Matrix.round3(beta[j]);
            if(v == 0.0) continue;
            if(sb.length() == 0) sb.append(v < 0 ? "-" : "");
            else sb.append(v < 0 ? " - " : " + ");
            if(Math.abs(v) != 1.0 || j == 0) sb.append(Matrix.formatNumber(Math.abs(v)));
            if(j >= 1 && j <= 3){
                sb.append("x");
                if(j >= 2) sb.append("^").append(j);
            } else if(j >= 4){
                double k = knots[j - 4];
                sb.append("(x ").append(k < 0 ? "+ " : "- ").append(Matrix.formatNumber(Math.abs(k))).append(")^3_+");
            }
        }
        return "y(x) = " + (sb.length() == 0 ? "0" : sb.toString());
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("Posisi knot:");
        if(knots.length == 0) sb.append(" (tanpa knot)");
        for(double k : knots){
            sb.append(' ').append(Matrix.formatNumber(k));
        }
        sb.append("\nKoefisien regresi:\n");
        for(int j = 0; j < beta.length; j++){
            sb.append(String.format("beta%d = %s%n", j, Matrix.formatNumber(beta[j])));
        }
        sb.append(toEquation()).append('\n');
        return sb.toString();
    }
}