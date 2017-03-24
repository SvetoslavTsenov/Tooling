package devicemanager;

import screenextension.ControlledScreen;
import screenextension.ScreensController;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DeviceManagerController implements ControlledScreen {

    private DeviceManagerModel deviceManagerModel;
    private ScreensController myController;
    private AndroidManager androidManager;
    private IOSManager iosManager;
    public List<Device> data = new ArrayList<>();

    @FXML
    private TextField textFieldNamespace;

    @FXML
    private ListView devices;

    @FXML
    private ProgressIndicator loadingIndicator;

    public DeviceManagerController() {
        this.androidManager = new AndroidManager();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    @Override
    public void setScreenParent(ScreensController screenParent) {
        this.deviceManagerModel = new DeviceManagerModel();
        this.iosManager = new IOSManager();
        this.loadingIndicator.setVisible(false);

        this.myController = screenParent;
        this.data = new ArrayList<>();
        this.data.addAll(this.androidManager.findAllDevices());
        this.data.addAll(this.iosManager.getAllDevices());

        this.devices.setItems(FXCollections.observableList(this.data));
        this.devices.setPrefSize(200, 250);
        this.devices.setEditable(true);

        this.devices.setCellFactory(new Callback<ListView<Device>, ListCell<Device>>() {

            @Override
            public ListCell<Device> call(ListView<Device> p) {

                ListCell<Device> cell = new ListCell<Device>() {

                    @Override
                    protected void updateItem(Device iDevice, boolean bln) {
                        super.updateItem(iDevice, bln);
                        if (iDevice != null) {
                            setText(String.format("%s %s %s", iDevice.getName(), iDevice.getUidid(), iDevice.getType()));
                        }
                    }

                };

                return cell;
            }
        });


        Bindings.bindBidirectional(this.textFieldNamespace.textProperty(), this.deviceManagerModel.firstProperty());
        this.textFieldNamespace.setText("nativescript");
        this.deviceManagerModel.firstProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
                System.out.println("model old val: " + arg1);
                System.out.println("model new val: " + arg2);
                System.out.println();
            }
        });

        this.textFieldNamespace.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
                System.out.println("textField old val: " + arg1);
                System.out.println("textField new val: " + arg2);
                System.out.println();
            }
        });
    }

    public void handleSubmitButtonAction(ActionEvent actionEvent) {
        loadingIndicator.setVisible(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1); // just emulates some loading time

                    // populates the list view with dummy items
                    String namespace = textFieldNamespace.getText();
                    Device device = (Device) devices.getSelectionModel().getSelectedItem();
                    androidManager.uninstallApps(device.getUidid(), namespace != null ? namespace : "nativescript");
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
}
