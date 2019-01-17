package daniel.com.notizapp.core;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;

import daniel.com.notizapp.file.EWichtigkeit;
import daniel.com.notizapp.file.NotizFile;
import daniel.com.notizapp.util.Constants;
import daniel.com.notizapp.R;
import daniel.com.notizapp.setting.SettingsActivity;
import daniel.com.notizapp.util.Util;

/**
 * Created by Tristan on 30.04.2016.
 */

public class NotizActivity extends AppCompatActivity {
    public static final int STANDART_INPUT_TYPE = InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE;
    public static final int INPUT_TYPE_CAPITAL = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | STANDART_INPUT_TYPE;
    private final Context context = this;

    private boolean deleted = false;
    private NotizFile notizFile;
    private EditText textField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_editor);

        textField = (EditText) findViewById(R.id.editText);

        Intent intent = getIntent();
        notizFile = (NotizFile) intent.getSerializableExtra(Constants.FILE_EXTRA_KEY);

        String sharedText = intent.getStringExtra(Constants.SHARED_TEXT_KEY);
        if (sharedText == null) sharedText = "";

        if (!notizFile.isNew()) {
            textField.setText((notizFile.getText() + sharedText).replaceAll("\\s+$", ""));
        } else {
            textField.setText(sharedText);

            textField.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager keyboard = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.showSoftInput(textField, 0);
                }
            }, 200);
        }
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * Sobald die Activity beendet oder pausiert wird, wird die Datei gespeichert.
     */
    @Override
    protected void onPause() {
        super.onPause();
        String text = textField.getText().toString().replaceAll("\\s+$", "");

        if (!deleted && (!notizFile.isNew() || !text.isEmpty())) {
            String oldText = notizFile.getText();
            try {
                saveFile();
            } catch (Exception e) {
                try {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(text, text);
                    clipboard.setPrimaryClip(clip);
                    restorOldFile(oldText);
                    Toast.makeText(this, R.string.datei_speichern_fehler, Toast.LENGTH_LONG).show();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Stellt die alte Datei wieder her. Gibt es keine oder keinen Text, dann wird sie gelöscht.
     * @param oldText Der alte Text
     */
    private void restorOldFile(String oldText) {
        if (oldText != null && !oldText.isEmpty()) {
            notizFile.setText(oldText);
            notizFile.safeFile();
        } else {
            notizFile.delete();
        }
    }

    /**
     * Speichert die Datei ab und löscht die alte. Es wird nur geprüft, ob der Pfad noch stimmt,
     * sonst nichts.
     * @throws Exception Wenn Datei nicht gelöscht werden kann oder das Speichern fehlschlägt.
     *                   Dabei wird kein Text gesichert oder die alte Datei wiederhergestellt.
     */
    private boolean saveFile() throws Exception {
        String text = textField.getText().toString().replaceAll("\\s+$", "");
        //text = text.replaceAll("�", "");

        deleteFileIfNotNew();

        File file = new File(notizFile.getPath());
        if (!SplashActivity.getFolderPath().equals(file.getParent())) {
            notizFile.setPath(Util.generatePath());
        }

        notizFile.setText(text);
        return notizFile.safeFile();
    }

    /**
     * Datei löschen, aber nur wenn sie noch nicht existieren.
     * @throws Exception Wenn Datei nicht gelöscht werden kann.
     */
    private void deleteFileIfNotNew() throws Exception {
        if (!notizFile.isNew()) {
            if (!notizFile.delete()) {
                throw new Exception("Can't delete File!");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menubuttons_note_acitvity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder builder;
        AlertDialog dialog;
        switch (item.getItemId()) {
            case R.id.save:
                finish();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.share:
                Intent intent = Util.createShareFileIntent(context, notizFile, getExternalFilesDir(null));

                if (intent == null) {
                    intent = new Intent(Intent.ACTION_SEND);
                    intent.setType(Constants.SHARE_TEXT_TYPE);
                    intent.putExtra(Intent.EXTRA_TEXT, textField.getText().toString());
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_title)));
                return true;
            case R.id.wichtigkeit:
                builder = new AlertDialog.Builder(context);
                CharSequence[] sequence = new CharSequence[4];
                sequence[0] = getString(EWichtigkeit.DRINGEND.getStringResource());
                sequence[1] = getString(EWichtigkeit.WICHTIG.getStringResource());
                sequence[2] = getString(EWichtigkeit.HAT_ZEIT.getStringResource());
                sequence[3] = getString(EWichtigkeit.STANDARD.getStringResource());
                builder.setSingleChoiceItems(sequence,
                        notizFile.getWichtigkeit().getIntCode(),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        notizFile.setWichtigkeit(EWichtigkeit.DRINGEND);
                                        break;
                                    case 1:
                                        notizFile.setWichtigkeit(EWichtigkeit.WICHTIG);
                                        break;
                                    case 2:
                                        notizFile.setWichtigkeit(EWichtigkeit.HAT_ZEIT);
                                        break;
                                    case 3:
                                        notizFile.setWichtigkeit(EWichtigkeit.STANDARD);
                                        break;
                                }
                                dialog.cancel();
                            }
                        });
                dialog = builder.create();
                dialog.show();
                return true;
            case R.id.naming:
                builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.name_file_dialog_message);
                LayoutInflater inflater = getLayoutInflater();
                builder.setView(inflater.inflate(R.layout.layout_naming_dialog, null));
                builder.setPositiveButton(R.string.ok_button_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditText editText = (EditText) ((AlertDialog) dialog).findViewById(R.id.naming_textfield);
                        if (editText.getText().toString().isEmpty()) {
                            notizFile.setName(NotizFile.NO_NAME_SET);
                        } else {
                            notizFile.setName(editText.getText().toString());
                        }
                    }
                });
                builder.setNeutralButton(R.string.delete_notice, null);
                builder.setNegativeButton(R.string.cancel_button_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                dialog = builder.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        final EditText editText = (EditText) ((AlertDialog) dialog).findViewById(R.id.naming_textfield);
                        editText.setText(notizFile.getName().equals(NotizFile.NO_NAME_SET) ? "" : notizFile.getName());

                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                editText.setText("");
                            }
                        });
                    }
                });
                dialog.show();
                return true;
            case R.id.delete:
                builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.delete_file_dialog_message);
                builder.setPositiveButton(R.string.ok_button_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        notizFile.delete();
                        deleted = true;
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.cancel_button_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                dialog = builder.create();
                dialog.show();
                return true;
            case R.id.zwischenspeichern:
                String oldText = notizFile.getText();
                try {
                    if (saveFile()) {
                        Toast.makeText(context, R.string.info_notiz_gespeichert, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    restorOldFile(oldText);
                    Toast.makeText(context, R.string.info_nichtspeicherbare_zeichen, Toast.LENGTH_LONG).show();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Einstellungen abrufen und setzen.
     */
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean autoCapital = preferences.getBoolean(Constants.CAPITAL_SETTING_KEY, false);

        textField.setInputType(autoCapital ?
                INPUT_TYPE_CAPITAL :
                STANDART_INPUT_TYPE);
        textField.setSingleLine(false);
        try {
            textField.setTextSize(Float.parseFloat(preferences.getString(Constants.TEXT_SIZE_SETTING_KEY, "18")));
        } catch (NumberFormatException e) {
            textField.setTextSize(18);
        }
        textField.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        textField.setSelection(textField.getText().length());

        // Hintergrund der Titelleiste setzen
        Util.setTitlebarColor(this);

        // Hintergrund der Editorleiste setzen
        RelativeLayout editbar = (RelativeLayout) findViewById(R.id.editbar);
        int color = ContextCompat.getColor(context, R.color.colorEditBar);
        int prefInt = preferences.getInt(Constants.EDITBAR_COLOR_SETTING_KEY, color);
        ColorDrawable colorDrawable = new ColorDrawable(prefInt);
        editbar.setBackground(colorDrawable);
    }

    /**
     * Aktion des Copy-Buttons. Kopiert kompletten oder markierten Text in Zwischenablage.
     * @param view .
     */
    public void bCopyToClipboardPressed(View view) {
        if (textField == null || textField.getText().toString().trim().isEmpty()) {
            return;
        }

        int selectionStart = textField.getSelectionStart();
        int selectionEnd = textField.getSelectionEnd();
        String text;

        if (selectionStart == selectionEnd) {
            text = textField.getText().toString().replaceAll("\\s+$", "");
        } else {
            text = textField.getText().toString().substring(selectionStart, selectionEnd);
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(text, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, R.string.info_copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    /**
     * Aktion des Paste-Buttons. Fügt Text aus Zwischenablage ein. Ist ein Teil des Textes markiert,
     * wird dieser ersetzt.
     * @param view .
     */
    public void bPasteFromClipboardPressed(View view) {
        if (textField == null) {
            return;
        }

        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

            if (!clipboard.hasPrimaryClip())
                return;

            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);

            String pasteData = item.getText().toString();
            String newText;

            int selectionStart = textField.getSelectionStart();
            int selectionEnd = textField.getSelectionEnd();
            String oT = textField.getText().toString();


            newText = oT.substring(0, selectionStart)
                    + pasteData
                    + oT.substring(selectionEnd);

            textField.setText(newText);

            textField.setSelection(selectionStart + pasteData.length());
        } catch (Exception e) {
            Toast.makeText(context, R.string.clipboard_paste_fehler, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Aktion des Tab-Buttons. Fügt 4 Leerzeichen ein. (Später individuell einstellbar)
     * @param view .
     */
    public void bTabPressed(View view) {
        if (textField == null) {
            return;
        }
        int selectionStart = textField.getSelectionStart();
        int selectionEnd = textField.getSelectionEnd();
        String s = textField.getText().toString();
        String newText = s.substring(0, selectionStart) + getString(R.string.tab);
        newText = selectionEnd < textField.getText().length() ?
                newText + s.substring(selectionEnd) : newText;
        textField.setText(newText);
        textField.setSelection(selectionStart + 5, selectionStart + 5);
    }

    /**
     * Aktion des Listen-Buttons. Zeigt eine Liste an.
     * @param view .
     */
    public void bListPressed(View view) {
        String text = textField.getText().toString();
        int currentCursorPosition = textField.getSelectionStart();
        int lastLineBreak = getLastLineBreak(text, currentCursorPosition);

        boolean listActivated;
        try {
            listActivated = isListActivated(text, lastLineBreak);
        } catch (Exception ignored) {
            Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_SHORT).show();
            return;
        }
        int length = text.length();
        if (listActivated) {
            // Wenn aktiviert
            String firstPart;
            if (lastLineBreak > 0) {
                firstPart = lastLineBreak + 1 > length ? text :
                        text.substring(0, lastLineBreak + 1);
            } else {
                firstPart = "";
            }
            String substring = text.substring(lastLineBreak);
            int index = substring.indexOf("-");
            String secondPart = lastLineBreak + 1 > length ? "" :
                    text.substring(lastLineBreak + index + 1).trim();

            String newText = firstPart + secondPart;
            textField.setText(newText);
            try {
                textField.setSelection(currentCursorPosition - (length - newText.length()));
            } catch (Exception ignored) {
            }
        } else {
            // Wenn nicht aktiviert
            String firstPart;
            String secondPart;
            if (lastLineBreak > 0) {
                firstPart = lastLineBreak + 1 > length ? text :
                        text.substring(0, lastLineBreak + 1);
                secondPart = lastLineBreak + 1 > length ? "" :
                        text.substring(lastLineBreak + 1).trim();
            } else {
                firstPart = "";
                secondPart = lastLineBreak + 1 > length ? "" :
                        text.substring(lastLineBreak).trim();
            }

            String newText = firstPart + getString(R.string.listString) + secondPart;
            textField.setText(newText);
            try {
                textField.setSelection(currentCursorPosition + (newText.length() - length));
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Prüft, ob in der Zeile, nach dem Zeilenumbruch, die Liste aktiv ist.
     * @param text          Zu prüfender Text
     * @param lastLineBreak Index des Zeilenumbruchs vor der Zeile
     * @return True, wenn aktiv, sonst false
     */
    private boolean isListActivated(String text, int lastLineBreak) {
        String s = text.substring(lastLineBreak);
        s = s.trim();
        return s.startsWith("-");
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            String text;
            int currentCursorPosition;
            int lastLineBreak;
            switch (event.getKeyCode()) {
                // Bei aktiver Liste eine neue Listenzeile hinzufügen, sonst einfache neue Zeile
                case KeyEvent.KEYCODE_ENTER:
                    text = textField.getText().toString();
                    currentCursorPosition = textField.getSelectionStart();
                    lastLineBreak = getLastLineBreak(text, currentCursorPosition);
                    if (isListActivated(text, lastLineBreak)) {
                        String firstPart = text.substring(0, currentCursorPosition);
                        String seconPart = text.substring(textField.getSelectionEnd());
                        String newText = firstPart + "\n" + getString(R.string.listString) + seconPart;
                        textField.setText(newText);
                        try {
                            textField.setSelection(currentCursorPosition + getString(R.string.listString).length() + 1);
                        } catch (Exception ignored) {
                        }
                        return true;
                    }
                    return super.dispatchKeyEvent(event);
                // Bei aktiver Liste diese löschen, wenn Cursor an erster Position ist,
                // sonst einfach löschen
                case KeyEvent.KEYCODE_DEL:
                    /*text = textField.getText().toString();
                    currentCursorPosition = textField.getSelectionStart();
                    lastLineBreak = getLastLineBreak(text, currentCursorPosition);
                    if (isListActivated(text, lastLineBreak)) {

                    }*/
                    return super.dispatchKeyEvent(event);
                default:
                    return super.dispatchKeyEvent(event);
            }
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    /**
     * Findet den Zeilenumbruch vor der aktuellen Position.
     * @param text                  Text
     * @param currentCursorPosition Aktuelle Cursorposition
     * @return Index vom Zeilenumbruch
     */
    private int getLastLineBreak(String text, int currentCursorPosition) {
        int lastLineBreak;
        try {
            lastLineBreak = text.substring(0, currentCursorPosition).lastIndexOf("\n");
            if (lastLineBreak < 0) {
                lastLineBreak = 0;
            }
        } catch (Exception e) {
            lastLineBreak = 0;
        }
        return lastLineBreak;
    }
}