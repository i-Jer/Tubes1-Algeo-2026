package algeo.cli;

import algeo.determinant.Determinant;
import algeo.interpolasi.Interpolasi;
import algeo.interpolasi.NaturalCubicSpline;
import algeo.inverse.Inverse;
import algeo.io.ConsoleInput;
import algeo.io.FileIO;
import algeo.io.InputEndException;
import algeo.io.Style;
import algeo.matrix.Matrix;
import algeo.regression.SplineRegression;
import algeo.spl.SPLResult;
import algeo.spl.SPLSolver;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

public class CLI {
    //batas ukuran input dari spesifikasi (3.1.1 poin 7)
    private static final int MANUAL_MAX = 11;
    private static final int FILE_MAX = 1001;
    private static final int MAX_POINTS = 10;

    private final ConsoleInput in = new ConsoleInput(System.in);
    //keterangan sumber input terakhir, ikut ditulis di hasil
    private String inputSource = "";

    public void run(){
        Style.banner("KALKULATOR ALJABAR LINIER", "Tugas Besar 1 IF2123 Aljabar Linier dan Geometri");
        String[] mainMenu = {"Sistem Persamaan Linier (SPL)", "Determinan Matriks", "Matriks Balikan (Invers)",
            "Interpolasi Polinomial", "Natural Cubic Spline Interpolation", "Regresi Spline Kubik", "Keluar"};
        try{
            while(true){
                Style.menu("MENU UTAMA", mainMenu);
                int choice = in.readInt("Pilih menu: ", 1, 7);
                if(choice == 7) break;
                try{
                    switch(choice){
                        case 1 -> menuSPL();
                        case 2 -> menuDeterminant();
                        case 3 -> menuInverse();
                        case 4 -> menuInterpolation();
                        case 5 -> menuSpline();
                        case 6 -> menuRegression();
                    }
                } catch(InputEndException e){
                    throw e;
                } catch(RuntimeException e){
                    //pengaman terakhir supaya program tidak crash
                    Style.error("Terjadi kesalahan: " + e.getMessage());
                }
            }
        } catch(InputEndException e){
            System.out.println();
            Style.info("Input berakhir.");
        }
        Style.info("Program selesai.");
    }

    private void menuSPL(){
        String[] methods = {"Metode Eliminasi Gauss", "Metode Eliminasi Gauss-Jordan", "Metode Matriks Balikan", "Kaidah Cramer"};
        int m = subMenu("SUB-MENU SPL", methods);
        if(m == 0) return;
        Matrix aug = readMatrixInput(true);
        if(aug == null) return;

        StringBuilder steps = stepsFor(aug);
        String result;
        try{
            SPLResult r = switch(m){
                case 1 -> SPLSolver.gauss(aug, steps);
                case 2 -> SPLSolver.gaussJordan(aug, steps);
                case 3 -> SPLSolver.inverseMethod(aug, steps);
                default -> SPLSolver.cramer(aug, steps);
            };
            result = r.toString();
        } catch(IllegalArgumentException | ArithmeticException e){
            result = e.getMessage() + "\n";
        }
        showAndSave(header("Sistem Persamaan Linier", methods[m - 1]) + matrixText("Matriks augmented", aug), steps, result);
    }

    private void menuDeterminant(){
        String[] methods = {"Metode Reduksi Baris (OBE)", "Metode Ekspansi Kofaktor"};
        int m = subMenu("SUB-MENU DETERMINAN", methods);
        if(m == 0) return;
        Matrix a = readMatrixInput(false);
        if(a == null) return;

        StringBuilder steps = stepsFor(a);
        String result;
        try{
            double det = (m == 1) ? Determinant.byRowReduction(a, steps) : Determinant.byCofactorExpansion(a, steps);
            result = "det = " + Matrix.formatNumber(det) + "\n";
        } catch(IllegalArgumentException | ArithmeticException e){
            result = e.getMessage() + "\n";
        }
        showAndSave(header("Determinan Matriks", methods[m - 1]) + matrixText("Matriks", a), steps, result);
    }

    private void menuInverse(){
        String[] methods = {"Metode Augmentasi [A | I] (Gauss-Jordan)", "Metode Adjoin"};
        int m = subMenu("SUB-MENU MATRIKS BALIKAN", methods);
        if(m == 0) return;
        Matrix a = readMatrixInput(false);
        if(a == null) return;

        StringBuilder steps = stepsFor(a);
        String result;
        try{
            Matrix inv = (m == 1) ? Inverse.byGaussJordan(a, steps) : Inverse.byAdjoint(a, steps);
            result = "A^-1 =\n" + inv;
        } catch(IllegalArgumentException | ArithmeticException e){
            result = e.getMessage() + "\n";
        }
        showAndSave(header("Matriks Balikan", methods[m - 1]) + matrixText("Matriks", a), steps, result);
    }

    private void menuInterpolation(){
        String[] methods = {"Interpolasi Polinomial (eliminasi Gauss)"};
        int m = subMenu("SUB-MENU INTERPOLASI POLINOMIAL", methods);
        if(m == 0) return;
        double[][] pts = readPointsInput();
        if(pts == null) return;
        double[] x = pts[0];
        double[] y = pts[1];

        String head = header("Interpolasi Polinomial", methods[0]) + pointsText(x, y);
        StringBuilder steps = new StringBuilder();
        double[] coef;
        try{
            coef = Interpolasi.calculateCoefficient(x, y, steps);
        } catch(IllegalArgumentException | ArithmeticException e){
            showAndSave(head, steps, e.getMessage() + "\n");
            return;
        }

        double min = Arrays.stream(x).min().getAsDouble();
        double max = Arrays.stream(x).max().getAsDouble();
        StringBuilder result = new StringBuilder("Koefisien:\n");
        for(int i = 0; i < coef.length; i++){
            result.append("a").append(i).append(" = ").append(Matrix.formatNumber(coef[i])).append('\n');
        }
        result.append("Domain interpolasi: ").append(Matrix.formatNumber(min)).append(" <= x <= ").append(Matrix.formatNumber(max)).append('\n');
        result.append(Interpolasi.toEquation(coef)).append('\n');

        StringBuilder report = buildReport(head, steps, result.toString());
        Style.printReport(report);
        evaluationLoop(report, "P", t -> {
            if(t < min || t > max){
                throw new IllegalArgumentException("x = " + Matrix.formatNumber(t) + " di luar domain ["
                    + Matrix.formatNumber(min) + ", " + Matrix.formatNumber(max) + "]");
            }
            return Interpolasi.evaluator(coef, t);
        });
        askSave(report);
    }

    //menu 5 tidak wajib memiliki sub-menu (3.1.3)
    private void menuSpline(){
        double[][] pts = readPointsInput();
        if(pts == null) return;
        double[] x = pts[0];
        double[] y = pts[1];

        String head = header("Natural Cubic Spline Interpolation", "Natural cubic spline (SPL tridiagonal dengan eliminasi Gauss)")
            + pointsText(x, y);
        StringBuilder steps = new StringBuilder();
        NaturalCubicSpline spline;
        try{
            spline = new NaturalCubicSpline(x, y, steps);
        } catch(IllegalArgumentException | ArithmeticException e){
            showAndSave(head, steps, e.getMessage() + "\n");
            return;
        }

        String result = spline + "Domain interpolasi: " + Matrix.formatNumber(spline.getDomainMin())
            + " <= x <= " + Matrix.formatNumber(spline.getDomainMax()) + "\n";
        StringBuilder report = buildReport(head, steps, result);
        Style.printReport(report);
        evaluationLoop(report, "S", spline::evaluate);
        askSave(report);
    }

    private void menuRegression(){
        String[] methods = {"Regresi Spline Kubik (truncated power basis, persamaan normal)"};
        int m = subMenu("SUB-MENU REGRESI SPLINE KUBIK", methods);
        if(m == 0) return;
        double[][] pts = readPointsInput();
        if(pts == null) return;
        double[] x = pts[0];
        double[] y = pts[1];

        int k = in.readInt("Jumlah knot K (0-" + MAX_POINTS + "): ", 0, MAX_POINTS);
        double[] knots = new double[k];
        for(int i = 0; i < k; i++){
            knots[i] = in.readDouble("Posisi knot " + (i + 1) + ": ");
        }
        Arrays.sort(knots);
        StringBuilder knotText = new StringBuilder("Posisi knot:");
        if(k == 0) knotText.append(" (tanpa knot)");
        for(double kn : knots){
            knotText.append(' ').append(Matrix.formatNumber(kn));
        }

        String head = header("Regresi Spline Kubik", methods[0]) + pointsText(x, y) + knotText + "\n";
        StringBuilder steps = new StringBuilder();
        SplineRegression reg;
        try{
            reg = new SplineRegression(x, y, knots, steps);
        } catch(IllegalArgumentException | ArithmeticException e){
            showAndSave(head, steps, e.getMessage() + "\n");
            return;
        }

        StringBuilder result = new StringBuilder("Koefisien regresi:\n");
        double[] beta = reg.getCoefficients();
        for(int j = 0; j < beta.length; j++){
            result.append("beta").append(j).append(" = ").append(Matrix.formatNumber(beta[j])).append('\n');
        }
        result.append(reg.toEquation()).append('\n');

        StringBuilder report = buildReport(head, steps, result.toString());
        Style.printReport(report);
        evaluationLoop(report, "y", reg::predict);
        askSave(report);
    }

    //tampilkan sub-menu, hasil 0 jika memilih kembali
    private int subMenu(String title, String[] options){
        String[] items = Arrays.copyOf(options, options.length + 1);
        items[options.length] = "Kembali";
        Style.menu(title, items);
        int c = in.readInt("Pilih metode: ", 1, options.length + 1);
        return c == options.length + 1 ? 0 : c;
    }

    //baca matriks dari keyboard atau file, null jika file gagal dibaca
    private Matrix readMatrixInput(boolean augmented){
        int source = in.readInt("Sumber input (1. Keyboard, 2. File .txt): ", 1, 2);
        if(source == 1){
            inputSource = "keyboard";
            if(augmented){
                int rows = in.readInt("Jumlah persamaan (1-" + MANUAL_MAX + "): ", 1, MANUAL_MAX);
                int vars = in.readInt("Jumlah variabel (1-" + MANUAL_MAX + "): ", 1, MANUAL_MAX);
                Style.info("Masukkan koefisien dan konstanta tiap persamaan, dipisah spasi:");
                return in.readMatrix(rows, vars + 1);
            }
            int rows = in.readInt("Jumlah baris (1-" + MANUAL_MAX + "): ", 1, MANUAL_MAX);
            int cols = in.readInt("Jumlah kolom (1-" + MANUAL_MAX + "): ", 1, MANUAL_MAX);
            Style.info("Masukkan elemen tiap baris, dipisah spasi:");
            return in.readMatrix(rows, cols);
        }

        String path = readPath("Path file .txt: ");
        try{
            Matrix m = FileIO.readMatrix(path, FILE_MAX, augmented ? FILE_MAX + 1 : FILE_MAX);
            inputSource = "file " + path;
            return m;
        } catch(FileNotFoundException e){
            Style.error("File tidak ditemukan atau tidak dapat dibuka: " + path);
        } catch(IOException e){
            Style.error("Gagal membaca file: " + e.getMessage());
        } catch(IllegalArgumentException e){
            Style.error("Format isi file tidak sesuai. " + e.getMessage());
        }
        return null;
    }

    //baca titik sampel dari keyboard atau file, null jika file gagal dibaca
    private double[][] readPointsInput(){
        int source = in.readInt("Sumber input (1. Keyboard, 2. File .txt): ", 1, 2);
        if(source == 1){
            inputSource = "keyboard";
            int n = in.readInt("Jumlah titik (1-" + MAX_POINTS + "): ", 1, MAX_POINTS);
            return in.readPoints(n);
        }

        String path = readPath("Path file .txt: ");
        try{
            double[][] pts = FileIO.readPoints(path, MAX_POINTS);
            inputSource = "file " + path;
            return pts;
        } catch(FileNotFoundException e){
            Style.error("File tidak ditemukan atau tidak dapat dibuka: " + path);
        } catch(IOException e){
            Style.error("Gagal membaca file: " + e.getMessage());
        } catch(IllegalArgumentException e){
            Style.error("Format isi file tidak sesuai. " + e.getMessage());
        }
        return null;
    }

    //minta nilai x berulang kali sampai input kosong, hasil f(x) dicetak dan dicatat ke report
    private void evaluationLoop(StringBuilder report, String name, DoubleUnaryOperator f){
        boolean first = true;
        while(true){
            String s = in.readLine("Masukkan nilai x untuk dievaluasi (kosongkan untuk selesai): ");
            if(s.isEmpty()) return;
            try{
                double t = FileIO.parseNumber(s);
                String line = name + "(" + Matrix.formatNumber(t) + ") = " + Matrix.formatNumber(f.applyAsDouble(t));
                Style.result(line);
                if(first){
                    report.append("Hasil evaluasi:\n");
                    first = false;
                }
                report.append(line).append('\n');
            } catch(IllegalArgumentException e){
                Style.error(e.getMessage());
            }
        }
    }

    private void askSave(StringBuilder report){
        if(!in.readYesNo("Simpan hasil ke file .txt? (y/n): ")) return;
        String path = readPath("Nama file output: ");
        if(!path.toLowerCase().endsWith(".txt")) path += ".txt";
        try{
            FileIO.write(path, report.toString());
            Style.success("Hasil disimpan ke " + path);
        } catch(IOException e){
            Style.error("Gagal menyimpan file: " + e.getMessage());
        }
    }

    //path boleh diapit tanda kutip (hasil "Copy as path" di Windows)
    private String readPath(String prompt){
        String p = in.readNonEmptyLine(prompt);
        if(p.length() >= 2 && p.startsWith("\"") && p.endsWith("\"")){
            p = p.substring(1, p.length() - 1);
        }
        return p;
    }

    private void showAndSave(String head, StringBuilder steps, String result){
        StringBuilder report = buildReport(head, steps, result);
        Style.printReport(report);
        askSave(report);
    }

    //gabungkan bagian input, langkah, dan hasil menjadi satu teks untuk layar dan file
    private static StringBuilder buildReport(String head, StringBuilder steps, String result){
        StringBuilder report = new StringBuilder(head).append('\n');
        if(steps == null){
            report.append("Langkah tidak ditampilkan karena matriks berukuran besar\n\n");
        } else if(steps.length() > 0){
            report.append("Langkah perhitungan:\n").append(steps).append('\n');
        }
        report.append("Hasil:\n").append(result);
        return report;
    }

    private String header(String title, String method){
        return "=== " + title + " ===\nMetode: " + method + "\nSumber input: " + inputSource + "\n";
    }

    //matriks sampai ukuran input keyboard ditampilkan lengkap beserta langkahnya
    private static boolean isSmall(Matrix m){
        return m.getRows() <= MANUAL_MAX && m.getCols() <= MANUAL_MAX + 1;
    }

    private static StringBuilder stepsFor(Matrix m){
        return isSmall(m) ? new StringBuilder() : null;
    }

    private static String matrixText(String label, Matrix m){
        if(isSmall(m)) return label + ":\n" + m;
        return label + ": " + m.getRows() + "x" + m.getCols() + " (tidak ditampilkan karena berukuran besar)\n";
    }

    private static String pointsText(double[] x, double[] y){
        StringBuilder sb = new StringBuilder("Titik sampel:\n");
        for(int i = 0; i < x.length; i++){
            sb.append("(").append(Matrix.formatNumber(x[i])).append(", ").append(Matrix.formatNumber(y[i])).append(")\n");
        }
        return sb.toString();
    }
}