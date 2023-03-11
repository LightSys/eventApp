package org.lightsys.eventApp.tools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import org.lightsys.eventApp.R;

public class RecyclerViewDivider extends RecyclerView.ItemDecoration {
    private Drawable divider;

    public RecyclerViewDivider(Context context){
        divider = ContextCompat.getDrawable(context,R.drawable.recycler_view_divider);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int child_count = parent.getChildCount();
        for (int i = 0; i < child_count; i++){
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();

            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }
}
