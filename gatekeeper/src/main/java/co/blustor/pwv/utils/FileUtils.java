package co.blustor.pwv.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    public static byte[] read(File file) throws IOException {

        byte[] buffer = new byte[(int) file.length()];

        InputStream ios = null;
        try {
            ios = new FileInputStream(file);
            if (ios.read(buffer) == -1) {
                throw new IOException("EOF reached while trying to read the whole file");
            }
        } finally {
            try {
                if (ios != null) {
                    ios.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return buffer;
    }
}
