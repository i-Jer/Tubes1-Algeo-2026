package algeo.interpolasi;

import algeo.matrix.Matrix;
import algeo.spl.SPLResult;
import algeo.spl.SPLSolver;

public class Interpolasi {

    public static double[] calculateCoefficient(double[] x, double[] y) {
        if (x == null || y == null) {
            throw new IllegalArgumentException("Data titik tidak boleh ada yang null");
        }
        if (x.length != y.length) {
            throw new IllegalArgumentException("Jumlah x dan y harus sama");
        }
        if (x.length == 0) {
            throw new IllegalArgumentException("Minimal terdapat satu titik");
        }

        int n = x.length;
        for (int i = 0 ; i < n - 1; i++){
            for(int j = i + 1 ; j < n ; j++){
                if(x[i] == x[j]){
                    throw new IllegalArgumentException("Nilai x tidak boleh ada yang sama");
                }
            }
        }

        // Membuat matrix A dan B untuk SPL interpolasi
        Matrix A = new Matrix(n, n);
        Matrix B = new Matrix(n, 1);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A.set(i, j, Math.pow(x[i], j));
            }
            B.set(i, 0, y[i]);
        }
        Matrix aug = Matrix.augment(A, B);

        // Menyelesaikan SPL menggunakan metode eliminasi Gauss-Jordan
        SPLResult result = SPLSolver.gaussJordan(aug, null);
        if (result.getType() != SPLResult.Type.UNIQUE) {
            throw new IllegalArgumentException("Interpolasi tidak memiliki solusi tunggal");
        }
        return result.getSolution();
    }

    public static double evaluator(double[] koefisien, double x) {
        double hasil = 0.0;
        for (int i = 0; i < koefisien.length; i++) {
            hasil += koefisien[i] * Math.pow(x, i);
        }
        return hasil;
    }
}