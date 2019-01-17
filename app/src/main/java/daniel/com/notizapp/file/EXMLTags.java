package daniel.com.notizapp.file;

/**
 * Created by Daniel on 24.12.2016.
 */

enum EXMLTags {
    FILE("FILE"),
    NAME("NAME"),
    WICHTIGKEIT("WICHTIGKEIT"),
    TEXT("TEXT"),
    DATE("DATE");

    private String value;

    EXMLTags(String s) {
        value = s;
    }

    public String getValue() {
        return value;
    }
}