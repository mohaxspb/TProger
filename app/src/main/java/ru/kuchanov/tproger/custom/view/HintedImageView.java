/*
 11.04.2015
HintedImageView.java
Created by Kuchanov Yuri,
mohax.spb@gmail.com
 */
package ru.kuchanov.tproger.custom.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Extended image view to show the content description in a Toast view when the
 * user long presses.
 * <p/>
 * Note: `android:contentDescription` must be set in order for the toast to work
 *
 * @author Callum Taylor <http://callumtaylor.net>
 * @see <https://github.com/scruffyfox/Robin-Client/blob/master/v2%20client/Robin/src/main/java/in/lib/view/HintedImageView.java></https://github.com/scruffyfox/Robin-Client/blob/master/v2%20client/Robin/src/main/java/in/lib/view/HintedImageView.java>
 */
public class HintedImageView extends ImageView implements OnLongClickListener
{
    private OnLongClickListener mOnLongClickListener;

    public HintedImageView(Context context)
    {
        super(context);
        setOnLongClickListener(this);
    }

    public HintedImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setOnLongClickListener(this);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l)
    {
        if (l == this)
        {
            super.setOnLongClickListener(l);
            return;
        }

        mOnLongClickListener = l;
    }

    @Override
    public boolean onLongClick(View v)
    {
        if (mOnLongClickListener != null)
        {
            if (!mOnLongClickListener.onLongClick(v))
            {
                handleLongClick();
                return true;
            }
        }
        else
        {
            handleLongClick();
            return true;
        }

        return false;
    }

    private void handleLongClick()
    {
        String contentDesc = getContentDescription().toString();
        if (!TextUtils.isEmpty(contentDesc))
        {
            int[] pos = new int[2];
            getLocationInWindow(pos);

            Toast t = Toast.makeText(getContext(), contentDesc, Toast.LENGTH_SHORT);
            t.setGravity(Gravity.TOP | Gravity.LEFT, pos[0] - ((contentDesc.length() / 2) * 12), pos[1] - 128);
            t.show();
        }
    }
}