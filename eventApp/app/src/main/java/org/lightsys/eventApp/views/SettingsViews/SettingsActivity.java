package org.lightsys.eventApp.views.SettingsViews;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;

import org.lightsys.eventApp.R;
import org.lightsys.eventApp.tools.ColorContrastHelper;
import org.lightsys.eventApp.tools.LocalDB;

/**
 * Created by Littlesnowman88 on 05/30/2018
 * Allows the user to adjust time zone and notification update frequency
 */
public class SettingsActivity extends AppCompatActivity {

    private int color, black_or_white;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*initialize activity-related settings data*/
        setContentView(R.layout.settings_layout);

        /* set up an action bar for returning to Main Activity */
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        LocalDB db = new LocalDB(this.getApplicationContext());
        try {
            color = Color.parseColor(db.getThemeColor("themeColor"));
            black_or_white = ColorContrastHelper.determineBlackOrWhite(color);
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
            actionBar = ColorContrastHelper.setToolBarTextColor(actionBar, black_or_white);
        } catch (Exception e) {
            color = Color.parseColor("#6080C0");
            black_or_white = ColorContrastHelper.determineBlackOrWhite(color);
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
            actionBar = ColorContrastHelper.setToolBarTextColor(actionBar, black_or_white);
        }
    }

    /* used for the back-arrow button that returns to the Main Activity
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}