package daniel.com.notizapp.file;

/**
 * Created by Tristan on 12.02.2017.
 */

public enum ESortBy {

    NOT_SORTED,
    NAME_ASC,
    NAME_DESC,
    DATE_ASC,
    DATE_DESC,
    WICHTIG_ASC,
    WICHTIG_DESC;

    public static ESortBy getSorting(int i) {
        switch (i) {
            case 1:
                return NAME_ASC;
            case 2:
                return NAME_DESC;
            case 3:
                return DATE_ASC;
            case 4:
                return DATE_DESC;
            case 5:
                return WICHTIG_ASC;
            case 6:
                return WICHTIG_DESC;
            default:
                return NOT_SORTED;
        }
    }
}
