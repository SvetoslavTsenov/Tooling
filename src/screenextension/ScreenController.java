package screenextension;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class ScreenController implements Initializable,
                                            ControlledScreen { 

     ScreensController myController; 

     @Override
     public void initialize(URL url, ResourceBundle rb) {
         // TODO 
     }

     public void setScreenParent(ScreensController screenParent){
        myController = screenParent; 
     }

     //any required method here
 	} 