package top.cusoon.picselect.album;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.SharedElementCallback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import top.cusoon.picselect.R;

/**
 * Use the {@link ViewImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewImageFragment extends Fragment
{
    private static final String ARG_THEME_COLOR = "param1";
    private static final String ARG_IMAGES = "param2";
    private static final String ARG_INIT_POSITION = "param3";
    private static final String ARG_SHOW_TOP_BAR = "param4";

    private List<String> mImages;
    private int themeColor;
    private int initPosition;
    private boolean showTopBar;

    private TextView mTextView;
    private WiewPager mViewPager;
    private WiewPager.OnPageChangeListener pageListener;
    private ImagePagerAdapter mAdapter;

    private ViewGroup mTopBar;
    private boolean isExit;

    private OnFragmentInteractionListener mListenr;
    private SharedElementCallback mCallback = new SharedElementCallback()
    {
        @Override
        public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
            Log.e("ViewImage","onSharedElementStart");
        }

        @Override
        public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
            Log.e("ViewImage","onSharedElementEnd");
        }

        @Override
        public void onRejectSharedElements(List<View> rejectedSharedElements) {
            Log.e("ViewImage","onRejectSharedElements");
        }

        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            Log.e("ViewImage","onMapSharedElements"+mViewPager);
            if(mViewPager!=null && mViewPager.getCurrentItem()!=initPosition  && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                View sharedElement = mAdapter.getCurrentView();
                Log.e("ViewImage","onMapSharedElements"+sharedElement);
                if(sharedElement!=null) {
                    names.clear();
                    sharedElements.clear();
                    sharedElements.put(sharedElement.getTransitionName(), sharedElement);
                    names.add(sharedElement.getTransitionName());
                }
            }
        }

        @Override
        public Parcelable onCaptureSharedElementSnapshot(View sharedElement, Matrix viewToGlobalMatrix, RectF screenBounds) {
            Log.e("ViewImage","onCaptureSharedElementSnapshot");
            return super.onCaptureSharedElementSnapshot(sharedElement, viewToGlobalMatrix, screenBounds);
        }

        @Override
        public View onCreateSnapshotView(Context context, Parcelable snapshot) {
            Log.e("ViewImage","onCreateSnapshotView");
            return super.onCreateSnapshotView(context, snapshot);
        }
    };


    public static ViewImageFragment newInstance(@NonNull ArrayList<String> images, int themeColor, int initPosition, boolean showTopBar) {
        ViewImageFragment fragment = new ViewImageFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_IMAGES,images);
        args.putInt(ARG_THEME_COLOR,themeColor);
        args.putInt(ARG_INIT_POSITION, initPosition);
        args.putBoolean(ARG_SHOW_TOP_BAR, showTopBar);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e("ViewImage","onCreate");
        super.onCreate(savedInstanceState);
        setEnterSharedElementCallback(mCallback);
        if (getArguments() != null) {
            mImages = getArguments().getStringArrayList(ARG_IMAGES);
            themeColor = getArguments().getInt(ARG_THEME_COLOR);
            initPosition = getArguments().getInt(ARG_INIT_POSITION);
            showTopBar = getArguments().getBoolean(ARG_SHOW_TOP_BAR);
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_image, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.wl_lib_ib_back).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        mTopBar = (ViewGroup) view.findViewById(R.id.wl_lib_top_bar);
        if(showTopBar) {
            mTopBar.setBackgroundColor(themeColor);
        }else
        {
            mTopBar.setVisibility(View.GONE);
        }

        mTextView = (TextView) view.findViewById(R.id.wl_lib_tv_text);
        mTextView.setText(String.format(getString(R.string.wl_lib_indicate),initPosition+1,mImages.size()));
        mViewPager = (WiewPager) view.findViewById(R.id.wl_lib_vp_images);
        mAdapter = new ImagePagerAdapter(getActivity(),mImages);
        mViewPager.setAdapter(mAdapter);
        pageListener = new WiewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                mTextView.setText((position+1)+"/"+mAdapter.getCount());
                if(mListenr!=null)
                {
                    mListenr.onItemSelected(position);
                }
            }
        };
        mViewPager.addOnPageChangeListener(pageListener);
        if(initPosition!=0)
        mViewPager.setCurrentItem(initPosition);

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener()
        {
            @Override
            public void onBackStackChanged() {
                Log.e("ViewImage","onBackStackChanged");
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof  OnFragmentInteractionListener)
        {
            mListenr = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onResume() {
        Log.e("ViewImage","onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.e("ViewImage","onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e("ViewImage","onStop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
       isExit = true;
        Log.e("ViewImage","onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.e("ViewImage","onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.e("ViewImage","onDetach");
        if(mViewPager!=null)
        {
            mViewPager.removeOnPageChangeListener(pageListener);
        }
        super.onDetach();
    }

    public interface  OnFragmentInteractionListener{
        void onItemSelected(int position);
    }

}
