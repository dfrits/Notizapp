package daniel.com.notizapp.file;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;

import daniel.com.notizapp.R;
import daniel.com.notizapp.util.Util;

import static daniel.com.notizapp.file.EWichtigkeit.STANDARD;

/**
 * Created by Daniel on 23.12.2016.
 */

public class NotizFile implements Serializable, Comparable<NotizFile> {
    public static final String NO_NAME_SET = "#NO_NAME_SET";
    private static ESortBy sortBy = ESortBy.NOT_SORTED;

    private EWichtigkeit wichtigkeit;
    private boolean isNew;
    private String path;
    private String name;
    private String text;
    private String date;

    /**
     * Bei neuer Datei werden Standardwerte gesetzt, sonst wird Datei eingelesen.
     * @param path  Pfad der Datei
     * @param isNew Angabe, ob neue Notiz erstellt wird
     */
    public NotizFile(String path, boolean isNew) {
        this.path = path;
        this.isNew = isNew;
        if (isNew) {
            wichtigkeit = EWichtigkeit.STANDARD;
            name = NO_NAME_SET;
            text = "";
            date = Util.getCurrentDate();
        } else {
            init();
        }
    }

    /**
     * Einlesen der Datei. Ist die Datei eine txt-Datei so sind Name und Text gleich, sonst werden
     * abgespeicherte Daten eingelesen und gesetzt.
     */
    private void init() {
        try {
            if (path.endsWith("txt")) {
                wichtigkeit = STANDARD;
                text = Util.readTextFromFile(new File(path));
                name = NO_NAME_SET;
            } else if (path.endsWith("xml")) {
                InputStream in = new FileInputStream(new File(path));
                XMLParser.Entry entry = XMLParser.parse(in);
                text = entry.getText();
                name = entry.getName();
                wichtigkeit = entry.getWichtigkeit();
                date = entry.getDate();
                in.close();
            } else {
                throw new IllegalArgumentException("No XML- or TXT-File");
            }
        } catch (Exception e) {
            name = NO_NAME_SET;
            wichtigkeit = EWichtigkeit.STANDARD;
            text = "";
            date = "";
        }
    }

    /**
     * Speichert die Datei ab.
     * @return True bei Erfolg
     * @throws IllegalArgumentException Wenn die Datei weder .txt noch .xml Datei ist
     */
    public boolean safeFile() throws IllegalArgumentException {
        if (text.isEmpty()) {
            return false;
        }
        if (path.endsWith("txt")) {
            path = path.substring(0, path.lastIndexOf(".txt")) + ".xml";
        }

        return XMLParser.write(this);
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public EWichtigkeit getWichtigkeit() {
        return wichtigkeit;
    }

    public String getText() {
        return text;
    }

    public boolean isNew() {
        return isNew;
    }

    public static ESortBy sortedBy() {
        return sortBy;
    }

    public String getDate() {
        return date;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setWichtigkeit(EWichtigkeit wichtigkeit) {
        this.wichtigkeit = wichtigkeit;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setText(String text) {
        this.text = text;
    }

    public static void setSort(ESortBy newSortOrder) {
        sortBy = newSortOrder;
    }

    public boolean delete() {
        return new File(path).delete();
    }

    public boolean exists() {
        return new File(path).exists();
    }

    @Override
    public int compareTo(@NonNull NotizFile otherFile) {
        switch (sortBy) {
            case NAME_ASC:
                return compareName(sortBy, this, otherFile);
            case NAME_DESC:
                return compareName(sortBy, this, otherFile);
            case DATE_ASC:
                return compareDate(sortBy, this, otherFile);
            case DATE_DESC:
                return compareDate(sortBy, this, otherFile);
            case WICHTIG_ASC:
                return compareImportance(sortBy, this, otherFile);
            case WICHTIG_DESC:
                return compareImportance(sortBy, this, otherFile);
            default:
                return 0;
        }
    }

    private int compareImportance(ESortBy sortBy, NotizFile file1, @NonNull NotizFile file2) {
        if (file1.getWichtigkeit() == null || file2.getWichtigkeit() == null) {
            return 0;
        }

        if (sortBy == ESortBy.WICHTIG_ASC) {
            if (file1.getWichtigkeit().getIntCode() <= file2.getWichtigkeit().getIntCode()) {
                return -1;
            } else {
                return 1;
            }
        } else {
            if (file1.getWichtigkeit().getIntCode() <= file2.getWichtigkeit().getIntCode()) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    private int compareDate(ESortBy sortBy, NotizFile file1, @NonNull NotizFile file2) {
        if (file1.getDate() == null || file2.getDate() == null) {
            return 0;
        }

        if (sortBy == ESortBy.DATE_ASC) {
            return file2.getDate().compareTo(file1.getDate());
        } else {
            return file1.getDate().compareTo(file2.getDate());
        }

    }

    private int compareName(ESortBy sortBy,@NonNull  NotizFile file1,@NonNull  NotizFile file2) {
        String nameFile1 = file1.getName().equals(NotizFile.NO_NAME_SET) ? file1.getText() : file1.getName();
        String nameFile2 = file2.getName().equals(NotizFile.NO_NAME_SET) ? file2.getText() : file2.getName();

        if (nameFile1 == null || nameFile2 == null) {
            return 0;
        }

        if (sortBy == ESortBy.NAME_ASC) {
            return nameFile1.compareToIgnoreCase(nameFile2);
        } else {
            return nameFile2.compareToIgnoreCase(nameFile1);
        }
    }
}
