package daniel.com.notizapp.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import daniel.com.notizapp.array_adapter.CustomSingleSelectAdapter;
import daniel.com.notizapp.file.NotizFile;
import daniel.com.notizapp.util.Constants;
import daniel.com.notizapp.R;
import daniel.com.notizapp.util.Util;

/**
 * Created by Tristan on 18.09.2016.
 */
public class ShareReceiver extends Activity {
    private final Context context = this;

    public static String folderPath;

    private List<NotizFile> files;
    private String sharedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_share);

        Intent intent = getIntent();
        if ("text/plain".equals(intent.getType())) {
            handleSendText(intent);
        } else {
            finish();
        }
    }

    /**
     * Initialisierung
     * @param intent ShareIntent
     */
    private void handleSendText(Intent intent) {
        sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            init();
        }
    }

    /**
     * Initialisierung um eine Datei auszuwählen in der der Text gespeichert werden soll.
     */
    private void init() {
        folderPath = SplashActivity.getFolderPath();
        ListView listView = (ListView) findViewById(R.id.listView);

        files = Util.getAllNotices(context);

        if (listView != null) {
            listView.setAdapter(new CustomSingleSelectAdapter(context, files));
            listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    itemClicked(position);
                }
            });
        } else {
            Toast.makeText(context, R.string.init_fehler, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Bei Singleselektion wird die Datei geöffnet.
     * Bei Multiselektion wird die Datei selektiert oder deselektiert.
     * @param position Position der Datei in der Liste
     */
    private void itemClicked(int position) {
        Intent openNotice = new Intent(context, NotizActivity.class);
        openNotice.putExtra(Constants.SHARED_TEXT_KEY, sharedText);

        openNotice.putExtra(Constants.FILE_EXTRA_KEY, files.get(position));
        startActivity(openNotice);
    }

    /**
     * Neue Datei erzeugen um geteilten Text zu empfangen.
     * @param view .
     */
    public void createNewSharedFile(View view) {
        Intent newNotice = Util.getNewNoticeIntent(context);
        newNotice.putExtra(Constants.SHARED_TEXT_KEY, sharedText);
        startActivity(newNotice);
    }

    /**
     * Wird der ShareReciever beendet, wird der Notizblock normal gestartet.
     */
    private boolean firstStart = true;

    @Override
    protected void onResume() {
        super.onResume();
        if (firstStart) {
            firstStart = false;
        } else {
            startActivity(new Intent(context, SplashActivity.class));
            finish();
        }
    }
}
