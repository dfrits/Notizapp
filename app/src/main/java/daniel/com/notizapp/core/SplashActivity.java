package daniel.com.notizapp.core;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import daniel.com.notizapp.R;
import daniel.com.notizapp.util.Util;

import static daniel.com.notizapp.util.Constants.FILE_FOLDER_NAME;
import static daniel.com.notizapp.util.Constants.FOLDER_DEST_SETTING_KEY;
import static daniel.com.notizapp.util.Constants.FOLDER_EXTERNAL_DEST;
import static daniel.com.notizapp.util.Constants.FOLDER_INTERNAL_DEST;

/**
 * Created by Tristan on 13.11.2016.
 */

public class SplashActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;

    private static SharedPreferences pref;
    private static boolean hasExternal;
    private static String internalPath;
    private static String externalPath;
    private static String folderPath;

    /**
     * Einstieg in die App. Setzt den Pfad zum Ordner und verwaltet diesen.
     * @param savedInstanceState .
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            String secStore = System.getenv("SECONDARY_STORAGE");
            File[] dirs = getExternalFilesDirs(null);
            externalPath = "";
            hasExternal = false;
            if (secStore != null) {
                for (File file : dirs) {
                    externalPath = file.getAbsolutePath() + "/" + FILE_FOLDER_NAME;
                    if (externalPath.contains(secStore)) {
                        break;
                    }
                }
                if (!externalPath.isEmpty()) {
                    File dir = new File(externalPath);
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        testPermissionsOnRuntime();
                        hasExternal = hasExternal && (dir.exists() || dir.mkdirs());
                    } else {
                        hasExternal = dir.exists() || dir.mkdirs();
                        testPermissions(dir);
                    }
                }
            }
        } catch (Exception e) {
            // Wenn es nicht klappt auf die SD-Karte zuzugreifen, dann wird angenommen es gibt keine
            hasExternal = false;
            externalPath = "";
        }


        internalPath = getFilesDir().getAbsolutePath() + "/" + FILE_FOLDER_NAME;

        setFolderPath();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            Intent intent1 = new Intent(this, ShareReceiver.class);
            intent1.setAction(action);
            intent1.setType(type);
            intent1.putExtra(Intent.EXTRA_TEXT, intent.getStringExtra(Intent.EXTRA_TEXT));
            startActivity(intent1);
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }

    private void testPermissionsOnRuntime() throws SecurityException {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            //throw new SecurityException();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            //throw new SecurityException();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                hasExternal = grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            }
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                hasExternal = grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            }
            default:
                hasExternal = false;
                break;
        }
    }

    /**
     * Prüft, ob auf die SD-Karte zugegriffen werden kann.
     * @param dir Ordnerpfad
     * @throws Exception Wenn es nicht klappt
     */
    private void testPermissions(File dir) throws Exception {
        final String testText = "testtext";
        File testFolder = new File(dir.getParentFile(), "/testFolder");
        if (!testFolder.exists() && !testFolder.mkdirs()) {
            throw new Exception();
        }
        File testFile = new File(testFolder, "testfile");
        BufferedWriter out = new BufferedWriter(new FileWriter(testFile));
        out.write(testText);
        out.flush();
        out.close();

        StringBuilder text = new StringBuilder();
        BufferedReader in = new BufferedReader(new FileReader(testFile));
        String line;

        while ((line = in.readLine()) != null) {
            text.append(line);
        }
        in.close();

        if (!testFile.delete() && !testFolder.delete() && !text.toString().equals(testText)) {
            throw new Exception();
        }
    }

    /**
     * Setzt den Pfad neu.
     */
    public static void setFolderPath() {
        String dest;
        try {
            dest = pref.getString(FOLDER_DEST_SETTING_KEY, FOLDER_INTERNAL_DEST);
        } catch (Exception e) {
            dest = FOLDER_INTERNAL_DEST;
        }
        if (dest.equals(FOLDER_EXTERNAL_DEST) && hasExternal) {
            folderPath = externalPath;
        } else {
            folderPath = internalPath;
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(FOLDER_DEST_SETTING_KEY, FOLDER_INTERNAL_DEST);
            editor.apply();
        }
    }

    public static boolean hasExternalPath() {
        return hasExternal;
    }

    public static String getFolderPath() {
        setFolderPath();
        return folderPath == null ? internalPath : folderPath;
    }
}
