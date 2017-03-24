package filemanager;

import imageview.ImageHolder;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import utils.OSUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Created by tsenov on 2/13/17.
 */
public class FilesManagerViewController extends Region {

    // containers
    private final HBox toolBar;
    private final HBox bottomBar;
    private TextField searchBar;
    private ScrollPane imagesHolder;
    private ListView<File> allImagesListView;
    private Property<Node> imagesToCompareHolder;

    private ProgressIndicator loadingIndicator;


    private StringProperty storagePath = new SimpleStringProperty();
    private StringProperty searchBarText = new SimpleStringProperty();

    private ObservableList<File> allAvailableImagesList;
    private FilteredList<File> filteredData;

    public FilesManagerViewController() {
        this.prefWidth(500);
        this.prefHeight(500);
        this.loadingIndicator = new ProgressIndicator();
        this.loadingIndicator.setVisible(false);

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
        SortedList<File> sortedData = new SortedList<>(filteredData);

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

                    if (i.getName().toLowerCase().contains(lowerCaseFilter)) {
                        return true; // Filter matches first name.
                    } else if (i.getName().toLowerCase().contains(lowerCaseFilter)) {
                        return true; // Filter matches last name.
                    }
                    return false; // Does not match.
                });
            }
        });

        this.allImagesListView.setCellFactory(listView -> new ListCell<File>() {
            @Override
            public void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {

                    long millisecondsSinceEpoch = file.lastModified();
                    Instant instant = Instant.ofEpochMilli(millisecondsSinceEpoch);
                    ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);

//                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");
//                    String output = formatter.format(zdt);
                    //StringProperty fileName = new SimpleStringProperty(file.getName() + " t" + output);
                    StringProperty fileName = new SimpleStringProperty(file.getName());
                    //TextField tf = new TextField();
                    //tf.textProperty().bindBidirectional(fileName);
                    this.setText(fileName.getValue());
                    fileName.addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                            file.renameTo(new File(newValue));
                        }
                    });

                    //setGraphic(tf);
                }
            }
        });

        this.allImagesListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    File path = allImagesListView.getSelectionModel().getSelectedItem();
                    if (path.isDirectory()) {
                        storagePath.setValue(path.getAbsolutePath());
                    }
                } else {
                    File path = allImagesListView.getSelectionModel().getSelectedItem();
                    if (!path.isDirectory()) {
                        if (path.getAbsolutePath().endsWith(".png") || path.getAbsolutePath().endsWith(".jpg")) {
                            ImageHolder imgsHolder = new ImageHolder(path.getName(), path.getAbsolutePath());
                            imagesToCompareHolder.setValue(imgsHolder);
                        } else if (path.getAbsolutePath().endsWith("txt")
                                || path.getAbsolutePath().endsWith("xml")
                                || path.getAbsolutePath().endsWith("html")
                                || path.getAbsolutePath().endsWith("sh")) {
                            if (path.canRead()) {
                                byte[] encoded = new byte[0];
                                try {
                                    encoded = Files.readAllBytes(path.toPath());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                TextArea area = new TextArea(new String(encoded, Charset.forName("UTF8")));
                                imagesToCompareHolder.setValue(area);
                            }
                        }
                    }
                }
            }
        });

        this.getChildren().addAll(this.toolBar, this.allImagesListView, this.imagesHolder, this.loadingIndicator, this.bottomBar, this.searchBar);
        this.setStyle("-fx-background-color: #7CFC00");
    }

    private HBox getBottomBar() {

        Button deleteFiles = new Button("Delete files");
        deleteFiles.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ObservableList<File> files = allImagesListView.getSelectionModel().getSelectedItems();
                files.forEach(f -> {
                    f.delete();
                    allAvailableImagesList.remove(f);
                    allImagesListView.getItems().remove(f);
                    allImagesListView.refresh();
                });
            }
        });
        return new HBox(deleteFiles);
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        double browserActionBarHeight = this.toolBar.prefHeight(w);
        double bottomBarHeight = this.bottomBar.getHeight();
        layoutInArea(this.toolBar, 0, 0, w, 60, 0, HPos.LEFT, VPos.TOP);

        layoutInArea(this.searchBar, 0, browserActionBarHeight, w / 4, h - browserActionBarHeight - bottomBarHeight, 0, HPos.LEFT, VPos.TOP);
        layoutInArea(this.allImagesListView, 0, browserActionBarHeight, w / 3, h - browserActionBarHeight - bottomBarHeight - this.searchBar.getHeight(), 0, HPos.LEFT, VPos.TOP);

        layoutInArea(this.imagesHolder, this.allImagesListView.getWidth() + 100, browserActionBarHeight, 2 * w / 3, h - browserActionBarHeight - bottomBarHeight, 0, HPos.LEFT, VPos.TOP);
//        layoutInArea(this.loadingIndicator, w / 6, browserActionBarHeight, 5 * w / 6, h - browserActionBarHeight - bottomBarHeight, 0, HPos.LEFT, VPos.TOP);
        layoutInArea(this.bottomBar, 0, h - bottomBarHeight, w, bottomBarHeight, 0, HPos.LEFT, VPos.TOP);
    }

    private HBox getControlToolBar() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #7CFC00");

        Button btnOpen = new Button("Open");
        btnOpen.setVisible(false);
        btnOpen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                OSUtils.openFileExplorer(storagePath.getValue());
            }
        });

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

        this.storagePath.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                File file = new File(newValue);
                if (file.exists()) {
                    allAvailableImagesList.clear();

                    try {
                        Files.newDirectoryStream(file.toPath()).forEach(f ->
                                allAvailableImagesList.add(new File(f.toAbsolutePath().toString())));
                        btnOpen.setVisible(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    btnOpen.setVisible(true);
                }
            }
        });


        hbox.getChildren().addAll(
                storageFiled,
                btnOpen);

        return hbox;
    }
}
