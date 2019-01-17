package daniel.com.notizapp.file;

import android.graphics.Color;

import daniel.com.notizapp.R;

/**
 * Created by Daniel on 23.12.2016.
 */

public enum EWichtigkeit {
    DRINGEND(0, "0", Color.RED),
    WICHTIG(1, "1", Color.YELLOW),
    HAT_ZEIT(2, "2", Color.GREEN),
    STANDARD(3, "3", Color.TRANSPARENT);

    private String code_string;
    private int code_int;
    private int color;

    /**
     * @param i1 Codewert als int
     * @param s  Codewert als String
     * @param i  Farbwert
     */
    EWichtigkeit(int i1, String s, int i) {
        code_int = i1;
        code_string = s;
        color = i;
    }

    public int getColor() {
        return color;
    }

    public String getStringCode() {
        return code_string;
    }

    public int getIntCode() {
        return code_int;
    }

    public int getStringResource() {
        switch (code_string) {
            case "2":
                return R.string.hat_zeit;
            case "1":
                return R.string.wichtig;
            case "0":
                return R.string.dringend;
            default:
                return R.string.zurueck_setzen;
        }
    }
}
