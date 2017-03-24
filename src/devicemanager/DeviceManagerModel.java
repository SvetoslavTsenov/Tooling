package devicemanager;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DeviceManagerModel {

    StringProperty first = new SimpleStringProperty();
    //getter

    public String getFirst() {
        return first.get();
    }
    //setter

    public void setFirst(String first) {
        this.first.set(first);
    }
    //new "property" accessor

    public StringProperty firstProperty() {
        return first;
    }


    private final ObservableList<Device> devices =
            FXCollections.observableArrayList();

    public ReadOnlyObjectProperty<ObservableList<Device>> devicesProperty() {
        return new SimpleObjectProperty<>(devices);
    }

}
