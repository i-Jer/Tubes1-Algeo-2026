package algeo.spl;

import algeo.matrix.Matrix;

public class Elimination{
    private static final double EPSILON = 1e-9;

    //ubah matriks menjadi matriks eselon baris dengan partial pivot
    public static void toRowEchelonForm(Matrix m){
        int rows = m.getRows();
        int cols = m.getCols();
        int pivotRow = 0;

        for (int col = 0; col < cols && pivotRow < rows; col++){
            int maxRow = pivotRow;
            double maxValue = Math.abs(m.get(pivotRow, col));

            for (int r = pivotRow + 1; r < rows; r++){
                if (Math.abs(m.get(r, col)) > maxValue){
                    maxValue = Math.abs(m.get(r, col));
                    maxRow = r;
                }
            }

            if (maxValue < EPSILON){
                continue;
            }

            m.swapRows(pivotRow, maxRow);

            for (int r = pivotRow + 1; r < rows; r++){
                double factor = -m.get(r, col) / m.get(pivotRow, col);
                m.addRowMultiple(pivotRow, r, factor);
                m.set(r, col, 0.0);
            }

            pivotRow++;
        }
    }

    //ubah matriks menjadi matriks eleson baris tereduksi dengan partial pivot
    public static void toReducedRowEchelonForm(Matrix m){
        toRowEchelonForm(m);

        int rows = m.getRows();
        int cols = m.getCols();

        for (int r = rows - 1; r >= 0; r--){
            int pivotCol = -1;
            for (int c = 0; c < cols; c++) {
                if (Math.abs(m.get(r, c)) > EPSILON){
                    pivotCol = c;
                    break;
                }
            }

            if (pivotCol != -1){
                double pivotVal = m.get(r, pivotCol);
                m.multiplyRow(r, 1.0 / pivotVal);

                for (int aboveRow = r - 1; aboveRow >= 0; aboveRow--){
                    double factor = -m.get(aboveRow, pivotCol);
                    m.addRowMultiple(r, aboveRow, factor);
                    m.set(aboveRow, pivotCol, 0.0);
                }
            }
        }
    }
}