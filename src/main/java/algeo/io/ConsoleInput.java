package algeo.io;

import algeo.matrix.Matrix;
import java.io.InputStream;
import java.util.Scanner;

public class ConsoleInput {
    private final Scanner sc;

    public ConsoleInput(InputStream in){
        sc = new Scanner(in);
    }

    public String readLine(String prompt){
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    public String readNonEmptyLine(String prompt){
        while(true){
            String s = readLine(prompt);
            if(!s.isEmpty()) return s;
            System.out.println("Input tidak boleh kosong.");
        }
    }

    public int readInt(String prompt, int min, int max){
        while(true){
            String s = readLine(prompt);
            try{
                int v = Integer.parseInt(s);
                if(v >= min && v <= max) return v;
                System.out.println("Masukkan bilangan bulat dari " + min + " sampai " + max + ".");
            } catch(NumberFormatException e){
                System.out.println("\"" + s + "\" bukan bilangan bulat, coba lagi.");
            }
        }
    }

    public double readDouble(String prompt){
        while(true){
            String s = readLine(prompt);
            try{
                return FileIO.parseNumber(s);
            } catch(NumberFormatException e){
                System.out.println(e.getMessage() + ", coba lagi.");
            }
        }
    }

    public boolean readYesNo(String prompt){
        while(true){
            String s = readLine(prompt).toLowerCase();
            if(s.equals("y") || s.equals("ya")) return true;
            if(s.equals("n") || s.equals("tidak")) return false;
            System.out.println("Jawab dengan y atau n.");
        }
    }

    public Matrix readMatrix(int rows, int cols){
        double[][] data = new double[rows][];
        for(int i = 0; i < rows; i++){
            data[i] = readNumbers("Baris " + (i + 1) + ": ", cols);
        }
        return new Matrix(data);
    }

    public double[][] readPoints(int n){
        double[] x = new double[n];
        double[] y = new double[n];
        for(int i = 0; i < n; i++){
            double[] p = readNumbers("Titik " + (i + 1) + " (x y): ", 2);
            x[i] = p[0];
            y[i] = p[1];
        }
        return new double[][]{x, y};
    }

    private double[] readNumbers(String prompt, int count){
        while(true){
            String s = readLine(prompt);
            String[] tokens = s.isEmpty() ? new String[0] : s.split("\\s+");
            if(tokens.length != count){
                System.out.println("Harus berisi " + count + " angka, ditemukan " + tokens.length + ". Ulangi baris ini.");
                continue;
            }
            try{
                double[] v = new double[count];
                for(int j = 0; j < count; j++){
                    v[j] = FileIO.parseNumber(tokens[j]);
                }
                return v;
            } catch(NumberFormatException e){
                System.out.println(e.getMessage() + ". Ulangi baris ini.");
            }
        }
    }
}