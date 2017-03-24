package Main;

import screenextension.ScreensController;
import clientmanger.Browser;
import filemanager.FilesManagerViewController;
import imageview.CompareImagesViewController;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    public static final String IMAGE_CONTROLLER = "../imageview/ImageViewontroller";
    public static final String IMAGE_FXML = "../imageview/imageview.fxml";

    public static final String DEVICE_CONTROLLER = "../devicemanager/DeviceManagerController";
    public static final String DEVICE_FXML = "../devicemanager/devicemanager.view.fxml";

    public static final String WEBVIEW_CONTROLLER = "../clientmanger/WebViewViewController";
    public static final String WEBVIEWVIEW_FXML = "../clientmanger/webviewview.fxml";
    public Browser browser;
    private HBox menu;

    private ScreensController screensController;

    SimpleObjectProperty<Region> mainContainer = new SimpleObjectProperty<>();


//    private HBox toolBar;
//    private static String[] imageFiles = new String[]{
//            "product.png",
//            "blog.png",
//            "documentation.png",
//            "partners.png",
//    };
//    private static String[] captions = new String[]{
//            "Stored Images",
//            "Device manager",
//            "Stable",
//            "Release",
//    };
//    private static String[] urls = new String[]{
//            "http://www.oracle.com/products/index.html",
//            "http://blogs.oracle.com/",
//            "http://nsbuild01.telerik.com:8080/build/view/Stable/",
//            "http://nsbuild01.telerik.com:8080/build/view/Release/",
//    };


    @Override
    public void start(Stage primaryStage) {
        this.screensController = new ScreensController();
        this.screensController.setPrefHeight(1000);
        this.screensController.setPrefWidth(1500);
        this.browser = new Browser();

        this.mainContainer.set(this.browser);

        this.menu = this.addHBox();
        VBox vbox = new VBox(this.menu, this.screensController);
        screensController.setStyle("-fx-background-color: yellow");
        vbox.setStyle("-fx-background-color: green");

        Scene mainScene = new Scene(vbox);
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    public HBox addHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");

        Button buttonImages = new Button("Browser view");
        buttonImages.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                screensController.loadScreen(browser);
                screensController.setScreen(browser.toString());
            }

        });

        Button buttonDeviceManager = new Button("Device Manager");
        buttonDeviceManager.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                screensController.loadScreen(Main.DEVICE_CONTROLLER, Main.DEVICE_FXML);
                screensController.setScreen(Main.DEVICE_CONTROLLER);
            }
        });

        Button btnCompareImages = new Button("Compare Images");
        btnCompareImages.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                CompareImagesViewController compareImagesViewController = new CompareImagesViewController("Stable", "");
                Scene sc = new Scene(compareImagesViewController);
                Stage st = new Stage();
                st.setScene(sc);
                st.show();
            }
        });

        Button btnFilesManager = new Button("Files manager");
        btnFilesManager.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FilesManagerViewController filesManagerViewController = new FilesManagerViewController();
                Scene sc = new Scene(filesManagerViewController);
                Stage st = new Stage();
                st.setScene(sc);
                st.show();
            }
        });


        hbox.getChildren().addAll(buttonImages, buttonDeviceManager, btnCompareImages, btnFilesManager);

        return hbox;
    }

//    private void addStackPane(HBox hbox) {
//        StackPane stack = new StackPane();
//        Rectangle helpIcon = new Rectangle(30.0, 25.0);
//        helpIcon.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
//                new Stop[]{
//                        new Stop(0, Color.web("#4977A3")),
//                        new Stop(0.5, Color.web("#B0C6DA")),
//                        new Stop(1, Color.web("#9CB6CF")),}));
//        helpIcon.setStroke(Color.web("#D0E6FA"));
//        helpIcon.setArcHeight(3.5);
//        helpIcon.setArcWidth(3.5);
//
//        Text helpText = new Text("?");
//        helpText.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
//        helpText.setFill(Color.WHITE);
//        helpText.setStroke(Color.web("#7080A0"));
//
//        stack.getChildren().addAll(helpIcon, helpText);
//        stack.setAlignment(Pos.CENTER_RIGHT);     // Right-justify nodes in stack
//        StackPane.setMargin(helpText, new Insets(0, 10, 0, 0)); // Center "?"
//
//        hbox.getChildren().add(stack);            // Add to HBox from Example 1-2
//        HBox.setHgrow(stack, Priority.ALWAYS);    // Give stack any extra spa
//    }

//    public VBox addVBox() {
//        VBox vbox = new VBox();
//        vbox.setPadding(new Insets(10));
//        vbox.setSpacing(8);
//        vbox.setPrefWidth(1000);
//        vbox.getChildren().add(this.mainContainer);
//        vbox.setVgrow(this.mainContainer, Priority.ALWAYS);
//        return vbox;
//    }


//    public FlowPane addFlowPane() {
//        FlowPane flow = new FlowPane();
//        flow.setPadding(new Insets(5, 0, 5, 0));
//        flow.setVgap(4);
//        flow.setHgap(4);
//        flow.setPrefWrapLength(170); // preferred width allows for two columns
//        flow.setStyle("-fx-background-color: DAE6F3;");
//
//        return flow;
//    }
//
//    private Node addGridPane() {
//        GridPane grid = new GridPane();
//        grid.setHgap(10);
//        grid.setVgap(10);
//        grid.setPadding(new Insets(0, 10, 0, 10));
//
//        // Category in column 2, row 1
//        Text category = new Text("Sales:");
//        category.setFont(Font.font("Arial", FontWeight.BOLD, 20));
//        grid.add(category, 1, 0);
//
//        // Title in column 3, row 1
//        Text chartTitle = new Text("Current Year");
//        chartTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
//        grid.add(chartTitle, 2, 0);
//
//        // Subtitle in columns 2-3, row 2
//        Text chartSubtitle = new Text("Goods and Services");
//        grid.add(chartSubtitle, 1, 1, 2, 1);
//
//        // Right label in column 4 (top), row 3
//        Text servicesPercent = new Text("Services\n20%");
//        GridPane.setValignment(servicesPercent, VPos.TOP);
//        grid.add(servicesPercent, 3, 2);
//
//        return grid;
//    }
}