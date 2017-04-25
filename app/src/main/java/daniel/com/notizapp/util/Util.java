package daniel.com.notizapp.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import daniel.com.notizapp.R;
import daniel.com.notizapp.core.NotizActivity;
import daniel.com.notizapp.core.SplashActivity;
import daniel.com.notizapp.file.NotizFile;

/**
 * Created by Tristan on 29.06.2016.
 */
public class Util {

    /**
     * Erstellt einen Intent mit einer neuen Datei .
     * @param context Context der aufrufenden Activity
     * @return Intent
     */
    @NonNull
    public static Intent getNewNoticeIntent(Context context) {
        Intent newNotice = new Intent(context, NotizActivity.class);
        newNotice.putExtra(Constants.FILE_EXTRA_KEY,
                new NotizFile(Util.generatePath(), true));
        newNotice.putExtra(Constants.IS_NEW_FILE, true);
        return newNotice;
    }

    /**
     * Setzt die Farbe der Titelleiste je nach Einstellung
     */
    public static void setTitlebarColor(AppCompatActivity activity) {
        Context context = activity.getApplicationContext();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        ActionBar bar = activity.getSupportActionBar();
        int color = ContextCompat.getColor(context, R.color.colorPrimaryDark);
        int prefInt = pref.getInt(Constants.TITLEBAR_COLOR_SETTING_KEY, color);
        ColorDrawable colorDrawable = new ColorDrawable(prefInt);
        if (bar != null) {
            bar.setBackgroundDrawable(colorDrawable);
        }
    }

    /**
     * Zeigt den Ladedialog und verschiebt die Dateien.
     * @param oldPath Der alte Pfad
     * @param newPath Der neue Pfad
     * @param context Der Context der Activity
     * @param message Nachricht, die neben der Ladeanzeige angezeigt werden soll
     */
    public static void moveFiles(final String oldPath, final String newPath, final Context context, String message) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.show();
        Thread thread = new Thread(new Runnable() {
            public void run() {
                File oldFolder = new File(oldPath);
                final File[] files = oldFolder.listFiles();
                for (File file : files) {
                    moveFile(file, newPath, context);
                }
                progressDialog.dismiss();
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Hilfsmethode. Verschiebt die Datei.
     * @param inputFile  Datei die verschoben werden soll
     * @param outputPath Zielpfad
     */
    private static void moveFile(File inputFile, String outputPath, Context context) {
        InputStream in;
        OutputStream out;
        try {
            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Toast.makeText(context, R.string.cant_move_file, Toast.LENGTH_SHORT).show();
                    return;
                }
            }


            in = new FileInputStream(inputFile);
            out = new FileOutputStream(new File(Util.generatePath(outputPath)));

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file
            out.flush();
            out.close();

            // delete the original file
            inputFile.delete();


        } catch (Exception ignored) {
        }
    }

    /**
     * Sucht einen freien Namen in dem aktuellen Pfad.
     * @return Pfad mit Dateiname
     */
    public static String generatePath() {
        String filePath = SplashActivity.getFolderPath() + "/Notiz";
        String newPath = filePath + "0.xml";
        File file = new File(newPath);
        for (int i = 1; file.exists(); i++) {
            newPath = filePath + i + ".xml";
            file = new File(newPath);
        }
        return newPath;
    }

    /**
     * Sucht einen freien Namen in dem übergebenen Pfad.
     * @param dirPath Pfad eines Ordners
     * @return Pfad mit Dateiname
     */
    private static String generatePath(String dirPath) {
        String filePath = dirPath + "/Notiz";
        String newPath = filePath + "0.xml";
        File file = new File(newPath);
        for (int i = 1; file.exists(); i++) {
            newPath = filePath + i + ".xml";
            file = new File(newPath);
        }
        return newPath;
    }

    /**
     * Erzeugt ein Intent mit Der Datei zum teilen.
     * @param fileToShare      Datei, die geteilt werden soll
     * @param externalFilesDir Muss übergeben werden, weil es hier nicht erzeugt werden kann
     * @return Intent für den Chooser
     */
    public static Intent createShareFileIntent(NotizFile fileToShare, File externalFilesDir) {
        TempFile tempFile = createTempFile(fileToShare, externalFilesDir, 0);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "TestSubject");
        intent.putExtra(Intent.EXTRA_TEXT, tempFile.getText().trim());
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tempFile.getFile()));
        return intent;
    }

    /**
     * Erzeugt ein Intent mit den Dateien zum teilen.
     * @param filesToShare     Liste von Dateien, die geteilt werden sollen
     * @param externalFilesDir Muss übergeben werden, weil es hier nicht erzeugt werden kann
     * @return Intent für den Chooser
     */
    public static Intent createShareFilesIntent(List<NotizFile> filesToShare, File externalFilesDir) {
        ArrayList<Uri> uris = new ArrayList<>();
        for (int i = 0; i < filesToShare.size(); i++) {
            NotizFile fileToShare = filesToShare.get(i);
            TempFile tempFile = createTempFile(fileToShare, externalFilesDir, i + 1);
            uris.add(Uri.fromFile(tempFile.getFile()));
        }
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "testetset");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        return intent;
    }

    /**
     * Erzeugt eine temporäre Datei um diese Teilen zu können.
     * @param fileToShare      Datei die geteilt werden soll
     * @param externalFilesDir Pfad zum externen Speicher
     * @param number           Nummer der Datei
     * @return Temporäre Datei
     */
    private static TempFile createTempFile(NotizFile fileToShare, File externalFilesDir, int number) {
        File file = number == 0 ? new File(externalFilesDir, "Shared_Notice.txt") : new File(externalFilesDir, "Shared_Notice_" + number + ".txt");
        try {
            FileWriter wr = new FileWriter(file);

            wr.write(fileToShare.getText());
            wr.close();
        } catch (IOException ignored) {
        }
        return new TempFile(file, fileToShare.getText());
    }

    /**
     * Beeinhaltet Datei und Text
     */
    private static class TempFile {
        private final File file;
        private final String text;

        TempFile(File pFile, String pText) {
            file = pFile;
            text = pText;
        }

        public File getFile() {
            return file;
        }

        public String getText() {
            return text;
        }
    }

    /**
     * Files auslesen
     * @return Liste mit Files
     */
    public static List<NotizFile> getAllNotices(Context context) {
        File folder = new File(SplashActivity.getFolderPath());
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Toast.makeText(context, R.string.ordner_erstellen_fehler, Toast.LENGTH_SHORT).show();
                return new ArrayList<>();
            }
        }

        List<NotizFile> list = new ArrayList<>();
        for (File file : folder.listFiles()) {
            list.add(new NotizFile(file.getAbsolutePath(), false));
        }

        return list;
    }

    /**
     * Liest den Text von der Datei ein und gibt ihn als String zurück.
     * Es wird der komplette Inhalt ungeschnitten zurückgegeben, daher sollten nur "*.txt"-Dateien
     * verwendet werden.
     * @param file Datei, aus der gelesen werden soll
     * @return Kompletter Dateiinhalt
     * @throws IOException
     */
    @NonNull
    public static String readTextFromFile(File file) throws IOException {
        BufferedReader in;
        StringBuilder title = new StringBuilder();
        String line;
        in = new BufferedReader(new FileReader(file));

        while ((line = in.readLine()) != null) {
            title.append(line).append("\n");
        }
        in.close();
        return title.toString();
    }

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH.mm.ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}
