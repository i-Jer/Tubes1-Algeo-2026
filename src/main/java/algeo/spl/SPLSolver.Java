package algeo.spl;

import algeo.matrix.Matrix;

public class SPLSolver {
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
}