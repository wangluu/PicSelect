package top.cusoon.picselect.album;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import top.cusoon.picselect.R;
import top.cusoon.picselect.tools.AlbumTools;
import top.cusoon.picselect.tools.WUtils;

public class SelectPictureActivity extends AppCompatActivity implements View.OnClickListener, PreviewFragment.OnFragmentInteractionListener
{
    /**
     * 返回的图片参数名
     */
    public static final String IMAGES = "result_imgs";
    public static final String ARG_SELECTED = "selected_imgs";
    public static final String ARG_MAX_COUNT = "max_select_count";
    public static final String ARG_THEME_COLOR= "theme_color";

    private int maxCount = 9;
    private int themeColor;

    private ArrayList<String> selectedImgs;
    private final int REQUEST_CODE_CAPTURE = 1;

    private ViewGroup mTopBar;
    private ViewGroup mBottomBar;

    private AsyncTask<Void, Void, List<ImageBucket>> task;
    private GridView mGridView;
    private ImageGridAdapter mGridAdapter;

    private Button mComplete;
    private Button mDir;
    private TextView mPreview;
    private View mContainer;


    private ListView mDirListView;
    private DirAdapter mDirAdapter;

    private boolean isDirShowed;

    private Uri photoUri;
    private ContentObserver observer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_picture);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setupViews();
        refreshData();
        observer = new ContentObserver(new Handler())
        {
            @Override
            public void onChange(boolean selfChange) {
                refreshData();
            }
        };
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        selectedImgs.clear();
        ArrayList<String> argSelectedImgs = intent.getStringArrayListExtra(ARG_SELECTED);
        int argMaxCount = intent.getIntExtra(ARG_MAX_COUNT, -1);
        if (argSelectedImgs != null && argSelectedImgs.size() > 0) {
            selectedImgs.addAll(argSelectedImgs);
        }

        if (argMaxCount != -1) {
            maxCount = argMaxCount;
        }
        refreshBtnCompleteState();
        int color =  intent.getIntExtra(ARG_THEME_COLOR, -1);
        themeColor =color ==-1?getResources().getColor(R.color.colorPrimary):color;
        mTopBar.setBackgroundColor(themeColor);
        mBottomBar.setBackgroundColor((0xaa<<24)|(themeColor&0xffffff));
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed},new ColorDrawable((0x33<<24)|(themeColor&0xffffff)));
        drawable.addState(new int[]{},new ColorDrawable(Color.TRANSPARENT));
        mDirListView.setSelector(drawable);
        mDirAdapter.setThemeColor(themeColor);
    }
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId =context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    private void setupViews() {
        //       View root = findViewById(R.id.root);
//        Rect rectangle= new Rect();
//        Window window= getWindow();
//        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
//        int statusBarHeight= rectangle.top;
//        Log.e("sssss",statusBarHeight+"");
//        root.setPadding(0,getStatusBarHeight(),0,0);

        View root = findViewById(R.id.root);
       ViewGroup.MarginLayoutParams mp = (ViewGroup.MarginLayoutParams) root.getLayoutParams();
        mp.topMargin = getStatusBarHeight(this);
        root.setLayoutParams(mp);


        mTopBar = (ViewGroup) findViewById(R.id.wl_lib_top_bar);
        mBottomBar = (ViewGroup) findViewById(R.id.wl_lib_rl);
        mBottomBar.setOnClickListener(this);
        findViewById(R.id.wl_lib_ib_back).setOnClickListener(this);
        mGridView = (GridView) findViewById(R.id.wl_lib_gv_album);



        mComplete = (Button) findViewById(R.id.wl_lib_btn_complete);
        mDir = (Button) findViewById(R.id.wl_lib_btn_dir);
        mContainer = findViewById(R.id.wl_lib_fl_container);
        mDirListView = (ListView) findViewById(R.id.wl_lib_lv_dir);
        mContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                mDirListView.setTranslationY(mContainer.getHeight());
            }
        });
        mContainer.setOnClickListener(this);
        mContainer.setClickable(false);
        mComplete.setOnClickListener(this);
        mPreview = (TextView) findViewById(R.id.wl_lib_btn_preview);
        mPreview.setOnClickListener(this);
        refreshBtnCompleteState();
        mDir.setOnClickListener(this);

        selectedImgs = new ArrayList<>();
        mGridAdapter = new ImageGridAdapter(this);
        mGridAdapter.setSelectedImgs(selectedImgs);
        mGridView.setAdapter(mGridAdapter);
        mGridAdapter.setListener(new ImageGridAdapter.ImageGridAdapterListener()
        {
            @Override
            public void onCaptureClick() {
                if (selectedImgs.size() >= maxCount) {
                    Toast.makeText(SelectPictureActivity.this, String.format(getString(R.string.wl_lib_max_count_alert), maxCount + ""), Toast.LENGTH_SHORT).show();
                } else {
                    capture(REQUEST_CODE_CAPTURE);
                }
            }

            @Override
            public void onSelectClicked(int position, ImageView slc, View shadow, ImageItem item) {
                if (selectedImgs == null) {
                    selectedImgs = new ArrayList<>();
                }

                if (selectedImgs.contains(item.imagePath)) {
                    selectedImgs.remove(item.imagePath);
                    slc.setSelected(false);
                    shadow.setVisibility(View.GONE);
                    refreshBtnCompleteState();
                } else {
                    if (selectedImgs.size() >= maxCount) {
                        Toast.makeText(SelectPictureActivity.this, String.format(getString(R.string.wl_lib_max_count_alert), maxCount + ""), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    slc.setSelected(true);
                    shadow.setVisibility(View.VISIBLE);
                    selectedImgs.add(item.imagePath);
                    refreshBtnCompleteState();
                }
            }

            @Override
            public void onImageClicked(int position) {
                List<ImageItem> data = mGridAdapter.getData();
                ArrayList<String> srcImgs= new ArrayList<String>();
                int i=0;
                //拍照item
                if("capture".equals(data.get(0).imageId))
                {
                    i=1;
                    position-=1;
                }
                for(;i<data.size();i++)
                {
                    srcImgs.add(data.get(i).imagePath);
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.wl_lib_fgmnt_container, PreviewFragment.newInstance(srcImgs,selectedImgs,position,maxCount,themeColor), "preview").setTransition(FragmentTransaction.TRANSIT_NONE).addToBackStack("preview").commit();
            }
        });


        mDirAdapter = new DirAdapter(this);
        mDirListView.setAdapter(mDirAdapter);
        mDirListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    toggleDir();
                }
                ImageBucket item = mDirAdapter.getItem(position);
                mDirAdapter.setCheckedItem(position);
                mDirAdapter.notifyDataSetChanged();
                mDir.setText(item.bucketName);
                mGridAdapter.setData(item.imageList);
                mGridAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (task != null) {
            task.cancel(true);
        }
        if(observer!=null) {
            getContentResolver().unregisterContentObserver(observer);
        }
        super.onDestroy();
    }

    private void refreshData() {
        if (task != null) {
            task.cancel(true);
        }
        mGridAdapter.notifyDataSetInvalidated();
        mDirAdapter.notifyDataSetInvalidated();
        task = new AsyncTask<Void, Void, List<ImageBucket>>()
        {
            @Override
            protected List<ImageBucket> doInBackground(Void... params) {
                return AlbumTools.buildImagesBuckets(getContentResolver());
            }

            @Override
            protected void onPostExecute(List<ImageBucket> imageBuckets) {
                refreshView(imageBuckets);
            }
        };
        task.execute();
    }


    void refreshView(List<ImageBucket> imageBuckets) {
        task = null;
        ImageBucket old = mDirAdapter.getItem(mDirAdapter.getCheckedItem());
        mDirAdapter.setData(imageBuckets);
        if (imageBuckets == null || imageBuckets.size() == 0) {
            mDirAdapter.notifyDataSetChanged();
            mGridAdapter.setData(null);
            mGridAdapter.notifyDataSetChanged();
        } else {
            List<ImageItem> first = imageBuckets.get(0).imageList;
            if (first != null) {
                ImageItem capture = new ImageItem();
                capture.imageId = "capture";
                first.add(0, capture);
            }


            boolean find = false;
            if (old != null) {
                for (ImageBucket b : imageBuckets) {
                    if (b != null &&b.bucketId!=null &&  b.bucketId.equals( old.bucketId)) {
                        find = true;
                        int index = imageBuckets.indexOf(b);
                        mDirListView.performItemClick(null, index, mDirAdapter.getItemId(index));
                    }
                }
            }
            if (!find) {
                mDirListView.performItemClick(null, 0, mDirAdapter.getItemId(0));
            }

        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.wl_lib_ib_back) {
            onBackPressed();
        } else if (v.getId() == R.id.wl_lib_btn_dir) {
            toggleDir();
        } else if (v.getId() == R.id.wl_lib_btn_complete) {
            complete();
        } else if (v.getId() == R.id.wl_lib_fl_container) {
            toggleDir();
        } else if (v.getId() == R.id.wl_lib_btn_preview) {
            getSupportFragmentManager().beginTransaction().replace(R.id.wl_lib_fgmnt_container, PreviewFragment.newInstance(selectedImgs,selectedImgs,0,maxCount,themeColor), "preview").setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack("preview").commit();
        }
    }

    private void complete() {
        Intent data = new Intent();
        data.putStringArrayListExtra(IMAGES, selectedImgs);
        setResult(RESULT_OK, data);
        finish();
    }

    private void toggleDir() {
        if (isDirShowed) {
            isDirShowed = false;
            mContainer.setClickable(false);
            mDirListView.animate().translationY(mDirListView.getHeight()).setDuration(300).start();
            mContainer.animate().alpha(0).setDuration(300).start();
        } else {
            isDirShowed = true;
            mContainer.setClickable(true);
            mDirListView.animate().translationY(0).setDuration(300).start();
            mContainer.animate().alpha(1).setDuration(300).start();
        }
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (!getSupportFragmentManager().popBackStackImmediate()) {
            if (isDirShowed) {
                toggleDir();
            } else {
                super.onBackPressed();
            }
        }else{
//            View v =getWindow().getDecorView();
//            int a = v.getSystemUiVisibility();
//             a =a&(~View.SYSTEM_UI_FLAG_FULLSCREEN );
//            v.setSystemUiVisibility(a);
        }
    }

    private void refreshBtnCompleteState() {
        if (selectedImgs == null || selectedImgs.size() == 0) {
            mComplete.setEnabled(false);
            mComplete.setText(getResources().getString(R.string.wl_lib_complete));
            mPreview.setEnabled(false);
            mPreview.setText(getResources().getString(R.string.wl_lib_preview));
        } else {
            mComplete.setEnabled(true);
            mComplete.setText(String.format(getResources().getString(R.string.wl_lib_complete_count), selectedImgs.size(), maxCount));
            mPreview.setEnabled(true);
            mPreview.setText(String.format(getResources().getString(R.string.wl_lib_preview_count), selectedImgs.size(), maxCount));
        }
    }

    private void capture(int requestCode) {
        // 执行拍照前，应该先判断SD卡是否存在
        String SDState = Environment.getExternalStorageState();
        if (SDState.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// "android.media.action.IMAGE_CAPTURE"
            /***
             * 需要说明一下，以下操作使用照相机拍照，拍照后的图片会存放在相册中的 这里使用的这种方式有一个好处就是获取的图片是拍照后的原图
             * 如果不实用ContentValues存放照片路径的话，拍照后获取的图片为缩略图不清晰
             */
            ContentValues values = new ContentValues();

            photoUri = this.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            /** ----------------- */
            try {
                startActivityForResult(intent, requestCode);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "没有相机应用", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, e.getClass().getSimpleName(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "SD卡不存在", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CAPTURE) {
            String path = WUtils.getPathByUri(this, (photoUri));
            Log.e("ssssss",path+"------"+(new File(path)).exists());

            if (!TextUtils.isEmpty(path)) {
                // 让保存到相册中的图片马上可见，不然需要重启才可见
                MediaScannerConnection.scanFile(this, new String[]{path},
                        null,
                        new MediaScannerConnection.OnScanCompletedListener()
                        {
                            public void onScanCompleted(String path, Uri uri) {
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run() {
                                        refreshData();
                                    }
                                });
                            }
                        });
                selectedImgs.add(WUtils.getPathByUri(this, (photoUri)));
                refreshBtnCompleteState();
            }

        }

    }

    @Override
    public void onSelectChanged(List<String> selected) {
        try {
            if(selectedImgs!=null && selected!=null)
            {
                selectedImgs.clear();
                selectedImgs.addAll(selected);
                mGridAdapter.notifyDataSetChanged();
                refreshBtnCompleteState();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompleteClick() {
        complete();
    }

    public static class IntentBuilder{
        private int maxCount = 9;
        private ArrayList<String> data = new ArrayList<>();
        private int themeColor;

        public IntentBuilder  maxCount(int maxCount)
        {
            this.maxCount = maxCount;
            return this;
        }
        public IntentBuilder  selectedImages(@NonNull List<String> data)
        {
            this.data.addAll(data);
            return this;
        }

        public IntentBuilder  themeColor(int themeColor)
        {
            this.themeColor = themeColor;
            return this;
        }

        public Intent build(Context context)
        {
            Intent intent = new Intent(context,SelectPictureActivity.class);
            intent.putExtra(ARG_MAX_COUNT,maxCount);
            intent.putExtra(ARG_THEME_COLOR,themeColor);
            intent.putStringArrayListExtra(ARG_SELECTED,data);
            return intent;
        }
    }
}
