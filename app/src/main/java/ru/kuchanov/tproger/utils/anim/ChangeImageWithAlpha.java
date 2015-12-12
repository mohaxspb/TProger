package ru.kuchanov.tproger.utils.anim;

import android.animation.Animator;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.utils.MyUIL;

/**
 * Created by Юрий on 12.12.2015 20:07.
 * For ExpListTest.
 */
public class ChangeImageWithAlpha
{
//    private final static String LOG = ChangeImageWithAlpha.class.getSimpleName();

    private Context ctx;
    private View myView;
    private ArrayList<Article> artsWithImage;
    private ImageView cover;

    public void setValues(Context ctx, final View myView, ImageView cover, ArrayList<Article> artsWithImage)
    {
        this.myView = myView;
        this.ctx = ctx;
        this.artsWithImage = new ArrayList<>(artsWithImage);
        this.cover = cover;
    }

    public void animate(final int positionInList)
    {
//        myView.setAlpha(0f);
        myView.animate().cancel();

        myView.animate().alpha(1).setDuration(600).setListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                myView.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                MyUIL.getDefault(ctx).displayImage(artsWithImage.get(positionInList).getImageUrl(), cover);
                myView.animate().alpha(0).setDuration(600).setListener(new Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(Animator animation)
                    {
//                        myView.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        myView.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation)
                    {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation)
                    {

                    }
                });

            }

            @Override
            public void onAnimationCancel(Animator animation)
            {

            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {

            }
        });
    }

    public void updateArtsList(ArrayList<Article> artsWithImage)
    {
//        Log.i(LOG, "updateArtsList with size: " + artsWithImage.size());
        this.artsWithImage = new ArrayList<>(artsWithImage);
    }
}
