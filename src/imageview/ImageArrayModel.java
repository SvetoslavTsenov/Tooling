package imageview;

import java.util.ArrayList;

/**
 * Created by tsenov on 3/7/17.
 */
public class ImageArrayModel {
    private String imageName;
    private ArrayList<String> imagesPaths;

    public ImageArrayModel(String imageName) {
        this.imageName = imageName;
        this.imagesPaths = new ArrayList<>();
    }

    public ArrayList<String> getImagesPaths() {
        return imagesPaths;
    }

    public String getImageName() {
        return imageName;
    }

    public void addPath(String path) {
        this.imagesPaths.add(path);
    }
}
