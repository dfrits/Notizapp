package daniel.com.notizapp.array_adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import daniel.com.notizapp.R;
import daniel.com.notizapp.file.NotizFile;
import daniel.com.notizapp.util.Constants;

/**
 * Created by Tristan on 29.04.2017.
 */

public class CustomMultiSelectAdapter extends ArrayAdapter<NotizFile> {

    private SharedPreferences preferences;
    private final Context context;
    private final List<NotizFile> dataList;
    private boolean isSelected[];

    public CustomMultiSelectAdapter(Context context, List<NotizFile> dataList) {
        super(context, R.layout.layout_listview_row_multi_choice, dataList);
        this.context = context;
        this.dataList = dataList;
        isSelected = new boolean[dataList.size()];
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public NotizFile getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(final int position, View view, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_listview_row_multi_choice, null);
            holder = new ViewHolder();
            holder.relativeLayout = (LinearLayout) view.findViewById(R.id.row_relative_layout);
            holder.checkedTextView = (CheckedTextView) view.findViewById(R.id.checked_list_view_row);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        NotizFile notizFile = dataList.get(position);
        String rowText = notizFile.getName();
        rowText = rowText.equals(NotizFile.NO_NAME_SET) ?
                notizFile.getText().isEmpty() ? "Notiz" + position : notizFile.getText() :
                rowText;

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

        holder.checkedTextView.setText(rowText);
        holder.checkedTextView.setChecked(isSelected[position]);

        holder.checkedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // set the check text view
                /*boolean flag = holder.checkedTextView.isChecked();
                holder.checkedTextView.setChecked(!flag);
                isSelected[position] = !isSelected[position];*/
                selectBox(position, !holder.checkedTextView.isChecked());
            }
        });

        return view;
    }

    /**
     * Setzt einen Haken in die Checkbox an der angegebenen Position (true)
     * oder löscht diese (false).
     * @param position Position der Checkbox in der Liste
     * @param check    Bei true wird der Haken gesetzt und bei false entfernt
     */
    public void selectBox(int position, boolean check) {
        isSelected[position] = check;
        notifyDataSetChanged();
    }

    /**
     * Gibt das Array zurück. Dabei sind alle Checkboxen enthalten. Die Position der Checkbox in
     * der Liste entspricht der Position im Array. An dieser Position steht entweder <b>true</b>,
     * wenn ein Haken gesetzt wurde oder <b>false</b> andernfalls.
     * @return Array mit den Flags
     */
    public boolean[] getSelectedFlags() {
        return isSelected;
    }

    /**
     * Zählt die ausgewählten Checkboxen.
     * @return Anzahl ausgewählter Checkboxen
     */
    public int getSelectedFlagsCount() {
        int count = 0;
        for (boolean anIsSelected : isSelected) {
            if (anIsSelected) {
                count++;
            }
        }

        return count;
    }

    private class ViewHolder {
        LinearLayout relativeLayout;
        CheckedTextView checkedTextView;

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
}
