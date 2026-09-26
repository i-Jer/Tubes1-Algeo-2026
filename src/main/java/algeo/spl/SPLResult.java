package algeo.spl;

import algeo.matrix.Matrix;

public class SPLResult {
    public enum Type { UNIQUE, NONE, INFINITE }

    private final Type type;
    // expr[i] = nilai x(i+1) dalam bentuk [konstanta, koef t1, koef t2, ...], null kalo solusi ga ada
    private final double[][] expr;

    SPLResult(Type type, double[][] expr){
        this.type = type;
        this.expr = expr;
    }

    public Type getType(){ return type; }

    // solusi tunggal x1..xn (dipake interpolasi sama regresi)
    public double[] getSolution(){
        if(type != Type.UNIQUE){
            throw new IllegalStateException("SPL tidak memiliki solusi tunggal");
        }
        double[] x = new double[expr.length];
        for(int i = 0; i < expr.length; i++){
            x[i] = expr[i][0];
        }
        return x;
    }

    @Override
    public String toString(){
        if(type == Type.NONE) return "Solusi tidak ada\n";
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < expr.length; i++){
            sb.append("x").append(i + 1).append(" = ").append(formatExpr(expr[i])).append('\n');
        }
        return sb.toString();
    }

    static String formatExpr(double[] e){
        StringBuilder sb = new StringBuilder();
        if(Matrix.round3(e[0]) != 0.0) sb.append(Matrix.formatNumber(e[0]));
        for(int k = 1; k < e.length; k++){
            double coef = Matrix.round3(e[k]);
            if(coef == 0.0) continue;
            if(sb.length() == 0) sb.append(coef < 0 ? "-" : "");
            else sb.append(coef < 0 ? " - " : " + ");
            if(Math.abs(coef) != 1.0) sb.append(Matrix.formatNumber(Math.abs(coef)));
            sb.append('t').append(k);
        }
        return sb.length() == 0 ? "0" : sb.toString();
    }
}