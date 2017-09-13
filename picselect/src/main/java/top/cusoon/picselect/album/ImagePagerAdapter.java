package top.cusoon.picselect.album;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by l.wang on 2016/6/29.
 */
public class ImagePagerAdapter extends PagerAdapter
{
    private List<String> images;
    private Context context;
    private View currentView;

    ImagePagerAdapter(Context context, List<String> images)
    {
        this.context = context;
        this.images = images;
    }


    @Override
    public int getCount() {
        return images ==null?0:images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
//        final PhotoView iv= new PhotoView(context);
        WImgView iv = new WImgView(context);
//        iv.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener()
//        {
//            @Override
//            public void onViewTap(View view, float v, float v1) {
//                if(listener!=null)
//                {
//                    listener.onImageClick(position);
//                }
//            }
//        });
        iv.setDoubleTapListener(new WImgView.DoubleTapListener()
        {
            @Override
            public void onDoubleTap(MotionEvent ev) {

            }

            @Override
            public void onSingleTapConfirmed(MotionEvent ev) {
//                Toast.makeText(context,"onSingleTapConfirmed",Toast.LENGTH_LONG).show();
                if(listener!=null)
                {
                    listener.onImageClick(position);
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            iv.setTransitionName("image_"+position);
        }

   //     iv.setScaleType(ImageView.ScaleType.CENTER);
        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Glide.with(context).load(images.get(position)).placeholder(null).into(iv);
        container.addView(iv);
        return iv;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        currentView = (View) object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if(object instanceof View)
        {
            container.removeView((View) object);
        }
    }

    private OnImageClickListener listener;

    public OnImageClickListener getListener() {
        return listener;
    }

    public void setListener(OnImageClickListener listener) {
        this.listener = listener;
    }

    public View getCurrentView() {
        return currentView;
    }


    public interface OnImageClickListener
    {
        void onImageClick(int position);
    }

}
