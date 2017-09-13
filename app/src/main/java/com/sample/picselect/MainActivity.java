package com.sample.picselect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Random;

import top.cusoon.picselect.album.SelectPictureActivity;
import top.cusoon.picselect.album.ViewImageActivity;
import top.cusoon.picselect.view.SquareImageView;



public class MainActivity extends AppCompatActivity
{
    private RecyclerView rv;
    private RvAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rv = (RecyclerView) findViewById(R.id.rv);

        findViewById(R.id.btn_select).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                int c = new Random().nextInt(0xffffff) | (0xff << 24);
                startActivityForResult(new SelectPictureActivity.IntentBuilder().maxCount(9).themeColor(c).build(MainActivity.this), 1);
            }
        });

        rv.setLayoutManager(new GridLayoutManager(this,3));
        mAdapter = new RvAdapter();
        rv.setAdapter(mAdapter);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode ==Activity.RESULT_OK&& requestCode == 1 ){
            List<String> imgPaths = data.getStringArrayListExtra(SelectPictureActivity.IMAGES);
            mAdapter.setData(imgPaths);
            mAdapter.notifyDataSetChanged();
        }
    }

    public class RvAdapter extends RecyclerView.Adapter{
        private List<String>  data;
        public RvAdapter(){
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView v = new SquareImageView(MainActivity.this);
            v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            v.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new RecyclerView.ViewHolder(v)
            {
            };
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            Glide.with(MainActivity.this).load(data.get(position)).into((ImageView) holder.itemView);
            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                    int c = new Random().nextInt(0xffffff) | (0xff << 24);
                    Intent intent = new ViewImageActivity.IntentBuilder().images(data).initPosition(holder.getAdapterPosition()).themeColor(c).showTopBar(true).build(MainActivity.this);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return data ==null?0:data.size();
        }

        public List<String> getData() {
            return data;
        }

        public void setData(List<String> data) {
            this.data = data;
        }
    }


}
