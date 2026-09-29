package algeo.io;

import java.nio.charset.Charset;

public final class Style {
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String DIM = "\u001B[2m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final int WIDTH = 60;

    private static boolean color = false;
    private static boolean unicode = false;

    private Style(){}

    public static void init(boolean plain){
        boolean off = plain || System.getenv("NO_COLOR") != null || System.console() == null;
        //cmd.exe lama tidak menerjemahkan kode ANSI, sedangkan Windows Terminal, VS Code, dan Git Bash bisa
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        boolean ansi = !windows || System.getenv("WT_SESSION") != null
            || System.getenv("TERM_PROGRAM") != null || System.getenv("TERM") != null;
        color = !off && ansi;
        unicode = !plain && canEncode("╔═║╚┌─│└»✓✗");
    }

    private static boolean canEncode(String chars){
        String enc = System.getProperty("stdout.encoding", System.getProperty("sun.stdout.encoding"));
        try{
            Charset cs = (enc != null) ? Charset.forName(enc) : Charset.defaultCharset();
            return cs.newEncoder().canEncode(chars);
        } catch(RuntimeException e){
            return false;
        }
    }

    private static String paint(String code, String text){
        return color ? code + text + RESET : text;
    }

    private static String repeat(String s, int n){
        return s.repeat(Math.max(0, n));
    }

    public static void banner(String title, String subtitle){
        int w = Math.max(title.length(), subtitle.length()) + 8;
        String h = unicode ? "═" : "=";
        String v = unicode ? "║" : "|";
        System.out.println(paint(MAGENTA, (unicode ? "╔" : "+") + repeat(h, w) + (unicode ? "╗" : "+")));
        System.out.println(paint(MAGENTA, v) + paint(BOLD, center(title, w)) + paint(MAGENTA, v));
        System.out.println(paint(MAGENTA, v) + paint(DIM, center(subtitle, w)) + paint(MAGENTA, v));
        System.out.println(paint(MAGENTA, (unicode ? "╚" : "+") + repeat(h, w) + (unicode ? "╝" : "+")));
    }

    private static String center(String s, int w){
        int left = (w - s.length()) / 2;
        return repeat(" ", left) + s + repeat(" ", w - s.length() - left);
    }

    public static void menu(String title, String[] items){
        int w = title.length() + 6;
        for(int i = 0; i < items.length; i++){
            w = Math.max(w, items[i].length() + 8);
        }
        w = Math.max(w, 44);
        String h = unicode ? "─" : "-";
        String v = unicode ? "│" : "|";
        System.out.println();
        System.out.println(paint(CYAN, (unicode ? "┌" : "+") + h + " ") + paint(BOLD, title)
            + paint(CYAN, " " + repeat(h, w - title.length() - 3) + (unicode ? "┐" : "+")));
        for(int i = 0; i < items.length; i++){
            String num = String.format("%2d.", i + 1);
            String pad = repeat(" ", w - 4 - num.length() - items[i].length());
            System.out.println(paint(CYAN, v) + "  " + paint(YELLOW, num) + " " + items[i] + pad + " " + paint(CYAN, v));
        }
        System.out.println(paint(CYAN, (unicode ? "└" : "+") + repeat(h, w) + (unicode ? "┘" : "+")));
    }

    public static String prompt(String text){
        return paint(CYAN + BOLD, (unicode ? "» " : "> ") + text);
    }

    public static void error(String msg){
        System.out.println(paint(RED, (unicode ? "✗ " : "[!] ") + msg));
    }

    public static void success(String msg){
        System.out.println(paint(GREEN, (unicode ? "✓ " : "[OK] ") + msg));
    }

    public static void info(String msg){
        System.out.println(paint(DIM, msg));
    }

    public static void result(String line){
        System.out.println(paint(GREEN + BOLD, line));
    }

    public static void printReport(CharSequence report){
        boolean inResult = false;
        System.out.println();
        for(String line : report.toString().split("\n")){
            if(line.startsWith("=== ") && line.endsWith(" ===")){
                String t = line.substring(4, line.length() - 4);
                String h = unicode ? "━" : "=";
                System.out.println(paint(MAGENTA + BOLD, repeat(h, 3) + " " + t + " " + repeat(h, WIDTH - t.length() - 5)));
            } else if(line.equals("Langkah perhitungan:") || line.equals("Hasil:") || line.equals("Hasil evaluasi:")){
                inResult = !line.startsWith("Langkah");
                String t = line.substring(0, line.length() - 1);
                String h = unicode ? "─" : "-";
                System.out.println();
                System.out.println(paint(CYAN + BOLD, repeat(h, 2) + " " + t + " " + repeat(h, WIDTH - t.length() - 4)));
            } else if(line.startsWith("Metode: ") || line.startsWith("Sumber input: ")){
                int p = line.indexOf(':') + 1;
                System.out.println(paint(DIM, line.substring(0, p)) + paint(BOLD, line.substring(p)));
            } else if(line.startsWith("Langkah tidak ditampilkan")){
                System.out.println(paint(DIM, line));
            } else if(line.matches("R\\d+ (=|<->) .*")){
                System.out.println(paint(YELLOW, line));
            } else if(inResult && !line.isEmpty()){
                System.out.println(paint(GREEN + BOLD, line));
            } else if(line.endsWith(":") && !line.startsWith("[")){
                System.out.println(paint(BOLD, line));
            } else {
                System.out.println(line);
            }
        }
    }
}