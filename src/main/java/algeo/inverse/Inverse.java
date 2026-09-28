package algeo.inverse;

import algeo.determinant.Determinant;
import algeo.matrix.Matrix;
import algeo.spl.Elimination;

public class Inverse {
    public static final int MAX_ADJOINT_SIZE = 100;

    // Metode augmentasi [A | I] dengan eliminasi Gauss-Jordan
    public static Matrix byGaussJordan(Matrix a, StringBuilder steps){
        requireSquare(a);
        requireNonSingular(a, steps);
        int n = a.getRows();

        Matrix aug = Matrix.augment(a, Matrix.identity(n));
        if(steps != null) steps.append("Matriks augmented [A | I]:\n").append(aug).append('\n');
        Elimination.toReducedRowEchelonForm(aug, steps);
        if(steps != null) steps.append("Bagian kiri sudah menjadi I, sehingga bagian kanan adalah A^-1\n");

        return aug.slice(0, n, n, 2 * n);
    }

    // Metode adjoin: A^-1 = (1 / det(A)) x adj(A)
    public static Matrix byAdjoint(Matrix a, StringBuilder steps){
        requireSquare(a);
        int n = a.getRows();
        if(n > MAX_ADJOINT_SIZE){
            throw new IllegalArgumentException("Metode adjoin hanya untuk matriks hingga "
                + MAX_ADJOINT_SIZE + "x" + MAX_ADJOINT_SIZE + ", gunakan metode Gauss-Jordan");
        }

        double det = Determinant.byRowReduction(a, null);
        if(steps != null) steps.append("det(A) = ").append(Matrix.formatNumber(det)).append('\n');
        if(det == 0.0){
            throw new ArithmeticException("Matriks tidak memiliki balikan karena determinannya 0");
        }

        // matriks 1x1: adj([a]) = [1]
        if(n == 1){
            Matrix res = new Matrix(1, 1);
            res.set(0, 0, 1.0 / det);
            return res;
        }

        Matrix cof = new Matrix(n, n);
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                double minor = Determinant.byRowReduction(a.submatrix(i, j), null);
                cof.set(i, j, ((i + j) % 2 == 0) ? minor : -minor);
            }
        }
        Matrix adj = cof.transpose();
        Matrix res = adj.multiply(1.0 / det);

        if(steps != null){
            steps.append("Matriks kofaktor C, dengan C_ij = (-1)^(i+j) x det(M_ij):\n").append(cof).append('\n');
            steps.append("adj(A) = transpose C:\n").append(adj).append('\n');
            steps.append("A^-1 = (1 / ").append(Matrix.formatNumber(det)).append(") x adj(A)\n");
        }
        return res;
    }

    private static void requireNonSingular(Matrix a, StringBuilder steps){
        Matrix m = a.copy();
        double tol = Elimination.tolerance(a);
        Elimination.toRowEchelonForm(m);
        for(int i = 0; i < m.getRows(); i++){
            if(Math.abs(m.get(i, i)) <= tol){
                if(steps != null) steps.append("Reduksi baris menghasilkan elemen diagonal 0, sehingga det(A) = 0\n");
                throw new ArithmeticException("Matriks tidak memiliki balikan karena determinannya 0");
            }
        }
    }

    private static void requireSquare(Matrix a){
        if(a.getRows() != a.getCols()){
            throw new IllegalArgumentException("Matriks tidak memiliki balikan karena bukan matriks persegi ("
                + a.getRows() + "x" + a.getCols() + ")");
        }
    }
}