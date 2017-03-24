package imageview;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageDownloader {

    public static String downloadImage(String sourceUrl, String targetDirectory) {
        File file = new File(targetDirectory);
        if (!file.exists()) {
            boolean result = file.mkdirs();
        }
        URL imageUrl = null;
        try {
            imageUrl = new URL(sourceUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        String filePath = imageUrl.getFile();
        String fileFullName = targetDirectory + File.separator + filePath.substring(filePath.lastIndexOf("/") + 1).replace("_actual", "");

        try {
            try (InputStream imageReader = new BufferedInputStream(
                    imageUrl.openStream());
                 OutputStream imageWriter = new BufferedOutputStream(
                         new FileOutputStream(fileFullName))) {
                int readByte;

                while ((readByte = imageReader.read()) != -1)

                {
                    imageWriter.write(readByte);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileFullName;
    }
}