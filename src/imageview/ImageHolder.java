package imageview;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;

public class ImageHolder extends VBox {
    private StringProperty name = new SimpleStringProperty();

    public ImageHolder(String label, String imagePath) {
        this.createLabel(label);

        this.setName(imagePath);
        String file = this.getFileToUri(imagePath);
        int maxWidth = 400;
        int maxHeight = 800;
        Image image = new Image(file, maxWidth, maxHeight, true, true);
        ImageView holder = new ImageView(image);
        holder.setPreserveRatio(true);
        this.setPrefWidth(image.getWidth());
        this.setPrefWidth(image.getHeight() + 50);
        this.getChildren().add(holder);
    }

    public ImageHolder(String label, String pathFullName, int width, int height, boolean preserveRatio, boolean smooth) {
        this.createLabel(label);

        String file = this.getFileToUri(pathFullName);
        this.setName(pathFullName);

        try {
            Image image = new Image(file, width, height, preserveRatio, smooth);
            ImageView holder = new ImageView(image);
            holder.setPreserveRatio(true);

            this.getChildren().add(holder);
        } catch (Exception ex) {

        }
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

    private void createLabel(String label) {
        if (label != "") {
            Label lb = new Label(label);
            lb.setStyle("-fx-background-color: red;");
            this.getChildren().add(lb);
        }
    }

    private String getFileToUri(String pathFullName) {
        File f = new File(pathFullName);
        String file = pathFullName;
        if (f.exists() && f.isFile()) {
            file = f.toURI().toString();
        }
        return file;
    }
}
