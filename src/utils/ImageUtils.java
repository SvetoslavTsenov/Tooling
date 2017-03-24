package utils;

import imageview.ImageViewModel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class ImageUtils {
    /**
     * Get image from file.
     * Returns null if image does not exist.
     */
    public static BufferedImage getImageFromFile(String filePath) {
        File file = new File(filePath);
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Save buffered image.
     */
    public static void saveBufferedImage(BufferedImage img, File file)
            throws IOException {
        ImageIO.write(img, "png", file);
    }

    /**
     * Save buffered image.
     */
    public static void saveBufferedImage(File img, String fileFullName)
            throws IOException {
        File f = new File(fileFullName);
        ImageIO.write(ImageIO.read(img), "png", f);
    }

    /**
     * This is a convenience method that calls find(File, String, boolean) with
     * the last parameter set to "false" (does not match directories).
     *
     * @see #find(File, String, boolean)
     */
    public static File find(File contextRoot, String fileName) {
        return find(contextRoot, fileName, false);
    }

    /**
     * Searches through the directory tree under the given context directory and
     * finds the first file that matches the file name. If the third parameter is
     * true, the method will also try to match directories, not just "regular"
     * files.
     *
     * @param contextRoot      The directory to start the search from.
     * @param fileName         The name of the file (or directory) to search for.
     * @param matchDirectories True if the method should try and match the name against directory
     *                         names, not just file names.
     * @return The java.io.File representing the <em>first</em> file or
     * directory with the given name, or null if it was not found.
     */
    public static File find(File contextRoot, String fileName, boolean matchDirectories) {
        if (contextRoot == null) {
            throw new NullPointerException("NullContextRoot");
        }

        if (fileName == null) {
            throw new NullPointerException("NullFileName");
        }

        if (!contextRoot.isDirectory()) {
            Object[] filler = {contextRoot.getAbsolutePath()};
            String message = "NotDirectory";
            throw new IllegalArgumentException(message);
        }

        File[] files = contextRoot.listFiles();

        //
        // for all children of the current directory...
        //
        for (int n = 0; n < files.length; ++n) {
            String nextName = files[n].getName();

            //
            // if we find a directory, there are two possibilities:
            //
            // 1. the names match, AND we are told to match directories.
            // in this case we're done
            //
            // 2. not told to match directories, so recurse
            //
            if (files[n].isDirectory()) {
                if (nextName.equals(fileName) && matchDirectories) {
                    return files[n];
                }

                File match = find(files[n], fileName);

                if (match != null) {
                    return match;
                }
            } else if (nextName.equals(fileName)) {
                // in the case of regular files, just check the names
                return files[n];
            }
        }

        return null;
    }

    public static boolean exist(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static void ensureFolderExists(String directory) {
        File file = new File(directory);
        if (!file.exists()) {
            boolean result = file.mkdirs();
        }
    }

    public static void cleanDirectory(String imageTempFolder) {
        File tempFolder = new File(imageTempFolder);
        if (tempFolder.exists() && tempFolder.isDirectory()) {
            File[] files = tempFolder.listFiles();
            for (File f : files) {
                f.delete();
            }
        }
    }

    public static ArrayList<String> getFileNamesFromDirectory(Path directory, String filter) {
        ArrayList<String> files = new ArrayList<>();
        try {
            Files.newDirectoryStream(directory, filter).forEach(file ->
                    files.add(file.toAbsolutePath().toString()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return files;
    }

    public static ArrayList<ImageViewModel> getImageViewModelsFromDirectory(Path directory, String filter) {
        ArrayList<ImageViewModel> files = new ArrayList<>();
        ArrayList names = new ArrayList();
        try {
            Files.newDirectoryStream(directory, filter).forEach(file -> {
                String f = file.toAbsolutePath().toString();
                if (!f.contains("_diff") && !f.contains("_expected")) {
                    String fName = ImageViewModel.convertUtlToFileName(f);
                    String fNameWithoutExt = fName.substring(0, fName.lastIndexOf("."));
                    if (!names.contains(fNameWithoutExt)) {
                        files.add(convertPathToImges(f, fName));
                        names.add(fNameWithoutExt);
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        return files;
    }

    private static ImageViewModel convertPathToImges(String file, String fName) {
        String ext = file.substring(file.lastIndexOf(".") + 1);

        ImageViewModel imageViewModel = new ImageViewModel();
        imageViewModel.setName(fName);

        imageViewModel.setActual(file);
        imageViewModel.setDiff(file.replace("." + ext, "_diff" + "." + ext));
        imageViewModel.setExpected(file.replace("." + ext, "_expected" + "." + ext));

        return imageViewModel;
    }


    public static ArrayList<File> getFilesFromDirectory(Path directory, String filter) {
        ArrayList<File> files = new ArrayList<>();
        try {
            Files.newDirectoryStream(directory, filter).forEach(file -> files.add(file.toFile()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return files;
    }

    public static void replaceFiles(ArrayList<File> filesToReplace, String storagePath) {
        for (File file : filesToReplace) {
            String fileFullName = storagePath + File.separator + file.getName();
            try {
                if (!fileFullName.endsWith("_diff.png") && !fileFullName.endsWith("_expected.png"))
                    saveBufferedImage(file, fileFullName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<String> getDirectories(String storage) {
        ArrayList<String> files = new ArrayList<>();
        File storageDirectory = new File(storage);
        if (storageDirectory.exists()) {
            String[] directories = storageDirectory.list(new FilenameFilter() {
                @Override
                public boolean accept(File current, String name) {
                    return new File(current, name).isDirectory();
                }
            });

            files = new  ArrayList<String>(Arrays.asList(directories));
        }

        return files;
    }
}
