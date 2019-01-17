package daniel.com.notizapp.array_adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import daniel.com.notizapp.R;
import daniel.com.notizapp.file.NotizFile;
import daniel.com.notizapp.util.Constants;

/**
 * Created by Tristan on 01.10.2016.
 */

public class CustomSingleSelectAdapter extends ArrayAdapter<NotizFile> {
    final private Context context;

    private SharedPreferences preferences;
    private List<NotizFile> data;


    public CustomSingleSelectAdapter(Context context, List<NotizFile> data) {
        super(context, R.layout.layout_listview_row, data);
        this.context = context;
        this.data = data;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        TextViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.layout_listview_row, parent, false);

            holder = new TextViewHolder((TextView) row.findViewById(R.id.list_view_row));

            row.setTag(holder);
        } else {
            holder = (TextViewHolder) row.getTag();
        }
        String rowText = data.get(position).getName();
        rowText = rowText.equals(NotizFile.NO_NAME_SET) ?
                data.get(position).getText().isEmpty() ? "Notiz " + position : data.get(position).getText()
                : rowText;

        int max_rows;
        float textSize;
        try {
            // Einstellungen holen
            max_rows = Integer.parseInt(preferences.getString(Constants.MAX_ROWS_SETTING_KEY, "3"));
            textSize = Float.parseFloat(preferences.getString(Constants.TEXT_SIZE_SETTING_KEY, "18"));

            if (max_rows == 0) {
                rowText = "Notiz " + position;
                max_rows = 1;
            }

        } catch (NumberFormatException e) {
            max_rows = 3;
            textSize = 18;
        }

        holder.setMaxRows(max_rows);
        holder.setTextSize(textSize);
        holder.setText(rowText);
        ColorDrawable backroundColor = new ColorDrawable(data.get(position).getWichtigkeit().getColor());
        row.setBackground(backroundColor);

        return row;
    }

    /**
     * Hält die Daten der Reihe
     */
    private class TextViewHolder {
        private TextView textView;

        TextViewHolder(TextView textView) {
            this.textView = textView;
        }

        void setText(String text) {
            if (textView != null) {
                textView.setText(text);
            }
        }

        void setMaxRows(int maxRows) {
            if (textView != null) {
                textView.setMaxLines(maxRows);
            }
        }

        void setTextSize(float textSize) {
            if (textView != null) {
                textView.setTextSize(textSize);
            }
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
