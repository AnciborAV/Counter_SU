package aav;

import java.io.*;

public class SerializeService {
    public static final String OUTPUT_FILE = "out.txt";

    private static int readFromFile() {
        try (var reader = new BufferedReader(new FileReader(OUTPUT_FILE))) {
            var s = reader.readLine();
            if (s == null || s.isBlank() || s.isEmpty()) {
                return 0;
            }
            var i = Integer.parseInt(s);
            System.out.printf("Counter is loaded, current value is: %s%n", i);
            return i;
        } catch (FileNotFoundException | NumberFormatException e) {
            return 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeToFile(int i) {
        var fout = new File(OUTPUT_FILE);
        try (var fos = new FileOutputStream(fout);
             var bw = new BufferedWriter(new OutputStreamWriter(fos));) {

            bw.write(String.valueOf(i));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void serialize(int i) {
        writeToFile(i);
    }

    public int deserialize() {
        return readFromFile();
    }
}
