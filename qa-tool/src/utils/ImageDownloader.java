package utils;

import imageview.ImageViewModel;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ImageDownloader {

    public static void downlaodImages(ArrayList<ImageViewModel> imageViewModels, String targetDirectory) {
        for (ImageViewModel imageViewModel : imageViewModels) {
            String imageName = imageViewModel.getName();
            String actualImageFullName = imageViewModel.getAcual();
            String actualName = ImageViewModel.convertUtlToFileName(actualImageFullName);
            if (!imageName.equals(actualName)) {
                actualImageFullName = actualImageFullName.replace(actualName, imageName);
            }

            downloadImage(actualImageFullName, targetDirectory);
            downloadImage(imageViewModel.getDiff(), targetDirectory);
            downloadImage(imageViewModel.getExpected(), targetDirectory);
        }
    }

    public static String downloadImage(String sourceUrl, String targetDirectory) {
        ImageUtils.ensureFolderExists(targetDirectory);
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