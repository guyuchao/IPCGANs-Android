package com.example.myapplication2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2; /*用来记录录像存储路径*/
    File file = new File(Environment.getExternalStorageDirectory().getPath() + "/video.mp4");//设置录像存储路径
    Uri uri = Uri.fromFile(file);//文件转成Uri格式

    @Override protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState); setContentView(R.layout.mainactivity);
        Button takePhoto = (Button) findViewById(R.id.take_photo);
        Button chooseFromAlbum = (Button) findViewById(R.id.choose_from_album);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // 在新的Intent里面打开，并且传递TAKE_PHOTO选
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, image_album_showActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("id", TAKE_PHOTO);
                //使用显式Intent传递参数，用以区分功能
                intent.putExtras(bundle);
                MainActivity.this.startActivity(intent);//启动新的Intent
            }
        });

        //设置相册选择的响应
        chooseFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在新的Intent里面打开，并且传递CHOOSE_PHOTO选项
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, image_album_showActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("id", CHOOSE_PHOTO);
                intent.putExtras(bundle);
                MainActivity.this.startActivity(intent);
            }
        });
    }
}