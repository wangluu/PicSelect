package top.cusoon.picselect.album;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import top.cusoon.picselect.R;

/**
 * Activities that contain this fragment must implement the
 * {@link PreviewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PreviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreviewFragment extends Fragment
{
    private static final String ARG_SRC_IMAGES = "param1";
    private static final String ARG_SELECTED_IMAGES = "param2";
    private static final String ARG_POSITION = "param3";
    private static final String ARG_MAX_COUNT = "param4";
    private static final String ARG_THEME_COLOR = "param5";
    private static final int MSG_TOGGLE = 1;
    private List<String> srcImages = new ArrayList<>();
    private List<String> selectedImgs = new ArrayList<>();
    private OnFragmentInteractionListener mListener;
    private WiewPager mPager;
    private ImagePagerAdapter mAdapter;
    private WiewPager.OnPageChangeListener mPagerListener;
    private int position;

    private ViewGroup mTopBar;
    private ViewGroup mBottomBar;
    private ViewGroup mChose;
    private View mChoseCheck;

    private boolean isTopBarHidden;

    private TextView mIndicate;

    private Button mComplete;
    private int maxCount;
    private int themeColor;
    private View mRootView;

    private int toggleFlag=3;
    private Handler handler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MSG_TOGGLE:
                    if(toggleFlag!=3){
                        handler.removeMessages(MSG_TOGGLE);
                        handler.sendEmptyMessageDelayed(MSG_TOGGLE,500);
                    }else {
                        toggleFlag = 0;
                        if (isTopBarHidden) {
                            isTopBarHidden = false;
                            View v = getActivity().getWindow().getDecorView();
                            v.setSystemUiVisibility(v.getSystemUiVisibility() ^ View.SYSTEM_UI_FLAG_FULLSCREEN);
                            mTopBar.postDelayed(new Runnable()
                            {
                                @Override
                                public void run() {
                                    mTopBar.setVisibility(View.VISIBLE);
                                    mTopBar.animate().translationY(0).setListener(new AnimatorListenerAdapter()
                                    {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            toggleFlag |= 1;
                                        }
                                    }).setDuration(500).start();
                                    mBottomBar.animate().translationY(0).setListener(new AnimatorListenerAdapter()
                                    {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            toggleFlag |= 2;
                                        }
                                    }).setDuration(500).start();
                                }
                            }, 500);
                        } else {
                            isTopBarHidden = true;
                            mTopBar.animate().translationY(-mTopBar.getHeight()).setListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    toggleFlag |= 1;
                                }
                            }).setDuration(500).start();
                            mBottomBar.animate().translationY(mBottomBar.getHeight()).setListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    toggleFlag |= 2;
                                    mTopBar.setVisibility(View.INVISIBLE);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                                        //Glide.with(getActivity()).load(srcImages.get(position)).placeholder(null).into((ImageView) mAdapter.getCurrentView());
                                    }
                                }
                            }).setDuration(500).start();
                        }
                    }
                    break;
            }
            return false;
        }
    });

    /**
     * @param srcImgs      所有图片
     * @param selectedImgs 选中的图片
     * @param position     srcImgs中的位置
     * @param maxCount     最大选择数
     * @param themeColor   主题颜色
     * @return
     */
    public static PreviewFragment newInstance(@NonNull ArrayList<String> srcImgs, @Nullable ArrayList<String> selectedImgs, int position, int maxCount, int themeColor) {
        PreviewFragment fragment = new PreviewFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_SRC_IMAGES, srcImgs);
        args.putStringArrayList(ARG_SELECTED_IMAGES, selectedImgs);
        args.putInt(ARG_POSITION, position);
        args.putInt(ARG_MAX_COUNT, maxCount);
        args.putInt(ARG_THEME_COLOR, themeColor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            List<String> temp = getArguments().getStringArrayList(ARG_SELECTED_IMAGES);
            if (temp != null) {
                selectedImgs.addAll(temp);
            }
            temp = getArguments().getStringArrayList(ARG_SRC_IMAGES);
            if (temp != null) {
                srcImages.addAll(temp);
            }
            position = getArguments().getInt(ARG_POSITION, 0);
            maxCount = getArguments().getInt(ARG_MAX_COUNT, 0);
            themeColor = getArguments().getInt(ARG_THEME_COLOR, getResources().getColor(R.color.colorPrimary));
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preview, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        mRootView = view;
        mPager = (WiewPager) view.findViewById(R.id.wl_lib_vp);
        mTopBar = (ViewGroup) view.findViewById(R.id.wl_lib_top_bar);
//        Rect rectangle= new Rect();
//        Window window= getActivity().getWindow();
//        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
//        int statusBarHeight= rectangle.top;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mTopBar.getLayoutParams();
        lp.topMargin = SelectPictureActivity.getStatusBarHeight(getActivity());
        mTopBar.setLayoutParams(lp);
        mTopBar.setVisibility(View.VISIBLE);


        mTopBar.setBackgroundColor(Color.argb(0xdd, Color.red(themeColor), Color.green(themeColor), Color.blue(themeColor)));
        mBottomBar = (ViewGroup) view.findViewById(R.id.wl_lib_bottom_bar);
        mBottomBar.setBackgroundColor(Color.argb(0xaa, Color.red(themeColor), Color.green(themeColor), Color.blue(themeColor)));
        mChose = (ViewGroup) view.findViewById(R.id.wl_lib_vg_chose);
        mChoseCheck = view.findViewById(R.id.wl_lib_iv_check);
        mIndicate = (TextView) view.findViewById(R.id.wl_lib_tv_indicate);
        view.findViewById(R.id.wl_lib_ib_back).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        mComplete = (Button) view.findViewById(R.id.wl_lib_btn_complete);
        mComplete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCompleteClick();
                }
            }
        });
        mChoseCheck.setSelected(selectedImgs.contains(srcImages.get(position)));

        mChose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                String image = srcImages.get(mPager.getCurrentItem());
                if (mChoseCheck.isSelected()) {
                    mChoseCheck.setSelected(false);
                    selectedImgs.remove(image);
                } else {
                    if (selectedImgs.size() >= maxCount) {
                        Toast.makeText(getActivity(), String.format(getString(R.string.wl_lib_max_count_alert), maxCount+""), Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        mChoseCheck.setSelected(true);
                        selectedImgs.add(image);
                    }
                }

                refreshBtnCompleteState();
                if (mListener != null) {
                    mListener.onSelectChanged(selectedImgs);
                }
            }
        });

        if (srcImages != null && srcImages.size() > 0) {
            mIndicate.setText(String.format(getString(R.string.wl_lib_indicate), position + 1, srcImages.size()));
        }
        refreshBtnCompleteState();
        mAdapter = new ImagePagerAdapter(getActivity(), srcImages);
        mAdapter.setListener(new ImagePagerAdapter.OnImageClickListener()
        {
            @Override
            public void onImageClick(final int position) {
                handler.sendEmptyMessage(MSG_TOGGLE);
            }
        });
//        mPager.postDelayed(new Runnable()
//        {
//            @Override
//            public void run() {
//           mPager.setAdapter(mAdapter);
//            }
//        },100);
        mPager.setAdapter(mAdapter);

        mPagerListener = new WiewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mChoseCheck.setSelected(selectedImgs.contains(srcImages.get(position)));
                mIndicate.setText(String.format(getString(R.string.wl_lib_indicate), position + 1, srcImages.size()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        mPager.addOnPageChangeListener(mPagerListener);
        mPager.setCurrentItem(position);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isTopBarHidden) {
            // v.setSystemUiVisibility(v.getSystemUiVisibility()^View.SYSTEM_UI_FLAG_FULLSCREEN);
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

    }

    private void refreshBtnCompleteState() {
        if (selectedImgs == null || selectedImgs.size() == 0) {
            mComplete.setEnabled(false);
            mComplete.setText(getResources().getString(R.string.wl_lib_complete));
        } else {
            mComplete.setEnabled(true);
            mComplete.setText(String.format(getResources().getString(R.string.wl_lib_complete_count), selectedImgs.size(), maxCount));
        }
    }

    @Override
    public void onDestroyView() {
        if (mPager != null) {
            mPager.removeOnPageChangeListener(mPagerListener);
        }
        int v = mRootView.getSystemUiVisibility();
        v = v ^ View.SYSTEM_UI_FLAG_FULLSCREEN & (~View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        mRootView.setSystemUiVisibility(v);

        super.onDestroyView();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener
    {
        void onSelectChanged(List<String> selectedImgs);

        void onCompleteClick();
    }

}
