package algeo.determinant;

import algeo.matrix.Matrix;
import algeo.spl.Elimination;

public class Determinant {
    public static final int MAX_COFACTOR_SIZE = 11;

    // Metode OBE: ubah ke matriks segitiga atas
    public static double byRowReduction(Matrix a, StringBuilder steps){
        requireSquare(a);
        Matrix m = a.copy();
        double tol = Elimination.tolerance(a);

        if(steps != null) steps.append("Reduksi baris menjadi matriks segitiga atas:\n");
        int swaps = Elimination.toRowEchelonForm(m, steps);

        double det = (swaps % 2 == 0) ? 1.0 : -1.0;
        StringBuilder diag = new StringBuilder();
        for(int i = 0; i < m.getRows(); i++){
            if(Math.abs(m.get(i, i)) <= tol){
                det = 0.0;
                break;
            }
            det *= m.get(i, i);
            if(i > 0) diag.append(" x ");
            diag.append("(").append(Matrix.formatNumber(m.get(i, i))).append(")");
        }
        if(Double.isInfinite(det)){
            throw new ArithmeticException("Nilai determinan terlalu besar (melebihi batas tipe double)");
        }

        if(steps != null){
            steps.append("Penjumlahan kelipatan baris tidak mengubah determinan,\n");
            steps.append("setiap pertukaran baris mengalikan determinan dengan -1.\n");
            steps.append("Jumlah pertukaran baris = ").append(swaps).append('\n');
            if(det == 0.0){
                steps.append("Terdapat elemen diagonal bernilai 0, sehingga det = 0\n");
            } else {
                steps.append(String.format("det = (-1)^%d x %s = %s%n", swaps, diag, Matrix.formatNumber(det)));
            }
        }
        return det;
    }

    // Metode ekspansi kofaktor dengan nol terbanyak
    public static double byCofactorExpansion(Matrix a, StringBuilder steps){
        requireSquare(a);
        if(a.getRows() > MAX_COFACTOR_SIZE){
            throw new IllegalArgumentException("Ekspansi kofaktor hanya untuk matriks hingga "
                + MAX_COFACTOR_SIZE + "x" + MAX_COFACTOR_SIZE + ", gunakan metode reduksi baris");
        }
        return cofactor(a, steps);
    }

    private static double cofactor(Matrix m, StringBuilder steps){
        int n = m.getRows();
        if(n == 1) return m.get(0, 0);
        if(n == 2) return m.get(0, 0) * m.get(1, 1) - m.get(0, 1) * m.get(1, 0);

        int[] line = bestLine(m);
        boolean byRow = line[0] == 1;
        int k = line[1];
        if(steps != null) steps.append(String.format("Ekspansi kofaktor sepanjang %s %d:%n", byRow ? "baris" : "kolom", k + 1));

        double det = 0.0;
        for(int j = 0; j < n; j++){
            int r = byRow ? k : j;
            int c = byRow ? j : k;
            double a = m.get(r, c);
            if(a == 0.0) continue;
            double sign = ((r + c) % 2 == 0) ? 1.0 : -1.0;
            double minor = cofactor(m.submatrix(r, c), null);
            det += a * sign * minor;
            if(steps != null){
                steps.append(String.format("a%d%d = %s, M%d%d = %s, C%d%d = (-1)^%d x M%d%d = %s%n",
                    r + 1, c + 1, Matrix.formatNumber(a),
                    r + 1, c + 1, Matrix.formatNumber(minor),
                    r + 1, c + 1, r + c + 2, r + 1, c + 1, Matrix.formatNumber(sign * minor))
                );
            }
        }
        if(steps != null) steps.append("det = jumlah a x C = ").append(Matrix.formatNumber(det)).append('\n');
        return det;
    }

    private static int[] bestLine(Matrix m){
        int n = m.getRows();
        int[] best = {1, 0};
        int bestZeros = -1;
        for(int i = 0; i < n; i++){
            int rowZeros = 0;
            int colZeros = 0;
            for(int j = 0; j < n; j++){
                if(m.get(i, j) == 0.0) rowZeros++;
                if(m.get(j, i) == 0.0) colZeros++;
            }
            if(rowZeros > bestZeros){
                bestZeros = rowZeros;
                best = new int[]{1, i};
            }
            if(colZeros > bestZeros){
                bestZeros = colZeros;
                best = new int[]{0, i};
            }
        }
        return best;
    }

    private static void requireSquare(Matrix a){
        if(a.getRows() != a.getCols()){
            throw new IllegalArgumentException("Matriks tidak memiliki determinan karena bukan matriks persegi ("
                + a.getRows() + "x" + a.getCols() + ")");
        }
    }
}