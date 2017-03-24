package imageview;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import utils.ImageDownloader;
import utils.ImageUtils;
import utils.OSUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class ImageViewController extends Region {
    private static final String STORAGE_ENVIRONMENT_VARIABLE = "STORAGE";

    private HBox toolBar;
    private ListViewImageBrowser listViewImageBrowser;
    private ComboBox cbBranches;
    private ComboBox cbApplications;
    private ComboBox listViewDevices;

    private SimpleStringProperty localStorage = new SimpleStringProperty();
    private StringProperty remoteStoragePath = new SimpleStringProperty();

    private ListProperty<String> comboBoxProperty = new SimpleListProperty<>();
    private ListProperty<String> applicationsFoldersProperty = new SimpleListProperty<>();
    private ListProperty<String> listViewDevicesProperty = new SimpleListProperty<>();

    private String selectedBranch;
    private String selectedApplication;
    private String selectedDevice;

    public ImageViewController(String stable, String application, String device) {
        this.selectedBranch = stable;
        this.selectedApplication = application;
        this.selectedDevice = device;

        this.toolBar = this.getControlToolBar();
        this.listViewImageBrowser = new ListViewImageBrowser();

        this.getChildren().addAll(this.toolBar, this.listViewImageBrowser);
        this.setStyle("-fx-background-color: #7CFC00");
    }

    public String getLocalStorage() {
        return this.localStorage.getValue();
    }

    public void setLocalStorage(String localStorage) {
        this.localStorage.setValue(localStorage);
    }

    public void update(ImageViewModel imageViewModel) {
        this.listViewImageBrowser.update(imageViewModel);
    }

    public void update(ArrayList<ImageViewModel> imageViewModels) {
        this.listViewImageBrowser.update(imageViewModels);
    }

    public void clear() {
        this.getChildren().clear();
    }

    public void handleSubmitButtonAction(ActionEvent actionEvent) {
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        double browserActionBarHeight = this.toolBar.prefHeight(w);
        layoutInArea(this.toolBar, 0, 0, w, 60, 0, HPos.LEFT, VPos.TOP);
        layoutInArea(this.listViewImageBrowser, 0, browserActionBarHeight, w, h - browserActionBarHeight, 0, HPos.LEFT, VPos.TOP);
    }

    private HBox getControlToolBar() {
        HBox mainHbox = new HBox();
        VBox vBox = new VBox();
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #7CFC00");

        List<String> branches = new ArrayList<>();
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

        this.listViewDevices = new ComboBox();
        this.listViewDevices.setVisible(false);
        this.listViewDevices.setPrefSize(200, 20);
        this.listViewDevices.setEditable(true);
        this.listViewDevices.setOnAction(this.onDeviceSelected());

        Button btnOpenFolder = new Button("Open");
        btnOpenFolder.setVisible(false);
        btnOpenFolder.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                OSUtils.openFileExplorer(remoteStoragePath.getValue());
            }
        });


        Button btnTransferImagesLocally = new Button("Copy images");
        Button btnReplaceImg = new Button("Replace");
        btnReplaceImg.setVisible(false);

        this.remoteStoragePath.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (new File(newValue).exists()) {
                    btnTransferImagesLocally.setVisible(true);
                    btnReplaceImg.setVisible(true);
                    btnOpenFolder.setVisible(true);
                } else {
                    btnOpenFolder.setVisible(false);
                    btnReplaceImg.setVisible(false);
                }
            }
        });

        String selectedB = cbBranches.getSelectionModel().getSelectedItem().toString();
        if (selectedB != "") {
            File branchFile = this.getBranchFullPath(selectedB);
            this.loadFiles(branchFile, this.cbApplications, this.applicationsFoldersProperty);
            if (this.applicationsFoldersProperty.contains(this.selectedApplication)) {
                this.cbApplications.getSelectionModel().select(this.selectedApplication);
                File appFile = this.getSelectedApplication(this.selectedApplication);
                this.loadFiles(appFile, this.listViewDevices, this.listViewDevicesProperty);
                if (this.selectedDevice != null && !this.selectedDevice.isEmpty() && this.listViewDevicesProperty.contains(this.selectedDevice)) {
                    this.listViewDevices.getSelectionModel().select(this.selectedDevice);
                    this.getSelectedStorage(this.selectedDevice);
                }
            }
        }

        btnTransferImagesLocally.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ImageUtils.cleanDirectory(getLocalStorage());
                ImageDownloader.downlaodImages(listViewImageBrowser.getImagesToReplace(), getLocalStorage());
                btnReplaceImg.setVisible(true);
                OSUtils.openFileExplorer(getLocalStorage());
            }
        });

        btnReplaceImg.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                File tempFolderDirectory = new File(getLocalStorage());
                String storage = remoteStoragePath.getValue();

                if (tempFolderDirectory.exists()) {
                    ArrayList<File> filesToReplace = ImageUtils.getFilesFromDirectory(tempFolderDirectory.toPath(), "*.{png}");
                    ImageUtils.replaceFiles(filesToReplace, storage);
                }

                OSUtils.openFileExplorer(storage);
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
        storageFiled.textProperty().bindBidirectional(this.remoteStoragePath);
        hbox.getChildren().addAll(
                this.cbBranches,
                this.cbApplications,
                this.listViewDevices,
                storageFiled,
                btnOpenFolder,
                btnTransferImagesLocally,
                btnReplaceImg);

        TextField textField = new TextField();
        textField.textProperty().bindBidirectional(this.localStorage);

        vBox.getChildren().addAll(hbox, textField);
        mainHbox.getChildren().addAll(vBox);

        return mainHbox;
    }

    private EventHandler<ActionEvent> onBranchChanged() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String branch = cbBranches.getSelectionModel().getSelectedItem().toString();
                File file = getBranchFullPath(branch);
                loadFiles(file, cbApplications, applicationsFoldersProperty);
            }
        };
    }

    private EventHandler<ActionEvent> onApplicationChanged() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String application = cbApplications.getSelectionModel().getSelectedItem().toString();
                File file = getSelectedApplication(application);
                loadFiles(file, listViewDevices, listViewDevicesProperty);
            }
        };
    }

    private EventHandler<ActionEvent> onDeviceSelected() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String device = listViewDevices.getSelectionModel().getSelectedItem().toString();
                getSelectedStorage(device);
            }
        };
    }

    private void loadFiles(File file, ComboBox cb, ListProperty<String> property) {
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
        this.remoteStoragePath.setValue(file.getAbsolutePath());
    }

    private void getSelectedStorage(String device) {
        selectedDevice = this.selectedApplication + File.separator + device;
        File file = new File(selectedDevice);
        if (file.isDirectory() && file.exists()) {
            this.remoteStoragePath.setValue(file.getPath());
        } else {
            this.remoteStoragePath.setValue("");
        }
    }

    private File getBranchFullPath(String branch) {
        String env = System.getenv(STORAGE_ENVIRONMENT_VARIABLE);
        if (env.endsWith("Stable")) {
            env = env.replace("Stable", "");
        }
        if (env.endsWith("Release")) {
            env = env.replace("Release", "");
        }
        File file = new File(env + File.separator + branch + File.separator + "images");
        this.selectedBranch = file.getAbsolutePath();
        this.remoteStoragePath.setValue(this.selectedBranch);
        return file;
    }

    private File getSelectedApplication(String application) {
        this.selectedApplication = selectedBranch + File.separator + application;
        return new File(selectedApplication);
    }
}