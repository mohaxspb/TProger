package ru.kuchanov.tproger.utils.anim;

import android.animation.Animator;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.ArrayList;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.robospice.db.Article;
import ru.kuchanov.tproger.utils.MyRandomUtil;
import ru.kuchanov.tproger.utils.SingltonUIL;

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
                if(artsWithImage.size()!=0)
                {
                    //TODO workaround for indexOfBoundsExeption
                    //check if pos>size and if so generate new random pos
                    int checkedPosition = positionInList;
                    if (checkedPosition >= artsWithImage.size())
                    {
                        checkedPosition = MyRandomUtil.nextInt(0, artsWithImage.size());
                    }
                    SingltonUIL.getInstance().displayImage(artsWithImage.get(checkedPosition).getImageUrl(), cover, DisplayImageOptions.createSimple());
                }
                else
                {
                     cover.setImageResource(R.drawable.tproger_small);
                }

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
