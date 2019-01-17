package daniel.com.notizapp.file;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import daniel.com.notizapp.util.Util;

/**
 * Created by Daniel on 23.12.2016.
 */

class XMLParser {
    private static final String ns = null;

    /**
     * Parst die Datei und gibt die Daten zurück.
     * @param in Inputstream der Datei
     * @return Entry mit Daten aus der Datei
     * @throws XmlPullParserException
     * @throws IOException
     */
    static Entry parse(InputStream in) throws XmlPullParserException, IOException {
        XmlPullParser parser;
        try {
            parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFile(parser);
        } finally {
            in.close();
        }
    }

    /**
     * Liest die Datei ein.
     * @param parser Parser der Datei
     * @return Entry mit Daten aus der Datei
     * @throws IOException
     * @throws XmlPullParserException
     */
    private static Entry readFile(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, EXMLTags.FILE.getValue());
        String text = "";

        String name = parser.getAttributeValue(ns, EXMLTags.NAME.getValue());
        EWichtigkeit wichtigkeit = transferToWichtigkeit(parser.getAttributeValue(ns, EXMLTags.WICHTIGKEIT.getValue()));
        String date = parser.getAttributeValue(ns, EXMLTags.DATE.getValue());
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tag = parser.getName();
            if (tag.equals(EXMLTags.TEXT.getValue())) {
                text = readText(parser);
            } else {
                skip(parser);
            }
        }
        return new Entry(name, wichtigkeit, text, date);
    }

    /**
     * Ist es nicht der gewollte Tag, dann wird dieser übersprungen.
     * @param parser Parser der Datei
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    /**
     * Formatiert den Codewert in der Datei zum entsprechendem Enumwert.
     * @param value Codewert
     * @return Enum
     * @throws IOException
     * @throws XmlPullParserException
     */
    private static EWichtigkeit transferToWichtigkeit(String value) throws IOException, XmlPullParserException {
        if (value.equals(EWichtigkeit.DRINGEND.getStringCode())) {
            return EWichtigkeit.DRINGEND;
        } else if (value.equals(EWichtigkeit.WICHTIG.getStringCode())) {
            return EWichtigkeit.WICHTIG;
        } else if (value.equals(EWichtigkeit.HAT_ZEIT.getStringCode())) {
            return EWichtigkeit.HAT_ZEIT;
        } else {
            return EWichtigkeit.STANDARD;
        }
    }

    /**
     * Liest den Text vom Tag.
     * @param parser Parser der Datei
     * @return Text
     * @throws IOException
     * @throws XmlPullParserException
     */
    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, EXMLTags.TEXT.getValue());
        String s = parser.nextText();
        parser.require(XmlPullParser.END_TAG, ns, EXMLTags.TEXT.getValue());
        return s;
    }

    /**
     * Speichert die Datei ab. Text-Dateien werden in XML-Format konvertiert.
     * @param file Zu speichernde Datei
     * @return True, bei Erfolg
     * @throws IllegalArgumentException Wenn die Datei weder .txt noch .xml Datei ist
     */
    static boolean write(NotizFile file) {
        XmlSerializer serializer = Xml.newSerializer();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(file.getPath()), false))) {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", null);
            serializer.startTag(ns, EXMLTags.FILE.getValue());
            serializer.attribute(ns, EXMLTags.NAME.getValue(), file.getName());
            serializer.attribute(ns, EXMLTags.WICHTIGKEIT.getValue(), file.getWichtigkeit().getStringCode());
            serializer.attribute(ns, EXMLTags.DATE.getValue(), Util.getCurrentDate());
            serializer.startTag(ns, EXMLTags.TEXT.toString());
            serializer.text(file.getText());
            serializer.endTag(ns, EXMLTags.TEXT.toString());
            serializer.endTag(ns, EXMLTags.FILE.toString());
            serializer.endDocument();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Speichert die eingelesenen Daten, damit diese leichter weiterverabeitet werden können.
     */
    static class Entry {
        String name;
        EWichtigkeit wichtigkeit;
        String text;
        String date;

        Entry(String name, EWichtigkeit wichtigkeit, String text, String date) {
            this.name = name;
            this.wichtigkeit = wichtigkeit;
            this.text = text;
            this.date = date;
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

        public String getDate() {
            return date;
        }
    }
}