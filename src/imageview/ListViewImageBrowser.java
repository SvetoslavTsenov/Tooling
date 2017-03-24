package imageview;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import utils.ImageUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ListViewImageBrowser extends Region {

    final private Image deleteBtn = new Image(getClass().getResourceAsStream("../sign-delete-icon.png"), 20, 20, true, true);
    final private TextField directoryField = new TextField();
    private HBox browser = new HBox();
    private ImagesHolder imagesHolder = new ImagesHolder(null);
    private ListView<ImageViewModel> imageFilesList;
    private ObservableList<ImageViewModel> imageFiles;
    private ArrayList<ImageViewModel> imagesToReplace;

    private String localStorage;

    public ListViewImageBrowser() {
        this.imageFiles = FXCollections.observableArrayList();
        this.imageFilesList = new ListView<>(imageFiles);

        this.imageFilesList.setCellFactory(listView -> new ListCell<ImageViewModel>() {
            @Override
            public void updateItem(ImageViewModel imageViewModel, boolean empty) {
                super.updateItem(imageViewModel, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    ImagesHolder imagesHolder = ImagesHolder.convertImageViewModelToImagesHolder(imageViewModel);
                    imagesHolder.prefWidthProperty().bind(imageFilesList.widthProperty());
                    Button btn = new Button();
                    btn.setGraphic(new ImageView(deleteBtn));
                    btn.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            final int selectedIdx = getIndex();
                            final int newSelectedIdx =
                                    (selectedIdx == imageFilesList.getItems().size() - 1)
                                            ? selectedIdx - 1
                                            : selectedIdx;
                            imageFilesList.getItems().remove(selectedIdx);
                            imageFilesList.getSelectionModel().select(newSelectedIdx);
                        }
                    });
                    HBox btnBox = new HBox(btn);
                    btnBox.setAlignment(Pos.CENTER_RIGHT);
                    imagesHolder.addNodeTo(btnBox);
                    setGraphic(imagesHolder);
                }
            }
        });

        this.imageFilesList.getSelectionModel().selectedItemProperty().addListener((obs, oldFile, newFile) -> {
            if (newFile == null) {
                this.imagesHolder.clearChildrenOfMainContainer();
            } else {
                this.imagesHolder.clearChildrenOfMainContainer();
                ImageViewModel imageViewModel = (ImageViewModel) newFile;
                this.imagesHolder.addImageHolder(new ImageHolder("actual", imageViewModel.getAcual()));
                this.imagesHolder.addImageHolder(new ImageHolder("diff", imageViewModel.getDiff()));
                this.imagesHolder.addImageHolder(new ImageHolder("expected", imageViewModel.getExpected()));
            }
        });
        this.browser = this.browserBar();
        this.getChildren().add(this.browser);
        this.getChildren().add(this.imageFilesList);
        this.getChildren().add(this.imagesHolder);
    }

    public void init(ArrayList<ImageViewModel> images) {
        this.imageFiles.setAll(images);
    }

    public void update(ArrayList<ImageViewModel> imagesToReplace) {
        this.imageFiles.addAll(imagesToReplace);
    }

    public void update(ImageViewModel imageToReplace) {
        this.imageFiles.add(imageToReplace);
    }

    public ArrayList<ImageViewModel> getImagesToReplace() {
        this.imagesToReplace = new ArrayList<>(this.imageFiles);

        return this.imagesToReplace;
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        double browserHeight = 60;
        layoutInArea(this.browser, 0, 0, w / 3, browserHeight, 0, HPos.LEFT, VPos.TOP);
        layoutInArea(this.imageFilesList, 0, browserHeight, w / 4, h - browserHeight, 0, HPos.LEFT, VPos.CENTER);
        layoutInArea(this.imagesHolder, w / 4, browserHeight, 2 * w / 3, h - browserHeight, 0, HPos.LEFT, VPos.CENTER);
    }

    private HBox browserBar() {
        HBox hBox = new HBox();
        Button loadButton = new Button("Load");
        loadButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String dirText = directoryField.getText().trim();
                File f = new File(dirText);
                if (f.exists() && f.isDirectory()) {
                    imageFiles.addAll(load(Paths.get(f.getAbsolutePath())));
                }
            }
        });
        hBox = new HBox(5, this.directoryField, loadButton);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5));

        return hBox;
    }

    private List<ImageViewModel> load(Path directory) {
        ArrayList<ImageViewModel> files = ImageUtils.getImageViewModelsFromDirectory(directory, "*.{jpg,jpeg,png,JPG,JPEG,PNG}");

        return files;
    }
}