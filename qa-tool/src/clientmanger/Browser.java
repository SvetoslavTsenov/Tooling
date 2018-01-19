package clientmanger;

import imageview.ImageViewController;
import imageview.ImageViewModel;
import imageview.ImagesHolder;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.event.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.web.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import screenextension.ControlledScreen;
import screenextension.ScreensController;
import utils.ImageDownloader;
import utils.ImageUtils;
import utils.OSUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Browser extends HBox implements ControlledScreen {

    private static String lastMousePositionSavedImageDirectory;

    private static String[] imageFiles = new String[]{
            "stable.png",
            "release.png",
    };

    private static String[] captions = new String[]{
            "Stable",
            "Release",
    };

    private static String[] urls = new String[]{
            "http://nsbuild01.telerik.com:8080/build/view/Stable/",
            "http://nsbuild01.telerik.com:8080/build/view/Release/",
            "http://nsbuild01.telerik.com:8080/build/",
    };

    private final Image[] images = new Image[imageFiles.length];
    private final WebView browser = new WebView();
    private final WebEngine webEngine = browser.getEngine();
    private final Button showPrevDoc = new Button("main");
    private final WebView smallView = new WebView();
    private final ComboBox cbBrowserHistory = new ComboBox();
    private final HBox browserActionBar;
    private final HBox browserToolBar;
    private VBox menuBar;
    private Stage compareImagesWindow;
    private Stage imagesListViewWindow;
    private ImageViewController imageViewController;

    private ArrayList<String> imagesList = new ArrayList<>();
    private final StringProperty addressBar = new SimpleStringProperty();
    private SimpleDoubleProperty menuWidth = new SimpleDoubleProperty();
    private String currentMousePosition = "";

    private boolean isExpanded = false;

    public Browser() {

        this.browserActionBar = this.createActionBar();
        this.browserToolBar = this.createBottomBar();
        //java.net.CookieHandler.setDefault(null);
        this.menuBar = this.createSideMenu();
        this.menuBar.setVisible(false);
        this.menuBar.setPrefWidth(0);
        this.showPrevDoc.setOnAction(new EventHandler() {
            @Override
            public void handle(Event t) {
                webEngine.executeScript("toggleDisplay('PrevRel')");
            }
        });

        //handle popup windows
        this.webEngine.setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>() {
            @Override
            public WebEngine call(PopupFeatures config) {
                smallView.setFontScale(0.8);
//                if (!browserToolBar.getChildren().contains(smallView)) {
//                    browserToolBar.getChildren().add(smallView);
//                }
                return null;
            }
        });

        this.webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
            @Override
            public void handle(WebEvent<String> event) {
                String data = event.getData();
                System.out.println("data: " + data);
                currentMousePosition = data;

                EventType eventType = event.getEventType();
                System.out.println("eventType: " + eventType);

                EventTarget eventtarget = event.getTarget();
                System.out.println("eventType: " + eventtarget);

                Object source = event.getSource();
                System.out.println("eventType: " + source);
            }
        });

        //process history
        final WebHistory history = webEngine.getHistory();
        history.getEntries().addListener(new ListChangeListener<WebHistory.Entry>() {
            @Override
            public void onChanged(Change<? extends WebHistory.Entry> c) {
                c.next();
                imagesList.clear();
                currentMousePosition = null;
                for (WebHistory.Entry e : c.getRemoved()) {
                    String url = e.getUrl();
                    cbBrowserHistory.getItems().remove(url);
                }
                for (WebHistory.Entry e : c.getAddedSubList()) {
                    String url = e.getUrl();
                    addressBar.setValue(url);
                    cbBrowserHistory.getItems().add(url);
                    String[] urlEl = url.split("/");
                    String number = urlEl[urlEl.length - 1];
                    if (tryParseInt(number)) {
                        imagesList.clear();
                        webEngine.load(url + "artifact/target/surefire-reports/html/index.html");
                    }
                }
            }
        });

        //set the behavior for the history combobox
        this.cbBrowserHistory.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ev) {
                int offset =
                        cbBrowserHistory.getSelectionModel().getSelectedIndex()
                                - history.getCurrentIndex();
                history.go(offset);
            }
        });


        this.browser.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY && event.isAltDown()) {
                    if (currentMousePosition != null && currentMousePosition.endsWith(".png") && !imagesList.contains(currentMousePosition)) {
                        imagesList.add(currentMousePosition);
                        event.consume();
                        createLisViewImageBrowserWindow(ImageViewModel.convertListOfUrlsToListOfIMageViewModels(imagesList));
                    }
                } else if (event.getButton() == MouseButton.PRIMARY && !event.isAltDown()
                        && currentMousePosition != null && currentMousePosition.endsWith("_actual.png")
                        && compareImagesWindow == null) {
                    Node node = (Node) event.getSource();
                    createPopupContent();
                } else {
                    if (compareImagesWindow != null) {
                        compareImagesWindow.close();
                        compareImagesWindow = null;
                    }
                }
            }
        });

        this.webEngine.load("http://nsbuild01.telerik.com:8080/build/view/Stable/");

        GridPane gr = new GridPane();
        gr.add(this.browserActionBar, 0, 0);
        gr.add(this.browser, 0, 1);
        gr.setHgrow(this.browser, Priority.ALWAYS);
        gr.setVgrow(this.browser, Priority.ALWAYS);
        setHgrow(gr, Priority.ALWAYS);
        gr.add(this.browserToolBar, 0, 2);

        this.getChildren().addAll(this.menuBar, gr);
    }

    public static boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    @Override
    public void setScreenParent(ScreensController screenPage) {
        this.prefWidthProperty().bind(screenPage.widthProperty());
        this.prefHeightProperty().bind(screenPage.heightProperty());
    }

    @Override
    protected double computePrefWidth(double height) {
        return 1600;
    }

    @Override
    protected double computePrefHeight(double width) {
        return 1000;
    }

    private VBox createSideMenu() {
        VBox vbox = new VBox();
        Button btnGetAllImages = new Button("Get all images form build");
        btnGetAllImages.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                imagesList.addAll(ClientManager.getImagesFromArtifacts(browser.getEngine().getLocation()));
                createLisViewImageBrowserWindow(ImageViewModel.convertListOfUrlsToListOfIMageViewModels(imagesList));
            }
        });

        Button btnPreviewImages = new Button("Preview");

        btnPreviewImages.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (getTempFolder() != null) {
                    File directory = new File(getTempFolder());
                    if (directory.exists()) {
                        createLisViewImageBrowserWindow(ImageUtils.getImageViewModelsFromDirectory(directory.toPath(), "*.{jpg,jpeg,png,JPG,JPEG,PNG}"));
                    }
                } else {
                    createLisViewImageBrowserWindow(new ArrayList<>());
                }
            }
        });

        this.menuWidth.setValue(btnGetAllImages.getWidth());
        vbox.getChildren().addAll(btnGetAllImages, btnPreviewImages);

        return vbox;
    }

    private HBox createBottomBar() {
        HBox toolBar = new HBox();
        Hyperlink[] hpls = new Hyperlink[captions.length];
        for (int i = 0; i < captions.length; i++) {
            // create hyperlinks
            Hyperlink hpl = hpls[i] = new Hyperlink(captions[i]);
//            Image image = images[i] =
//                    new Image(getClass().getResourceAsStream(imageFiles[i]));
//            hpl.setGraphic(new imageview(image));
            final String url = urls[i];
//            final boolean addButton = (hpl.getText().equals("Stable") || hpl.getText().equals("Release"));
//
//            // process event
//            hpl.setOnAction(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent e) {
//                    needDocumentationButton = addButton;
//                    webEngine.load(url);
//                }
//            });
        }

        this.smallView.setPrefSize(120, 80);

        toolBar.setAlignment(Pos.CENTER);
        toolBar.getChildren().addAll(hpls);
        toolBar.getChildren().add(this.createSpacer());

        return toolBar;
    }

    private HBox createActionBar() {
        //Creating a GridPane container
        HBox hBox = new HBox();
        TextField name = new TextField();
        name.textProperty().bindBidirectional(this.addressBar);
        name.setPrefWidth(800);
        name.setPromptText("enter url");
        Button submit = new Button("Load");
        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String url = name.getText();
                if (!url.isEmpty()) {
                    if (!url.startsWith("http")) {
                        url = "http://" + url;
                    }
                    webEngine.load(url);
                }
            }
        });
        this.cbBrowserHistory.setPrefWidth(60);

        Button btnExpand = new Button("...");

        btnExpand.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                isExpanded = !isExpanded;
                if (isExpanded) {
                    menuBar.setPrefWidth(150);
                    menuBar.setVisible(true);
                } else {
                    menuBar.setPrefWidth(0);
                    menuBar.setVisible(false);
                }
            }
        });

        hBox.getChildren().add(btnExpand);
        hBox.getChildren().add(this.cbBrowserHistory);
        hBox.getChildren().add(name);
        hBox.getChildren().add(submit);
        hBox.getChildren().add(this.createSpacer());

        return hBox;
    }

    private Node createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private void createPopupContent() {
        lastMousePositionSavedImageDirectory = this.currentMousePosition;
        ImagesHolder imagesHolder = ImagesHolder.convertImageUrlToImagesHolder(this.currentMousePosition);

        final Button btnSaveLocal = new Button("Save local");
        final VBox wizBox = new VBox(10);

        wizBox.setAlignment(Pos.TOP_LEFT);
        wizBox.getChildren().setAll(
                imagesHolder,
                btnSaveLocal
        );
        btnSaveLocal.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                ArrayList<ImageViewModel> imageViewModels = new ArrayList<>();
                imageViewModels.add(imagesHolder.getImageViewModel());
                String tempFolder = getTempFolder();
                ImageDownloader.downlaodImages(imageViewModels, tempFolder);
                OSUtils.openFileExplorer(tempFolder);
                compareImagesWindow.close();
            }
        });

        this.compareImagesWindow = new Stage();

        this.compareImagesWindow.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (compareImagesWindow != null) {
                    compareImagesWindow.close();
                    compareImagesWindow = null;
                }
            }
        });

        imagesHolder.prefHeightProperty().bind(this.compareImagesWindow.heightProperty());
        imagesHolder.prefWidthProperty().bind(this.compareImagesWindow.widthProperty());
        wizBox.prefWidth(1200);
        wizBox.prefHeight(1000);
        Scene scene = new Scene(wizBox);
        this.compareImagesWindow.setMinWidth(1000);
        this.compareImagesWindow.setHeight(1000);
        this.compareImagesWindow.setScene(scene);
        this.compareImagesWindow.show();
    }

    private void createLisViewImageBrowserWindow(ArrayList<ImageViewModel> images) {
        if (this.imagesListViewWindow == null) {
            String branch = "";
            String appliaction = "";
            if (this.currentMousePosition != null) {
                branch = this.currentMousePosition.toLowerCase().contains("stable") ? "Stable" : "Release";
                if (this.currentMousePosition.toLowerCase().contains("uitests")) {
                    appliaction = "uitests";
                }
            }

            this.imageViewController = new ImageViewController(branch, appliaction, "");
            this.imageViewController.setLocalStorage(this.getTempFolder());

            Scene sc = new Scene(this.imageViewController);
            this.imagesListViewWindow = new Stage();
            this.imagesListViewWindow.setScene(sc);
            this.imagesListViewWindow.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    imageViewController = null;
                    imagesList.clear();
                    imagesListViewWindow.close();
                    imagesListViewWindow = null;
                }
            });
            //Fill stage with content
            this.imagesListViewWindow.show();
            this.imageViewController.update(images);
        } else {
            //this.imageViewController.clear();
            this.imageViewController.update(ImageViewModel.convertUrlToImageViewModel(this.currentMousePosition));
        }

        // Hide this current window (if this is what you want)
        // ((Node)(event.getSource())).getScene().getWindow().hide();
    }

    private String getTempFolder() {
        String tempFolder = "/Users/tsenov/Downloads/temp";

        if (this.currentMousePosition == null || !this.currentMousePosition.contains("job")) {
            this.currentMousePosition = lastMousePositionSavedImageDirectory;
        } else {
            if (this.currentMousePosition.contains("Stable")) {
                tempFolder += File.separator + "Stable";
            }
            if (this.currentMousePosition.contains("Release")) {
                tempFolder += File.separator + "Stable";
            }
        }

        try {
            String job = this.currentMousePosition.substring(this.currentMousePosition.lastIndexOf("job") + 3, this.currentMousePosition.indexOf("artifact"));

            return tempFolder + File.separator + job;
        } catch (Exception e) {
            return this.currentMousePosition != null ? this.currentMousePosition : tempFolder;
        }
    }
}


