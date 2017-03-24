package imageview;

public class ImagesModel {

    private String actual;
    private String expected;
    private String difff;

    public ImagesModel(String actual, String expected, String diff) {
        this.actual = actual;
        this.expected = expected;
        this.difff = diff;
    }

    public String getActual(){
        return this.actual;
    }

    public String getExpected(){
        return  this .expected;
    }

    public String getDiff(){
        return this.difff;
    }

}
