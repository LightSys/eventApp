package org.lightsys.eventApp.tools;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.lightsys.eventApp.R;

/**
 * Created by otter57 on 4/5/17.
 *
 * adapter for refresh dropdown view
 */

public class RefreshAdapter extends ArrayAdapter<String> {

    private final String[] data;

    public RefreshAdapter(Context context, String [] data) {
        super(context, R.layout.refresh_item, data);
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, View v, @NonNull ViewGroup parent) {
        View mView = super.getView(position, v, parent);
        TextView textView = mView.findViewById(R.id.headerText);

        int time = Integer.parseInt(data[position]);
        String displayText;

        //depending on value, set appropriate text
        if (time == -1){
            displayText = "Never";
            textView.setText(displayText);
        }else if (time <60) {
            textView.append(" minute");
        } else {
            time = time/60;
            displayText = time + " hour";
            textView.setText(displayText);
        }
        if (time != 1 && time>0) {
            textView.append("s");
        }

        return mView;
    }
}