package algeo.interpolasi;

import algeo.matrix.Matrix;
import algeo.spl.SPLResult;
import algeo.spl.SPLSolver;

public class Interpolasi {
    public static final int MAX_POINTS = 10;

    public static double[] calculateCoefficient(double[] x, double[] y, StringBuilder steps){
        if(x == null || y == null){
            throw new IllegalArgumentException("Data titik tidak boleh ada yang null");
        }
        if(x.length != y.length){
            throw new IllegalArgumentException("Jumlah x dan y harus sama");
        }
        if(x.length == 0){
            throw new IllegalArgumentException("Minimal terdapat satu titik");
        }
        if(x.length > MAX_POINTS){
            throw new IllegalArgumentException("Jumlah titik maksimal " + MAX_POINTS);
        }

        int n = x.length;
        for(int i = 0; i < n - 1; i++){
            for(int j = i + 1; j < n; j++){
                if(x[i] == x[j]){
                    throw new IllegalArgumentException("Nilai x tidak boleh ada yang sama");
                }
            }
        }

        Matrix aug = new Matrix(n, n + 1);
        for(int i = 0; i < n; i++){
            double p = 1.0;
            for(int j = 0; j < n; j++){
                aug.set(i, j, p);
                p *= x[i];
            }
            aug.set(i, n, y[i]);
        }
        if(steps != null){
            steps.append("SPL [1 x x^2 ... | y]:\n").append(aug).append('\n');
            steps.append("(variabel x1..x").append(n).append(" pada SPL = a0..a").append(n - 1).append(")\n");
        }

        SPLResult result = SPLSolver.gauss(aug, steps);
        if(result.getType() != SPLResult.Type.UNIQUE){
            throw new IllegalArgumentException("Interpolasi tidak memiliki solusi tunggal");
        }
        return result.getSolution();
    }

    public static double evaluator(double[] koefisien, double x){
        double hasil = 0.0;
        for(int i = koefisien.length - 1; i >= 0; i--){
            hasil = hasil * x + koefisien[i];
        }
        return hasil;
    }

    public static String toEquation(double[] koefisien){
        StringBuilder sb = new StringBuilder("P(x) = ");
        boolean first = true;
        for(int i = 0; i < koefisien.length; i++){
            double c = Matrix.round3(koefisien[i]);
            if(c == 0.0) continue;
            if(first) sb.append(c < 0 ? "-" : "");
            else sb.append(c < 0 ? " - " : " + ");
            if(Math.abs(c) != 1.0 || i == 0) sb.append(Matrix.formatNumber(Math.abs(c)));
            if(i >= 1) sb.append("x");
            if(i >= 2) sb.append("^").append(i);
            first = false;
        }
        if(first) sb.append("0");
        return sb.toString();
    }
}