package algeo.matrix;

import java.util.Locale;

public class Matrix {
    private final int rows;
    private final int cols;
    private final double[][] data;

    //Konstruktor
    public Matrix(int rows, int cols){
        if (rows <= 0 || cols <= 0){
            throw new IllegalArgumentException("Ukuran matriks harus lebih besar dari 0");
        }
        this.rows = rows;
        this.cols = cols;
        this.data = new double[rows][cols];
    }

    public Matrix(double[][] source){
        if (source == null || source.length == 0){
            throw new IllegalArgumentException("Matriks harus memiliki minimal 1 baris");
        }
        if (source[0] == null || source[0].length == 0){
            throw new IllegalArgumentException("Matriks harus memiliki minimal 1 kolom");
        }
        this.rows = source.length;
        this.cols = source[0].length;
        this.data = new double[rows][cols];
        for (int i = 0; i < rows; i++){
            //jumlah kolom tiap baris harus sama
            if (source[i] == null || source[i].length != cols){
                int found = source[i] == null ? 0 : source[i].length;
                throw new IllegalArgumentException(String.format(
                    "Baris ke-%d memiliki %d kolom, seharusnya %d kolom", i + 1, found, cols));
            }
            System.arraycopy(source[i], 0, this.data[i], 0, cols);
        }
    }

    public int getRows(){ return rows; }
    public int getCols(){ return cols; }

    public double get(int r, int c){ return data[r][c]; }
    public void set(int r, int c, double value) { this.data[r][c] = value; }

    //Operasi Baris Elementer (OBE)
    public void swapRows(int r1, int r2){
        if (r1 == r2) return;
        double[] temp = data[r1];
        data[r1] = data[r2];
        data[r2] = temp;
    }

    public void multiplyRow(int r, double factor){
        for (int c = 0; c < cols; c++){
            data[r][c] *= factor;
        }
    }

    public void addRowMultiple(int sourceRow, int targetRow, double factor){
        addRowMultiple(sourceRow, targetRow, factor, 0);
    }

    public void addRowMultiple(int sourceRow, int targetRow, double factor, int startCol){
        if(factor == 0.0) return;
        double[] src = data[sourceRow];
        double[] dst = data[targetRow];
        for(int c = startCol; c < cols; c++) dst[c] += factor * src[c];
    }

    //dibawah ini operasi aljabar matriks

    //Penjumlahan matriks A + B
    public Matrix add(Matrix other){
        requireSameSize(other, "penjumlahan");
        Matrix res = new Matrix(rows, cols);
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++){
                res.data[i][j] = data[i][j] + other.data[i][j];
            }
        }
        return res;
    }

    //Pengurangan matriks A - B
    public Matrix subtract(Matrix other){
        requireSameSize(other, "pengurangan");
        Matrix res = new Matrix(rows, cols);
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++){
                res.data[i][j] = data[i][j] - other.data[i][j];
            }
        }
        return res;
    }

    private void requireSameSize(Matrix other, String operation){
        if(this.rows != other.rows || this.cols != other.cols){
            throw new IllegalArgumentException(String.format(
                "Dimensi tidak cocok untuk %s matriks (%dx%d dan %dx%d)",
                operation, rows, cols, other.rows, other.cols));
        }
    }

    //Perkalian skalar k * A
    public Matrix multiply(double scalar){
        Matrix res = new Matrix(rows, cols);
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++){
                res.data[i][j] = scalar * data[i][j];
            }
        }
        return res;
    }
    
    //Perkalian matriks A dan B
    public Matrix multiply(Matrix other) {
        if (this.cols != other.rows){
            throw new IllegalArgumentException("Dimensi tidak cocok untuk perkalian matriks.");
        }
        Matrix res = new Matrix(this.rows, other.cols);
        for (int i = 0; i < this.rows; i++) {
            for (int k = 0; k < this.cols; k++){
                double aik = this.data[i][k];
                if (aik == 0.0) continue;
                for (int j = 0; j < other.cols; j++){
                    res.data[i][j] += aik * other.data[k][j];
                }
            }
        }
        return res;
    }

    //Transpose matriks (A^T)
    public Matrix transpose(){
        Matrix res = new Matrix(cols, rows);
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){
                res.data[j][i] = data[i][j];
            }
        }
        return res;
    }

    //Hapus 1 baris dan 1 kolom (buat ekspansi kofaktor atau adjoin)
    public Matrix submatrix(int skipRow, int skipCol){
        if(rows < 2 || cols < 2){
            throw new IllegalArgumentException("Submatriks minor hanya bisa dibentuk dari matriks minimal 2x2");
        }
        if(skipRow < 0 || skipRow >= rows || skipCol < 0 || skipCol >= cols){
            throw new IndexOutOfBoundsException("Indeks baris/kolom yang dihapus di luar matriks");
        }
        Matrix res = new Matrix(rows - 1, cols - 1);
        int ri = 0;
        for (int i = 0; i < rows; i++){
            if (i == skipRow) continue;
            int rj = 0;
            for (int j = 0; j < cols; j++){
                if (j == skipCol) continue;
                res.data[ri][rj] = data[i][j];
                rj++;
            }
            ri++;
        }
        return res;
    }

    public Matrix slice(int rowStart, int rowEnd, int colStart, int colEnd){
        if(rowStart < 0 || rowEnd > rows || rowStart >= rowEnd
                || colStart < 0 || colEnd > cols || colStart >= colEnd){
            throw new IndexOutOfBoundsException(String.format(
                "Blok [%d..%d) x [%d..%d) tidak valid untuk matriks %dx%d",
                rowStart, rowEnd, colStart, colEnd, rows, cols));
        }
        Matrix res = new Matrix(rowEnd - rowStart, colEnd - colStart);
        for(int i = rowStart; i < rowEnd; i++){
            System.arraycopy(data[i], colStart, res.data[i - rowStart], 0, colEnd - colStart);
        }
        return res;
    }

    //gabung matriks A dan B menjadi [A | B] (buat invers Gauss-Jordan)
    public static Matrix augment(Matrix a, Matrix b) {
        if (a.rows != b.rows){
            throw new IllegalArgumentException("Jumlah baris harus sama untuk augmentasi");
        }
        Matrix res = new Matrix(a.rows, a.cols + b.cols);
        for (int i = 0; i < a.rows; i++){
            System.arraycopy(a.data[i], 0, res.data[i], 0, a.cols);
            System.arraycopy(b.data[i], 0, res.data[i], a.cols, b.cols);
        }
        return res;
    }

    //buat matriks identitas N x N
    public static Matrix identity(int n){
        Matrix m = new Matrix(n, n);
        for (int i = 0; i < n; i++){
            m.data[i][i] = 1.0;
        }
        return m;
    }

    //tambahan
    public Matrix copy(){
        Matrix copyMatrix = new Matrix(this.rows, this.cols);
        for (int i = 0; i < this.rows; i++){
            System.arraycopy(data[i], 0, copyMatrix.data[i], 0, cols);
        }
        return copyMatrix;
    }

    public static double round3(double v){
        if(!Double.isFinite(v) || Math.abs(v) >= 1e15) return v;
        double r = Math.round(Math.abs(v) * 1000.0) / 1000.0;
        if(r == 0.0) return 0.0;
        return v < 0 ? -r : r;
    }

    public static String formatNumber(double v){
        String s = String.format(Locale.US, "%.3f", round3(v));
        if(s.indexOf('.') >= 0){
            s = s.replaceAll("0+$", "");
            if(s.endsWith(".")) s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++){
            sb.append("[ ");
            for (int j = 0; j < cols; j++){
                sb.append(String.format(Locale.US, "%10.3f ", round3(data[i][j])));
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}
