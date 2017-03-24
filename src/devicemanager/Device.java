package devicemanager;

public class Device {
    private String name;
    private String uidid;
    private DeviceType type;

    public Device(String uidid, String name, DeviceType type) {
        this.name = name;
        this.uidid = uidid;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public String getUidid() {
        return this.uidid;
    }

    public DeviceType getType() {
        return this.type;
    }

}
