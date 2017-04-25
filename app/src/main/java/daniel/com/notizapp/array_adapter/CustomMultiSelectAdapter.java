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
import android.widget.CheckedTextView;

import java.util.ArrayList;
import java.util.List;

import daniel.com.notizapp.R;
import daniel.com.notizapp.file.NotizFile;
import daniel.com.notizapp.util.Constants;

/**
 * Created by Tristan on 29.12.2016.
 */

public class CustomMultiSelectAdapter extends ArrayAdapter<NotizFile> {
    private final Context context;

    private SharedPreferences preferences;
    private List<TextViewHolder> holderList;
    private List<NotizFile> checkedItems;
    private List<NotizFile> data;

    public CustomMultiSelectAdapter(Context context, List<NotizFile> data) {
        super(context, R.layout.layout_listview_row_multi_choice, data);
        this.context = context;
        this.data = data;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        checkedItems = new ArrayList<>();
        holderList = new ArrayList<>();
    }

    public List<NotizFile> getCheckedItems() {
        return checkedItems;
    }

    @NonNull
    @Override
    public View getView(final int position, final View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        final TextViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.layout_listview_row_multi_choice, parent, false);

            holder = new TextViewHolder();
            holder.checkedTextView = (CheckedTextView) row.findViewById(R.id.checked_list_view_row);
            holder.notizFile = data.get(position);
            // Reihe zwischenspeichern
            holderList.add(holder);

            row.setTag(holder);
        } else {
            holder = (TextViewHolder) row.getTag();
        }
        String rowText = holder.notizFile.getName();
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
        ColorDrawable backroundColor = new ColorDrawable(holder.notizFile.getWichtigkeit().getColor());
        row.setBackground(backroundColor);

        // Markiert bzw. demarkiert die Reihe und fügt die Notiz zum Array hinzu
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextViewHolder tag = (TextViewHolder) v.getTag();
                tag.checkedTextView.setChecked(!tag.checkedTextView.isChecked());
                try {
                    if (tag.checkedTextView.isChecked()) {
                        checkedItems.add(holderList.get(position).notizFile);
                    } else {
                        checkedItems.remove(holderList.get(position).notizFile);
                    }
                } catch (Exception e) {
                    try {
                        checkedItems.remove(holderList.get(position).notizFile);
                    } catch (Exception ignored) {
                    }
                }
            }
        });

        return row;
    }

    public void setAllItemsChecked(boolean checked) {
        if (checked) {
            checkedItems = new ArrayList<>(data);
        } else {
            checkedItems.clear();
        }
        for (TextViewHolder holder : holderList) {
            holder.checkedTextView.setChecked(checked);
        }
    }

    /**
     * Hält die Daten der Reihe
     */
    private class TextViewHolder {
        private CheckedTextView checkedTextView;
        private NotizFile notizFile;

        void setText(String text) {
            if (checkedTextView != null) {
                checkedTextView.setText(text);
            }
        }

        void setMaxRows(int maxRows) {
            if (checkedTextView != null) {
                checkedTextView.setMaxLines(maxRows);
            }
        }

        void setTextSize(float textSize) {
            if (checkedTextView != null) {
                checkedTextView.setTextSize(textSize);
            }
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
