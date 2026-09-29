package algeo.spl;

import algeo.determinant.Determinant;
import algeo.inverse.Inverse;
import algeo.matrix.Matrix;

public class SPLSolver {
    public static final int MAX_CRAMER_SIZE = 100;

    // Metode eliminasi Gauss
    public static SPLResult gauss(Matrix aug, StringBuilder steps){
        Matrix m = aug.copy();
        if(steps != null) steps.append("Eliminasi Gauss (menuju matriks eselon baris):\n");
        Elimination.toRowEchelonForm(m, steps);
        return backSubstitution(m, Elimination.tolerance(aug), steps);
    }

    // Metode eliminasi Gauss-Jordan
    public static SPLResult gaussJordan(Matrix aug, StringBuilder steps){
        Matrix m = aug.copy();
        if(steps != null) steps.append("Eliminasi Gauss-Jordan (menuju matriks eselon baris tereduksi):\n");
        Elimination.toReducedRowEchelonForm(m, steps);
        return backSubstitution(m, Elimination.tolerance(aug), steps);
    }

    // substitusi mundur matriks eselon
    private static SPLResult backSubstitution(Matrix m, double tol, StringBuilder steps){
        int rows = m.getRows();
        int n = m.getCols() - 1;
        if(n < 1){
            throw new IllegalArgumentException("Matriks augmented minimal memiliki 1 kolom koefisien dan 1 kolom konstanta");
        }

        int[] lead = new int[rows];
        boolean[] isPivot = new boolean[n];
        for(int r = 0; r < rows; r++){
            lead[r] = -1;
            for(int c = 0; c <= n; c++){
                if(Math.abs(m.get(r, c)) > tol){
                    lead[r] = c;
                    break;
                }
            }
            if(lead[r] == n){
                if(steps != null) steps.append(String.format("Baris %d berbentuk 0 = %s, SPL tidak konsisten%n", r + 1, Matrix.formatNumber(m.get(r, n))));
                return new SPLResult(SPLResult.Type.NONE, null);
            }
            if(lead[r] != -1) isPivot[lead[r]] = true;
        }

        int numFree = 0;
        for(int c = 0; c < n; c++){
            if(!isPivot[c]) numFree++;
        }
        double[][] expr = new double[n][numFree + 1];
        int k = 0;
        for(int c = 0; c < n; c++){
            if(!isPivot[c]){
                k++;
                expr[c][k] = 1.0;
            }
        }

        if(steps != null) steps.append("Substitusi mundur:\n");
        for(int r = rows - 1; r >= 0; r--){
            int p = lead[r];
            if(p == -1) continue;
            double[] e = expr[p];
            e[0] = m.get(r, n);
            for(int j = p + 1; j < n; j++){
                double a = m.get(r, j);
                if(a == 0.0) continue;
                for(int t = 0; t <= numFree; t++){
                    e[t] -= a * expr[j][t];
                }
            }
            double pivot = m.get(r, p);
            for(int t = 0; t <= numFree; t++){
                e[t] /= pivot;
            }
            if(steps != null) steps.append("x").append(p + 1).append(" = ").append(SPLResult.formatExpr(e)).append('\n');
        }

        SPLResult.Type type = numFree == 0 ? SPLResult.Type.UNIQUE : SPLResult.Type.INFINITE;
        return new SPLResult(type, expr);
    }
    
    // Metode matriks balikan
    public static SPLResult inverseMethod(Matrix aug, StringBuilder steps){
        requireSquareSystem(aug, "Metode matriks balikan");
        int n = aug.getRows();
        Matrix a = aug.slice(0, n, 0, n);
        Matrix b = aug.slice(0, n, n, n + 1);

        Matrix inv;
        try{
            inv = Inverse.byGaussJordan(a, steps);
        } catch(ArithmeticException e){
            throw new ArithmeticException("Metode matriks balikan tidak dapat digunakan: " + e.getMessage()
                + ". Gunakan metode Gauss atau Gauss-Jordan");
        }
        Matrix x = inv.multiply(b);
        if(steps != null){
            steps.append("A^-1:\n").append(inv).append('\n');
            steps.append("x = A^-1 b:\n").append(x).append('\n');
        }

        double[][] expr = new double[n][1];
        for(int i = 0; i < n; i++){
            expr[i][0] = x.get(i, 0);
        }
        return new SPLResult(SPLResult.Type.UNIQUE, expr);
    }

    // Kaidah Cramer
    public static SPLResult cramer(Matrix aug, StringBuilder steps){
        requireSquareSystem(aug, "Kaidah Cramer");
        int n = aug.getRows();
        if(n > MAX_CRAMER_SIZE){
            throw new IllegalArgumentException("Kaidah Cramer hanya untuk SPL hingga " + MAX_CRAMER_SIZE
                + " variabel, gunakan metode Gauss atau Gauss-Jordan");
        }
        Matrix a = aug.slice(0, n, 0, n);

        double det = Determinant.byRowReduction(a, null);
        if(steps != null) steps.append("det(A) = ").append(Matrix.formatNumber(det)).append('\n');
        if(det == 0.0){
            throw new ArithmeticException("Kaidah Cramer tidak dapat digunakan karena det(A) = 0."
                + " Gunakan metode Gauss atau Gauss-Jordan");
        }

        double[][] expr = new double[n][1];
        for(int i = 0; i < n; i++){
            Matrix ai = a.copy();
            for(int r = 0; r < n; r++){
                ai.set(r, i, aug.get(r, n));
            }
            double deti = Determinant.byRowReduction(ai, null);
            expr[i][0] = deti / det;
            if(steps != null){
                steps.append(String.format("A%d (kolom %d diganti b):%n", i + 1, i + 1)).append(ai);
                steps.append(String.format("det(A%d) = %s, x%d = %s / %s = %s%n%n", i + 1, Matrix.formatNumber(deti),
                    i + 1, Matrix.formatNumber(deti), Matrix.formatNumber(det), Matrix.formatNumber(expr[i][0])));
            }
        }
        return new SPLResult(SPLResult.Type.UNIQUE, expr);
    }

    //metode balikan dan Cramer hanya untuk SPL dengan jumlah persamaan = jumlah variabel
    private static void requireSquareSystem(Matrix aug, String method){
        int n = aug.getCols() - 1;
        if(n < 1 || aug.getRows() != n){
            throw new IllegalArgumentException(method + " tidak dapat digunakan karena jumlah persamaan ("
                + aug.getRows() + ") tidak sama dengan jumlah variabel (" + n + "). Gunakan metode Gauss atau Gauss-Jordan");
        }
    }
}