package top.cusoon.picselect.album;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import top.cusoon.picselect.R;

public class ViewImageActivity extends AppCompatActivity
{
    private static final String ARG_THEME_COLOR = "param1";
    private static final String ARG_IMAGES = "param2";
    private static final String ARG_INIT_POSITION = "param3";
    private static final String ARG_SHOW_TOP_BAR = "param4";

    private ArrayList<String> data = new ArrayList<>();
    private int themeColor;
    private int initPosition;
    private boolean showTopBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout container = new FrameLayout(this);
        container.setId(R.id.wl_lib_container);
        setContentView(container, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        data = getIntent().getStringArrayListExtra(ARG_IMAGES);
        themeColor = getIntent().getIntExtra(ARG_THEME_COLOR, getResources().getColor(R.color.colorPrimary));
        initPosition = getIntent().getIntExtra(ARG_INIT_POSITION, 0);
        showTopBar = getIntent().getBooleanExtra(ARG_SHOW_TOP_BAR, true);

        getSupportFragmentManager().beginTransaction().replace(R.id.wl_lib_container, ViewImageFragment.newInstance(data, themeColor, initPosition, showTopBar)).commit();
    }

    public static class IntentBuilder
    {
        private ArrayList<String> data = new ArrayList<>();
        private int themeColor;
        private int initPosition;
        private boolean showTopBar = true;

        public IntentBuilder initPosition(int position) {
            this.initPosition = position;
            return this;
        }

        public IntentBuilder images(@NonNull List<String> data) {
            this.data.addAll(data);
            return this;
        }

        public IntentBuilder themeColor(int themeColor) {
            this.themeColor = themeColor;
            return this;
        }

        public IntentBuilder showTopBar(boolean show) {
            this.showTopBar = show;
            return this;
        }

        public Intent build(Context context) {
            if (data == null || data.size() == 0) {
                throw new RuntimeException("images can not be null");
            }

            Intent intent = new Intent(context, ViewImageActivity.class);
            intent.putExtra(ARG_SHOW_TOP_BAR, showTopBar);
            intent.putExtra(ARG_THEME_COLOR, themeColor);
            intent.putStringArrayListExtra(ARG_IMAGES, data);
            intent.putExtra(ARG_INIT_POSITION, initPosition);
            return intent;
        }
    }

}
