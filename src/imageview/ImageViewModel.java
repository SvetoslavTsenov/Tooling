package imageview;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;

public class ImageViewModel {

    private StringProperty name = new SimpleStringProperty();
    private StringProperty actual = new SimpleStringProperty();
    private StringProperty expected = new SimpleStringProperty();
    private StringProperty diff = new SimpleStringProperty();

    public String getAcual() {
        return actual.get();
    }

    public void setActual(String first) {
        this.actual.set(first);
    }

    public StringProperty actualProperty() {
        return this.actual;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String first) {
        this.name.set(first);
    }

    public StringProperty nameProperty() {
        return this.name;
    }

    public void setExpected(String expected) {
        this.expected.set(expected);
    }

    public String getExpected() {
        return this.expected.get();
    }

    public StringProperty expectedProperty() {
        return this.expected;
    }

    public void setDiff(String diff) {
        this.diff.set(diff);
    }

    public String getDiff() {
        return this.diff.get();
    }

    public StringProperty diffProperty() {
        return this.diff;
    }

    public static ImageViewModel convertUrlToImageViewModel(String imageFullName) {
        ImageViewModel imageViewModel = new ImageViewModel();
        imageViewModel.setName(convertUtlToFileName(imageFullName));
        imageViewModel.setActual(imageFullName);
        imageViewModel.setDiff(imageFullName.replace("actual", "diff"));
        imageViewModel.setExpected(imageFullName.replace("actual", "expected"));

        return imageViewModel;
    }

    public static ArrayList<ImageViewModel> convertListOfUrlsToListOfIMageViewModels(ArrayList<String> urls) {
        ArrayList<ImageViewModel> imageViewModels = new ArrayList<>();

        for (String url :
                urls) {
            imageViewModels.add(convertUrlToImageViewModel(url));
        }

        return imageViewModels;
    }

    public static String convertUtlToFileName(String url) {
        String pathFullName = url;
        String fileName = pathFullName.substring(pathFullName.lastIndexOf("/") + 1);

        return fileName;
    }
}
