package algeo.io;

import algeo.matrix.Matrix;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FileIO {
    private static final Pattern NUMBER = Pattern.compile("[+-]?(\\d+([.,]\\d*)?|[.,]\\d+)([eE][+-]?\\d+)?");

    public static double parseNumber(String token){
        String t = token.trim();
        if(!NUMBER.matcher(t).matches()){
            throw new NumberFormatException("\"" + t + "\" bukan angka yang valid");
        }
        double v = Double.parseDouble(t.replace(',', '.'));
        if(Double.isInfinite(v)){
            throw new NumberFormatException("\"" + t + "\" terlalu besar");
        }
        return v;
    }

    public static Matrix readMatrix(String path, int maxRows, int maxCols) throws IOException{
        List<String> lines = readLines(path);
        if(lines.isEmpty()){
            throw new IllegalArgumentException("File kosong");
        }
        if(lines.size() > maxRows){
            throw new IllegalArgumentException("Jumlah baris (" + lines.size() + ") melebihi batas " + maxRows);
        }
        double[][] data = new double[lines.size()][];
        for(int i = 0; i < lines.size(); i++){
            data[i] = parseLine(lines.get(i), i + 1);
            if(data[i].length != data[0].length){
                throw new IllegalArgumentException("Baris " + (i + 1) + " memiliki " + data[i].length
                    + " elemen, seharusnya " + data[0].length + " seperti baris 1");
            }
        }
        if(data[0].length > maxCols){
            throw new IllegalArgumentException("Jumlah kolom (" + data[0].length + ") melebihi batas " + maxCols);
        }
        return new Matrix(data);
    }

    public static double[][] readPoints(String path, int maxPoints) throws IOException{
        List<String> lines = readLines(path);
        if(lines.isEmpty()){
            throw new IllegalArgumentException("File kosong");
        }
        int n = lines.size();
        if(n > maxPoints){
            throw new IllegalArgumentException("Jumlah titik (" + n + ") melebihi batas " + maxPoints);
        }
        double[] x = new double[n];
        double[] y = new double[n];
        for(int i = 0; i < n; i++){
            double[] v = parseLine(lines.get(i), i + 1);
            if(v.length != 2){
                throw new IllegalArgumentException("Baris " + (i + 1) + " harus berisi tepat 2 angka (x y), ditemukan " + v.length);
            }
            x[i] = v[0];
            y[i] = v[1];
        }
        return new double[][]{x, y};
    }

    public static void write(String path, String content) throws IOException{
        try(Writer w = new FileWriter(path, StandardCharsets.UTF_8)){
            w.write(content);
        }
    }

    private static double[] parseLine(String line, int lineNo){
        String t = line.trim();
        if(t.isEmpty()){
            throw new IllegalArgumentException("Baris " + lineNo + " kosong");
        }
        String[] tokens = t.split("\\s+");
        double[] v = new double[tokens.length];
        for(int j = 0; j < tokens.length; j++){
            try{
                v[j] = parseNumber(tokens[j]);
            } catch(NumberFormatException e){
                throw new IllegalArgumentException("Baris " + lineNo + ": " + e.getMessage());
            }
        }
        return v;
    }

    private static List<String> readLines(String path) throws IOException{
        List<String> lines = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(path, StandardCharsets.UTF_8))){
            String line;
            while((line = br.readLine()) != null){
                lines.add(line);
            }
        }
        if(!lines.isEmpty() && lines.get(0).startsWith("\uFEFF")){
            lines.set(0, lines.get(0).substring(1));
        }
        while(!lines.isEmpty() && lines.get(lines.size() - 1).trim().isEmpty()){
            lines.remove(lines.size() - 1);
        }
        return lines;
    }
}