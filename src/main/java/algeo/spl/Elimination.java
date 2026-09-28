package algeo.spl;

import algeo.matrix.Matrix;
import java.util.Locale;

public class Elimination{

    public static double tolerance(Matrix m){
        double maxAbs = 0.0;
        for (int r = 0; r < m.getRows(); r++){
            for (int c = 0; c < m.getCols(); c++){
                maxAbs = Math.max(maxAbs, Math.abs(m.get(r, c)));
            }
        }
        return maxAbs * Math.max(m.getRows(), m.getCols()) * 1e-15;
    }

    public static int toRowEchelonForm(Matrix m){
        return toRowEchelonForm(m, null);
    }

    //ubah matriks menjadi matriks eselon baris dengan partial pivot
    //mengembalikan jumlah pertukaran baris (dipake determinan), steps = null kalo langkah tidak ditampilkan
    public static int toRowEchelonForm(Matrix m, StringBuilder steps){
        int rows = m.getRows();
        int cols = m.getCols();
        int pivotRow = 0;
        int swaps = 0;
        double tol = tolerance(m);

        for (int col = 0; col < cols && pivotRow < rows; col++){
            int maxRow = pivotRow;
            double maxValue = Math.abs(m.get(pivotRow, col));

            for (int r = pivotRow + 1; r < rows; r++){
                if (Math.abs(m.get(r, col)) > maxValue){
                    maxValue = Math.abs(m.get(r, col));
                    maxRow = r;
                }
            }

            if (maxValue <= tol){
                continue;
            }
            boolean changed = false;

            if (maxRow != pivotRow){
                m.swapRows(pivotRow, maxRow);
                swaps++;
                changed = true;
                if (steps != null) steps.append(String.format("R%d <-> R%d%n", pivotRow + 1, maxRow + 1));
            }

            for (int r = pivotRow + 1; r < rows; r++){
                double factor = -m.get(r, col) / m.get(pivotRow, col);
                if (factor == 0.0) continue;
                m.addRowMultiple(pivotRow, r, factor);
                m.set(r, col, 0.0);
                changed = true;
                if (steps != null) steps.append(String.format(Locale.US, "R%d = R%d + (%.3f) R%d%n", r + 1, r + 1, Matrix.round3(factor), pivotRow + 1));
            }
            if(steps != null && changed) steps.append(m).append('\n');

            pivotRow++;
        }
        return swaps;
    }

    public static void toReducedRowEchelonForm(Matrix m){
        toReducedRowEchelonForm(m, null);
    }

    //ubah matriks menjadi matriks eleson baris tereduksi dengan partial pivot
    public static void toReducedRowEchelonForm(Matrix m, StringBuilder steps){
        double tol = tolerance(m);
        toRowEchelonForm(m, steps);

        int rows = m.getRows();
        int cols = m.getCols();
        

        for (int r = rows - 1; r >= 0; r--){
            int pivotCol = -1;
            for (int c = 0; c < cols; c++) {
                if (Math.abs(m.get(r, c)) > tol){
                    pivotCol = c;
                    break;
                }
            }

            if (pivotCol != -1){
                double pivotVal = m.get(r, pivotCol);
                boolean changed = false;
                if(pivotVal != 1.0){
                    m.multiplyRow(r, 1.0 / pivotVal);
                    changed = true;
                    if (steps != null) steps.append(String.format(Locale.US, "R%d = R%d / (%.3f)%n", r + 1, r + 1, Matrix.round3(pivotVal)));
                }

                for (int aboveRow = r - 1; aboveRow >= 0; aboveRow--){
                    double factor = -m.get(aboveRow, pivotCol);
                    if (factor == 0.0) continue;
                    m.addRowMultiple(r, aboveRow, factor);
                    m.set(aboveRow, pivotCol, 0.0);
                    changed = true;
                    if (steps != null) steps.append(String.format(Locale.US, "R%d = R%d + (%.3f) R%d%n", aboveRow + 1, aboveRow + 1, Matrix.round3(factor), r + 1));
                }
                if(steps != null && changed) steps.append(m).append('\n');
            }
        }
    }
}