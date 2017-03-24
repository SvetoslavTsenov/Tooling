package imageview;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import utils.ImageUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tsenov on 2/13/17.
 */
public class CompareImagesViewController extends Region {

    private static final String STORAGE_ENVIRONMENT_VARIABLE = "STORAGE";

    private String selectedBranch;
    private String selectedApplication;

    // containers
    private final HBox toolBar;
    private final HBox bottomBar;
    private ComboBox cbBranches;
    private ComboBox cbApplications;
    private TextField searchBar;
    private ScrollPane imagesHolder;
    private ListView<ImageArrayModel> allImagesListView;
    private Property<Node> imagesToCompareHolder;

    private ProgressIndicator loadingIndicator;

    private ImageArrayModel selectedImageArrayModel;

    private StringProperty storagePath = new SimpleStringProperty();
    private StringProperty searchBarText = new SimpleStringProperty();

    private ListProperty<String> applicationsFoldersProperty = new SimpleListProperty<>();
    private ObservableList<ImageArrayModel> allAvailableImagesList;
    private FilteredList<ImageArrayModel> filteredData;

    public CompareImagesViewController(String stable, String application) {
        this.loadingIndicator = new ProgressIndicator();
        this.loadingIndicator.setVisible(false);
        this.selectedBranch = stable;
        this.selectedApplication = application;
        this.toolBar = this.getControlToolBar();
        this.bottomBar = this.getBottomBar();
        this.allAvailableImagesList = FXCollections.observableArrayList();
        this.allImagesListView = new ListView<>(this.allAvailableImagesList);
        this.allImagesListView.setVisible(true);
        this.allImagesListView.setEditable(true);
        this.imagesHolder = new ScrollPane();
        this.imagesToCompareHolder = new SimpleObjectProperty<>();
        this.imagesHolder.contentProperty().bindBidirectional(this.imagesToCompareHolder);
        this.searchBar = new TextField();
        this.searchBar.textProperty().bindBidirectional(this.searchBarText);

        this.filteredData = new FilteredList<>(this.allAvailableImagesList, i -> true);

        this.allImagesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // 3. Wrap the FilteredList in a SortedList.
        SortedList<ImageArrayModel> sortedData = new SortedList<>(filteredData);

        // 4. Bind the SortedList comparator to the TableView comparator.

        // 5. Add sorted (and filtered) data to the table.
        this.allImagesListView.setItems(filteredData);

        this.searchBar.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                filteredData.setPredicate(i -> {
                    // If filter text is empty, display all persons.
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    // Compare first name and last name of every person with filter text.
                    String lowerCaseFilter = newValue.toLowerCase();

                    if (i.getImageName().toLowerCase().contains(lowerCaseFilter)) {
                        return true; // Filter matches first name.
                    } else if (i.getImageName().toLowerCase().contains(lowerCaseFilter)) {
                        return true; // Filter matches last name.
                    }
                    return false; // Does not match.
                });
            }
        });

        this.allImagesListView.setCellFactory(listView -> new ListCell<ImageArrayModel>() {
            @Override
            public void updateItem(ImageArrayModel image, boolean empty) {
                super.updateItem(image, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(image.getImageName());
                }
            }
        });

        this.allImagesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldFile, newFile) -> {
            if (newFile == null) {
            } else {
                ImageArrayModel imageArrayModel = (ImageArrayModel) newFile;

                loadingIndicator.setVisible(true);
                Platform.runLater(() -> {
                    ImagesHolder imgsHolder = convertImageArrayModelToImagesHolder(imageArrayModel);
                    selectedImageArrayModel = new ImageArrayModel(imageArrayModel.getImageName());
                    imagesToCompareHolder.setValue(imgsHolder);
                });

                Platform.runLater(
                        () -> loadingIndicator.setVisible(false) // stop displaying the loading indicat
                );
            }
        });

        this.getChildren().addAll(this.toolBar, this.allImagesListView, this.imagesHolder, this.loadingIndicator, this.bottomBar, this.searchBar);
        this.setStyle("-fx-background-color: #7CFC00");
    }

    private HBox getBottomBar() {

        Button getImagesToCompare = new Button("Compare selected images");
        getImagesToCompare.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ArrayList<ListView<String>> images = new ArrayList();
                ImagesHolder imgsHolder = convertImageArrayModelToImagesHolder(selectedImageArrayModel);
                ScrollPane scrollPane = new ScrollPane(imgsHolder);
                Scene sc = new Scene(scrollPane);
                Stage st = new Stage();
                st.setScene(sc);
                st.show();
            }
        });

        Button deleteFiles = new Button("Delete files");
        deleteFiles.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ObservableList<ImageArrayModel> images = allImagesListView.getSelectionModel().getSelectedItems();
                for (ImageArrayModel imageArrayModel : images) {
                    for (String imagePath : imageArrayModel.getImagesPaths()) {
                        new File(imagePath).delete();
                        allAvailableImagesList.remove(imagePath);
                        allImagesListView.getItems().remove(imagePath);
                        allImagesListView.refresh();
                    }
                }
            }
        });
        return new HBox(deleteFiles, getImagesToCompare);
    }

    private ImagesHolder convertImageArrayModelToImagesHolder(final ImageArrayModel imageArrayModel) {
        ImagesHolder imgsHolder = new ImagesHolder(null);
        for (String image :
                imageArrayModel.getImagesPaths()) {
            ImageHolder imageHolder = new ImageHolder(image, image);
            imageHolder.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getButton() == MouseButton.PRIMARY && event.isAltDown()) {
                        imageArrayModel.getImagesPaths().remove(image);
                        selectedImageArrayModel.addPath(image);
                        event.consume();
                    }
                }
            });
            imgsHolder.addImageHolder(imageHolder);
        }
        return imgsHolder;
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        double browserActionBarHeight = this.toolBar.prefHeight(w);
        double bottomBarHeight = this.bottomBar.getHeight();
        layoutInArea(this.toolBar, 0, 0, w, 60, 0, HPos.LEFT, VPos.TOP);

        layoutInArea(this.searchBar, 0, browserActionBarHeight, w / 6, h - browserActionBarHeight - bottomBarHeight, 0, HPos.LEFT, VPos.TOP);
        layoutInArea(this.allImagesListView, 0, browserActionBarHeight, w / 6, h - browserActionBarHeight - bottomBarHeight - this.searchBar.getHeight(), 0, HPos.LEFT, VPos.TOP);

        layoutInArea(this.imagesHolder, w / 6, browserActionBarHeight, 5 * w / 6, h - browserActionBarHeight - bottomBarHeight, 0, HPos.LEFT, VPos.TOP);
        layoutInArea(this.loadingIndicator, w / 6, browserActionBarHeight, 5 * w / 6, h - browserActionBarHeight - bottomBarHeight, 0, HPos.LEFT, VPos.TOP);
        layoutInArea(this.bottomBar, 0, h - bottomBarHeight, w, bottomBarHeight, 0, HPos.LEFT, VPos.TOP);
    }

    private HBox getControlToolBar() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #7CFC00");

        ArrayList<String> branches = new ArrayList<>();
        branches.add("Stable");
        branches.add("Release");
        this.cbBranches = new ComboBox(FXCollections.observableArrayList(branches));
        this.cbBranches.setEditable(true);
        this.cbBranches.setPrefSize(100, 20);
        this.cbBranches.setOnAction(this.onBranchChanged());
        this.cbBranches.getSelectionModel().select(this.selectedBranch);

        this.cbApplications = new ComboBox();
        this.cbApplications.setVisible(false);
        this.cbApplications.setPrefSize(100, 20);
        this.cbApplications.setEditable(true);
        this.cbApplications.setOnAction(this.onApplicationChanged());


        Button getImages = new Button("Get images");
        getImages.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ArrayList<ListView<String>> images = new ArrayList();
                getAllImagesFromApplication(new File(storagePath.getValue()));
            }
        });

        this.storagePath.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (new File(newValue).exists()) {
                    getImages.setVisible(true);
                } else {
                    getImages.setVisible(false);
                }
            }
        });

        String selectedB = cbBranches.getSelectionModel().getSelectedItem().toString();
        if (selectedB != "") {
            File branchFile = this.getBranchFullPath(selectedB);
            this.loadDirectories(branchFile, this.cbApplications, this.applicationsFoldersProperty);
            if (this.applicationsFoldersProperty.contains(this.selectedApplication)) {
                this.cbApplications.getSelectionModel().select(this.selectedApplication);
                File appFile = this.getSelectedApplication(this.selectedApplication);

            }
        }


        TextField storageFiled = new TextField();
        storageFiled.setMinWidth(Region.USE_PREF_SIZE);
        storageFiled.setMaxWidth(Region.USE_PREF_SIZE);
        storageFiled.textProperty().addListener((ov, prevText, currText) -> {
            // Do this in a Platform.runLater because of Textfield has no padding at first time and so on
            Platform.runLater(() -> {
                Text text = new Text(currText);
                text.setFont(storageFiled.getFont()); // Set the same font, so the size is the same
                double width = text.getLayoutBounds().getWidth() // This big is the Text in the TextField
                        + storageFiled.getPadding().getLeft() + storageFiled.getPadding().getRight() // Add the padding of the TextField
                        + 2d; // Add some spacing
                storageFiled.setPrefWidth(width); // Set the width
                storageFiled.positionCaret(storageFiled.getCaretPosition()); // If you remove this line, it flashes a little bit
                text.setUnderline(true);
                text.setStyle("-fx-text-fill: light-gray; -fx-font-size: 16;");
            });
        });
        storageFiled.textProperty().bindBidirectional(this.storagePath);
        hbox.getChildren().addAll(
                this.cbBranches,
                this.cbApplications,
                storageFiled,
                getImages);

        return hbox;
    }

    private EventHandler<ActionEvent> onBranchChanged() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String branch = cbBranches.getSelectionModel().getSelectedItem().toString();
                File file = getBranchFullPath(branch);
                loadDirectories(file, cbApplications, applicationsFoldersProperty);
                storagePath.setValue(selectedBranch);
            }
        };
    }

    private EventHandler<ActionEvent> onApplicationChanged() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String application = cbApplications.getSelectionModel().getSelectedItem().toString();
                File file = getSelectedApplication(application);

                getAllImagesFromApplication(file);
                storagePath.setValue(selectedApplication);
            }
        };
    }

    private void getAllImagesFromApplication(File file) {
        ArrayList<String> filesToReplace = ImageUtils.getDirectories(file.getAbsolutePath());
        Map<String, ImageArrayModel> images = new HashMap<String, ImageArrayModel>();
        loadingIndicator.setVisible(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1); // just emulates some loading time
                    try {
                        for (String dir : filesToReplace) {
                            for (String path : ImageUtils.getFileNamesFromDirectory(new File(file.getAbsolutePath() + File.separator + dir).toPath(), "*.{jpg,jpeg,png,JPG,JPEG,PNG}")) {
                                String fileName = new File(path).getName();
                                if (!images.keySet().contains(fileName)) {
                                    ImageArrayModel imageArrayModel = new ImageArrayModel(fileName);
                                    imageArrayModel.addPath(path);
                                    images.put(fileName, imageArrayModel);
                                } else {
                                    ImageArrayModel imageArrayModel = images.get(fileName);
                                    imageArrayModel.addPath(path);
                                }
                            }
                        }

                        ArrayList<ImageArrayModel> arrayModels = new ArrayList<>();
                        for (String key : images.keySet()) {
                            arrayModels.add(images.get(key));

                        }
                        arrayModels.sort(new Comparator<ImageArrayModel>() {
                            @Override
                            public int compare(ImageArrayModel o1, ImageArrayModel o2) {
                                return o1.getImageName().compareToIgnoreCase(o2.getImageName());
                            }
                        });

                        allAvailableImagesList.clear();
                        allAvailableImagesList.addAll(arrayModels);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            loadingIndicator.setVisible(false); // stop displaying the loading indicator
                        }
                    });
                }
            }
        }).start();
    }

    private void loadDirectories(File file, ComboBox cb, ListProperty<String> property) {
        if (file.exists()) {
            String[] directories = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File current, String name) {
                    return new File(current, name).isDirectory();
                }
            });

            property.setValue(FXCollections.observableArrayList(directories));
            cb.itemsProperty().bind(property);
            cb.setVisible(true);
        }
    }

    private File getBranchFullPath(String branch) {
        String env = System.getenv(STORAGE_ENVIRONMENT_VARIABLE);
        String bEnv = env.replace("Stable", "").replace("Release", "");
        String branchFormat = bEnv + branch + File.separator + "images";
        File file = new File(branchFormat);
        this.selectedBranch = file.getAbsolutePath();

        return file;
    }

    private File getSelectedApplication(String application) {
        this.selectedApplication = selectedBranch + File.separator + application;
        return new File(selectedApplication);
    }
}
