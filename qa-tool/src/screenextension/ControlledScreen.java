package screenextension;

import java.net.URL;
import java.util.ResourceBundle;

public interface ControlledScreen {

    void initialize(URL url, ResourceBundle rb);

    //This method will allow the injection of the Parent ScreenPane
    public void setScreenParent(ScreensController screenPage);
}