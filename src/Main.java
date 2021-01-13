import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[]args) {
        String input= "";
        try {
            input = FileUtil.readFile(new File("src\\code.txt"));
            RecursiveDescentParser recursiveDescentParser = new RecursiveDescentParser(input);
            recursiveDescentParser.parse();
        }
        catch (IOException e) { System.out.println(e.getMessage()); }
    }
}
