package imageview;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ImagesHolder extends VBox {
    private static final int width = 120;
    private static final int height = 160;

    private StringProperty label;
    private HBox mainContainer;
    private ImageViewModel imageViewModel;

    public ImagesHolder(StringProperty label) {
        this.label = new SimpleStringProperty();
        if (label != null) {
            this.label.bindBidirectional(label);
            TextField lb = new TextField(this.getLabel());
            lb.textProperty().bindBidirectional(this.label);
            this.getChildren().add(lb);
        }

        this.mainContainer = new HBox();
        this.mainContainer.setSpacing(10);
        this.mainContainer.setAlignment(Pos.TOP_LEFT);
        this.getChildren().add(this.mainContainer);
    }

    public static void convertImageViewModelToImagesHolder(ImagesHolder imagesHolder, ImageViewModel imageViewModel) {
        imagesHolder.labelProperty().bindBidirectional(imageViewModel.nameProperty());
        imagesHolder.imageViewModel = imageViewModel;
        imagesHolder.addImageHolder(new ImageHolder("actual", imageViewModel.getAcual(), width, height, true, true));
        imagesHolder.addImageHolder(new ImageHolder("diff", imageViewModel.getDiff(), width, height, true, true));
        imagesHolder.addImageHolder(new ImageHolder("expected", imageViewModel.getExpected(), width, height, true, true));
    }

    public static ImagesHolder convertImageViewModelToImagesHolder(ImageViewModel imageViewModel) {
        ImagesHolder imagesHolder = new ImagesHolder(imageViewModel.nameProperty());
        imagesHolder.imageViewModel = imageViewModel;
        imagesHolder.addImageHolder(new ImageHolder("actual", imageViewModel.getAcual(), width, height, true, true));
        imagesHolder.addImageHolder(new ImageHolder("diff", imageViewModel.getDiff(), width, height, true, true));
        imagesHolder.addImageHolder(new ImageHolder("expected", imageViewModel.getExpected(), width, height, true, true));

        return imagesHolder;
    }
//
//    public static void convertImageViewModelToImagesHolderNoWidtHeight(ImagesHolder imagesHolder, ImageViewModel imageViewModel) {
//        ImagesHolder images = new ImagesHolder(imageViewModel.nameProperty());
//        imagesHolder.labelProperty().bindBidirectional(imageViewModel.nameProperty());
//        imagesHolder.imageViewModel = imageViewModel;
//        imagesHolder.addImageHolder(new ImageHolder("actual", imageViewModel.getAcual()));
//        imagesHolder.addImageHolder(new ImageHolder("diff", imageViewModel.getDiff()));
//        imagesHolder.addImageHolder(new ImageHolder("expected", imageViewModel.getExpected()));
//    }

    public static ImagesHolder convertImageUrlToImagesHolder(String url) {
        ImageViewModel imageViewModel = new ImageViewModel();
        imageViewModel.setName(ImageViewModel.convertUtlToFileName(url));
        imageViewModel.setActual(url);
        imageViewModel.setDiff(url.replace("actual", "diff"));
        imageViewModel.setExpected(url.replace("actual", "expected"));
        ImagesHolder imagesHolder = new ImagesHolder(imageViewModel.nameProperty());
        imagesHolder.imageViewModel = imageViewModel;
        imagesHolder.addImageHolder(new ImageHolder("actual", imageViewModel.getAcual()));
        imagesHolder.addImageHolder(new ImageHolder("diff", imageViewModel.getDiff()));
        imagesHolder.addImageHolder(new ImageHolder("expected", imageViewModel.getExpected()));

        return imagesHolder;
    }

    public ImageViewModel getImageViewModel() {
        return this.imageViewModel;
    }

    public void setImageViewModel(ImageViewModel imageViewModel) {
        this.imageViewModel = imageViewModel;
    }

    public String getLabel() {
        return label.get();
    }

    public void setLabel(String first) {
        this.label.set(first);
    }

    public StringProperty labelProperty() {
        return this.label;
    }

    public void addImageHolder(ImageHolder imageHolder) {
        imageHolder.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY && event.isAltDown()) {
                    event.consume();
                    mainContainer.getChildren().remove(imageHolder);
                }
            }
        });
        this.mainContainer.getChildren().add(imageHolder);
    }

    public void addNodeTo(Node node) {
        this.mainContainer.getChildren().add(node);
    }

    public void clearChildrenOfMainContainer() {
        this.mainContainer.getChildren().clear();
    }
}
