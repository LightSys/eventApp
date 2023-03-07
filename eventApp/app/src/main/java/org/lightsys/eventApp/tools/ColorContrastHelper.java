package org.lightsys.eventApp.tools;

import android.graphics.Color;
import androidx.appcompat.app.ActionBar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import org.lightsys.eventApp.R;

/** Used by EventApp upper toolbars and action bars to contrast toolbar/action bar text against background colors
 * Created by Littlesnowman88 to help reduce code duplication
 * Created on 13 July 2018
 */
public final class ColorContrastHelper {
    private static final int BLACK = Color.parseColor("#000000");
    private static final int WHITE = Color.parseColor("#ffffff");


    /**
     * converts the given hex_color into grayscale and determines whether toolbar items should be black or white
     * Created by Littlesnowman88 on 22 June 2018
     * @param theme_color, the integer color to be analyzed and contrasted against
     * Postcondition: black_or_white = #000000 if black, ffffff if white, whatever will show better given hex_color
     */
    public static int determineBlackOrWhite(int theme_color) {
        int r = Color.red(theme_color); // 0 < r < 255
        int g = Color.green(theme_color); // 0 < g < 255
        int b = Color.blue(theme_color); // 0 < b < 255
        int average_intensity = (r + g + b) / 3;
        if (average_intensity >= 120) {
            return BLACK;
        } return WHITE;
    }

    /**
     * converts the given hex_color into grayscale and determines whether toolBar text color should be black or white
     * Created by Littlesnowman88 on 22 June 2018
     * @param action_bar, the action bar having its text color set
     * Postcondition: text color is set to either black or white, whatever will show better
     */
    public static ActionBar setToolBarTextColor(ActionBar action_bar, int black_or_white) {
        String title = action_bar.getTitle().toString();
        SpannableStringBuilder color_setter = new SpannableStringBuilder(title);
        if (black_or_white == BLACK) {
            ForegroundColorSpan color_span = new ForegroundColorSpan(BLACK);
            color_setter.setSpan(color_span, 0, title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            action_bar.setTitle(color_setter);
            action_bar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black);
        } else {
            ForegroundColorSpan color_span = new ForegroundColorSpan(WHITE);
            color_setter.setSpan(color_span, 0, title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            action_bar.setTitle(color_setter);
            action_bar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }
        return action_bar;
    }
}
