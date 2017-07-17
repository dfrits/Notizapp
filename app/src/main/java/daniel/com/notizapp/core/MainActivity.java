package daniel.com.notizapp.core;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import daniel.com.notizapp.array_adapter.CustomMultiSelectAdapter;
import daniel.com.notizapp.array_adapter.CustomSingleSelectAdapter;
import daniel.com.notizapp.R;
import daniel.com.notizapp.file.ESortBy;
import daniel.com.notizapp.file.EWichtigkeit;
import daniel.com.notizapp.file.NotizFile;
import daniel.com.notizapp.setting.SettingsActivity;
import daniel.com.notizapp.util.Constants;
import daniel.com.notizapp.util.Util;

public class MainActivity extends AppCompatActivity {
    private final Context context = this;

    private boolean firstStart = true;
    private String oldPath;
    private List<NotizFile> files;
    private ListView listView;
    private Button rightFloatButton;
    private LinearLayout view;
    private Menu menu;
    private TextView sortedByLabel;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        PreferenceManager.setDefaultValues(this, R.xml.preference_general, false);
        pref = PreferenceManager.getDefaultSharedPreferences(context);

        init();
    }

    /**
     * Initialisierung
     */
    private void init() {
        listView = (ListView) findViewById(R.id.listView);
        rightFloatButton = (Button) findViewById(R.id.bButtonRight);
        view = (LinearLayout) findViewById(R.id.overlayButtons);
        sortedByLabel = (TextView) findViewById(R.id.sortedByLabel);

        initFiles();

        if (listView != null) {
            listView.setAdapter(new CustomSingleSelectAdapter(context, files));
            listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    itemClicked(position);
                }
            });
            registerForContextMenu(listView);
            view.setVisibility(View.INVISIBLE);
        } else {
            Toast.makeText(context, R.string.init_fehler, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Liest die Dateien aus und füllt die Liste.
     */
    private void initFiles() {

        files = Util.getAllNotices(context);

        for (int i = 1; files == null && i < 10; i++) {
            files = Util.getAllNotices(context);
        }

        if (files == null) {
            Toast.makeText(context, R.string.datei_lese_fehler, Toast.LENGTH_SHORT).show();
            files = new ArrayList<>();
        }

        ESortBy sort = ESortBy.getSorting(pref.getInt(Constants.SORT_BY_KEY, ESortBy.NOT_SORTED.ordinal()));
        NotizFile.setSort(sort);
        Collections.sort(files);
        setSortByLabel(sort);
    }

    /**
     * Bei Singleselektion wird die Datei geöffnet.
     * Bei Multiselektion wird die Datei selektiert oder deselektiert.
     * @param position Position der Datei in der Liste
     */
    private void itemClicked(int position) {
        Intent openNotice = new Intent(context, NotizActivity.class);
        if (listView.getChoiceMode() == AbsListView.CHOICE_MODE_MULTIPLE) {
            if (listView.isItemChecked(position)) {
                listView.setItemChecked(position, true);
            } else {
                listView.setItemChecked(position, false);
            }
        } else {
            openNotice.putExtra(Constants.FILE_EXTRA_KEY, files.get(position));
            openNotice.putExtra(Constants.IS_NEW_FILE, false);
            startActivity(openNotice);
        }
    }

    // Popupmenü von ListView
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listView) {
            Vibrator vibrator = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(100);
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menubuttons_listview_popup, menu);
        }
    }

    // Popupmenü von ListView
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final NotizFile notizFile = files.get(info.position);
        AlertDialog.Builder builder;
        AlertDialog dialog;
        switch (item.getItemId()) {
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
                break;
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
                        dialog.cancel();
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
                break;
            case R.id.delete:
                builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.delete_file_dialog_message);
                builder.setPositiveButton(R.string.ok_button_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        notizFile.delete();
                        dialog.cancel();
                    }
                });
                builder.setNegativeButton(R.string.cancel_button_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                dialog = builder.create();
                break;
            case R.id.share:
                builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.share_file_dialog_message)
                        .setPositiveButton(R.string.file, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = Util.createShareFileIntent(files.get(info.position),
                                        getExternalFilesDir(null));
                                startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_title)));
                            }
                        })
                        .setNegativeButton(R.string.text, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = Util.createShareTextIntent(files.get(info.position)
                                );
                                startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_title)));
                            }
                        });
                dialog = builder.create();
                break;
            default:
                return super.onContextItemSelected(item);
        }
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (notizFile.exists()) {
                    notizFile.delete();
                    notizFile.safeFile();
                }
                initFiles();
                setSingleSelectView();
            }
        });
        dialog.show();
        return true;
    }

    // Titelleistenmenü
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menubuttons_main_acitvity, menu);
        this.menu = menu;
        return true;
    }

    // Titelleistenmenü
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences.Editor editor = pref.edit();
        ESortBy sort;
        switch (item.getItemId()) {
            case R.id.new_notiz:
                Intent newNotice = Util.getNewNoticeIntent(context);
                startActivity(newNotice);
                return true;
            case R.id.delete:
                if (files.size() == 0) {
                    return true;
                }
                setMultiSelectView(getResources().getString(R.string.delete_notice));
                return true;
            case R.id.share:
                if (files.size() == 0) {
                    return true;
                }
                setMultiSelectView(getResources().getString(R.string.share));
                return true;
            case R.id.select_all:
                CustomMultiSelectAdapter adapter = (CustomMultiSelectAdapter) listView.getAdapter();
                if (adapter.getSelectedFlagsCount() < listView.getCount()) {
                    checkAllItems(adapter, true);
                } else {
                    checkAllItems(adapter, false);
                }
                return true;
            case R.id.action_settings:
                oldPath = SplashActivity.getFolderPath();
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra("Activity", "Main");
                startActivity(intent);
                return true;
            case R.id.menuNoSort:
                editor.putInt(Constants.SORT_BY_KEY, ESortBy.NOT_SORTED.ordinal());
                editor.apply();
                NotizFile.setSort(ESortBy.NOT_SORTED);
                Collections.sort(files);
                ((CustomSingleSelectAdapter) listView.getAdapter()).notifyDataSetChanged();
                setSortByLabel(ESortBy.NOT_SORTED);
                return true;
            case R.id.menuSortName:
                if (NotizFile.sortedBy() == ESortBy.NAME_ASC) {
                    sort = ESortBy.NAME_DESC;
                } else {
                    sort = ESortBy.NAME_ASC;
                }
                setSortByLabel(sort);
                editor.putInt(Constants.SORT_BY_KEY, sort.ordinal());
                editor.apply();
                NotizFile.setSort(sort);
                Collections.sort(files);
                ((CustomSingleSelectAdapter) listView.getAdapter()).notifyDataSetChanged();
                return true;
            case R.id.menuSortDate:
                if (NotizFile.sortedBy() == ESortBy.DATE_ASC) {
                    sort = ESortBy.DATE_DESC;
                } else {
                    sort = ESortBy.DATE_ASC;
                }
                setSortByLabel(sort);
                editor.putInt(Constants.SORT_BY_KEY, sort.ordinal());
                editor.apply();
                NotizFile.setSort(sort);
                Collections.sort(files);
                ((CustomSingleSelectAdapter) listView.getAdapter()).notifyDataSetChanged();
                return true;
            case R.id.menuSortFavor:
                if (NotizFile.sortedBy() == ESortBy.WICHTIG_ASC) {
                    sort = ESortBy.WICHTIG_DESC;
                } else {
                    sort = ESortBy.WICHTIG_ASC;
                }
                setSortByLabel(sort);
                editor.putInt(Constants.SORT_BY_KEY, sort.ordinal());
                editor.apply();
                NotizFile.setSort(sort);
                Collections.sort(files);
                ((CustomSingleSelectAdapter) listView.getAdapter()).notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Setzt das Label über der Liste, wonach gerade sortiert wird.
     * @param sortBy Aktuelle Sortierung
     */
    private void setSortByLabel(ESortBy sortBy) {
        switch (sortBy) {
            case NOT_SORTED:
                sortedByLabel.setText("");
                break;
            case NAME_ASC:
                sortedByLabel.setText(R.string.sort_by_name_asc);
                break;
            case NAME_DESC:
                sortedByLabel.setText(R.string.sort_by_name_desc);
                break;
            case DATE_ASC:
                sortedByLabel.setText(R.string.sort_by_date_asc);
                break;
            case DATE_DESC:
                sortedByLabel.setText(R.string.sort_by_date_desc);
                break;
            case WICHTIG_ASC:
                sortedByLabel.setText(R.string.sort_by_importance_asc);
                break;
            case WICHTIG_DESC:
                sortedByLabel.setText(R.string.sort_by_importance_desc);
                break;
        }
    }

    /**
     * Ermöglicht mehrere Dateien in der Liste auszuwählen.
     * Menüitems in der Titelleiste werden geändert.
     * Auf dem linken Button wird immer Abbrechen angezeigt.
     * @param rightButtonText Text, der auf dem rechten Button angezeigt werden soll.
     */
    private void setMultiSelectView(String rightButtonText) {
        if (menu != null) {
            listView.setAdapter(new CustomMultiSelectAdapter(context, files));
            listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            menu.findItem(R.id.new_notiz).setVisible(false);
            menu.findItem(R.id.delete).setVisible(false);
            menu.findItem(R.id.share).setVisible(false);
            menu.findItem(R.id.action_settings).setVisible(false);
            menu.findItem(R.id.menuSort).setVisible(false);
            menu.findItem(R.id.select_all).setVisible(true);

            view.setVisibility(View.VISIBLE);
            rightFloatButton.setText(rightButtonText);
        }
    }

    /**
     * Setzt die Selektion und die Menüitems zurück
     */
    private void setSingleSelectView() {
        if (menu != null) {
            listView.setAdapter(new CustomSingleSelectAdapter(context, files));
            listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            menu.findItem(R.id.new_notiz).setVisible(true);
            menu.findItem(R.id.delete).setVisible(true);
            menu.findItem(R.id.share).setVisible(true);
            menu.findItem(R.id.action_settings).setVisible(true);
            menu.findItem(R.id.menuSort).setVisible(true);
            menu.findItem(R.id.select_all).setVisible(false);

            view.setVisibility(View.INVISIBLE);

            // Selektionen löschen
            for (int i = 0; i < listView.getCount(); i++) {
                listView.setItemChecked(i, false);
            }
        }
    }

    /**
     * Führt die Aktion des linken Buttons aus.
     * @param view .
     */
    public void leftFloatButton(View view) {
        setSingleSelectView();
    }

    /**
     * Führt die Aktion des rechten Buttons aus.
     * @param view .
     */
    public void rightFloatButton(View view) {
        // Files löschen
        if (files.size() != 0 && rightFloatButton.getText().equals(getResources().getString(R.string.delete_notice))) {
            boolean isSelected[] = ((CustomMultiSelectAdapter) listView.getAdapter()).getSelectedFlags();
            final ArrayList<NotizFile> selectedItems = new ArrayList<>();

            for (int i = 0; i < isSelected.length; i++) {
                if (isSelected[i]) {
                    selectedItems.add(files.get(i));
                }
            }

            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message mesg) {
                    throw new RuntimeException();
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.delete_file_dialog_message);
            builder.setPositiveButton(R.string.ok_button_text, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    for (NotizFile file : selectedItems) {
                        file.delete();
                    }
                    handler.sendMessage(handler.obtainMessage());
                }
            });
            builder.setNegativeButton(R.string.cancel_button_text, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    handler.sendMessage(handler.obtainMessage());
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

            try { Looper.loop(); }
            catch(RuntimeException ignored) {}

            initFiles();
        }
        // Files teilen
        else if (files.size() != 0 && rightFloatButton.getText().equals(getResources().getString(R.string.share))) {
            boolean isSelected[] = ((CustomMultiSelectAdapter) listView.getAdapter()).getSelectedFlags();
            final ArrayList<NotizFile> selectedItems = new ArrayList<>();

            for (int i = 0; i < isSelected.length; i++) {
                if (isSelected[i]) {
                    selectedItems.add(files.get(i));
                }
            }
            if (selectedItems.size() == 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.share_file_dialog_message)
                        .setPositiveButton(R.string.file, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = Util.createShareFileIntent(selectedItems.get(0),
                                        getExternalFilesDir(null));
                                startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_title)));
                            }
                        })
                        .setNegativeButton(R.string.text, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = Util.createShareTextIntent(selectedItems.get(0));
                                startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_title)));
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                Intent intent = Util.createShareFilesIntent(selectedItems, getExternalFilesDir(null));if (intent != null) {
                    startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_title)));
                } else {
                    Toast.makeText(this, getResources().getText(R.string.no_external_storage_found), Toast.LENGTH_SHORT).show();
                }
            }
        }

        setSingleSelectView();
    }

    /**
     * Prüft ob der Pfad geändert wurde und fragt gegebenenfalls ob die Files verschoben werden
     * sollen.
     */
    private void proofFolderDest() {
        SplashActivity.setFolderPath();
        if (oldPath != null && !oldPath.equals(SplashActivity.getFolderPath())) {
            if (new File(oldPath).listFiles().length > 0) {
                // Notizen kopieren?
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.move_files_dialog_message);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Util.moveFiles(oldPath, SplashActivity.getFolderPath(), context, getString(R.string.progress_dialog_message));
                        dialog.cancel();
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        initFiles();
                        setSingleSelectView();
                    }
                });
            }
        }
    }

    /**
     * Beim Starten der Acitvity passiert nichts. Danach wird die Liste aktualisiert und
     * die Selektion zurückgesetzt.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Util.setTitlebarColor(this);
        if (!firstStart) {
            proofFolderDest();
            initFiles();
            setSingleSelectView();
        } else {
            firstStart = false;
        }
    }

    /**
     * Ist Multiselection aktiviert, wird diese deaktiviert. Sonst wird die App beendet.
     */
    @Override
    public void onBackPressed() {
        if (listView.getChoiceMode() == AbsListView.CHOICE_MODE_MULTIPLE) {
            setSingleSelectView();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                finish();
            }
        } else {
            finish();
        }
    }

    /**
     * Setzt entweder alle Checkboxen auf true oder false.
     * @param adapter  Adapter, bei dem die Checkboxen gesetzt werden sollen
     * @param checkAll Bei true werden alle mit einem Haken versehen. Bei false werden alle entfernt
     */
    private void checkAllItems(CustomMultiSelectAdapter adapter, boolean checkAll) {
        for (int i = 0; i < adapter.getCount(); i++) {
            adapter.selectBox(i, checkAll);
        }
    }
}
