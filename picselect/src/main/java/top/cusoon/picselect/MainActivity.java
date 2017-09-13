package top.cusoon.picselect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.List;
import java.util.Random;

import top.cusoon.picselect.album.SelectPictureActivity;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_select).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                int c = new Random().nextInt(0xffffff) | (0xff << 24);
                startActivityForResult(new SelectPictureActivity.IntentBuilder().maxCount(9).themeColor(c).build(MainActivity.this), 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK && requestCode == 1){
            List<String> imgPaths = data.getStringArrayListExtra(SelectPictureActivity.IMAGES);
            //To do what you want
        }
    }
}
