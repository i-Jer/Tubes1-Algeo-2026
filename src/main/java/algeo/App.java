package algeo;

import algeo.cli.CLI;
import algeo.io.Style;
import java.util.Arrays;

public class App {
    public static void main(String[] args){
        Style.init(Arrays.asList(args).contains("--plain"));
        new CLI().run();
    }
}