import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class FileUtil{
    /**
     * read file using buffer and return it as string
     * @param file
     * @return
     * @throws IOException
     */
    public static String readFile(File file) throws IOException
    {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
            BufferedReader BufferReader = new BufferedReader(fileReader);
            char[] chars = new char[(int) file.length()];
            BufferReader.read(chars);
            BufferReader.close();
            return new String(chars);
        }
        finally {
            if (fileReader != null) {
                fileReader.close();
            }
        }
    }
}
