package com.example.myapplication2;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;


public class image_album_showActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {


    private ImageView picture,res_image;

    private Uri imageUri;
    private Button Return_page;
    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2; //接受前一个Intent传入的id
    private Bundle bundle;
    private int Show_Choice;
    private String upload_url="http://10.0.3.2:5000/upload_file";
    private String getimage_url="http://10.0.3.2:5000/get_image/";

    private RadioGroup AgeGroup;
    private String age_id="20";

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int id) {
        // TODO Auto-generated method stub
        switch (id) {
            case R.id.age20:
                age_id = "0";
                break;

            case R.id.age30:
                age_id = "1";
                break;

            case R.id.age40:
                age_id = "2";
                break;

            case R.id.age50:
                age_id = "3";
                break;

            case R.id.age60:
                age_id = "4";
                break;

            default:
                break;
        }
        String url_tmp=getimage_url+age_id;
        OkHttpUtils.get()     // 请求方式和请求url
                .url(url_tmp)
                .tag(this)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onResponse(String response, int id) {
                        //result.setText(response);
                        res_image.setImageBitmap(base64ToBitmap(response));
                        //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                    }
                });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_album_show);
        picture = (ImageView) findViewById(R.id.V_Image);
        res_image = (ImageView) findViewById(R.id.result);
        Return_page = (Button) findViewById(R.id.Return_Back_to_page1);
        bundle = this.getIntent().getExtras();
        Show_Choice = bundle.getInt("id");
        AgeGroup = (RadioGroup) findViewById(R.id.age_group);
        AgeGroup.setOnCheckedChangeListener(this);
        Return_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(image_album_showActivity.this, MainActivity.class);//也可以这样写intent.setClass(MainActivity.this, OtherActivity.class);
                startActivity(intent);
            }
        });

        switch (Show_Choice) {
            //如果传递为TAKE_PHOTO
            case TAKE_PHOTO: {
                File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //判断版本号
                if (Build.VERSION.SDK_INT < 24) {
                    imageUri = Uri.fromFile(outputImage);
                } else {
                    imageUri = FileProvider.getUriForFile(image_album_showActivity.this, "com.MapScanner.MapScanner", outputImage);
                }
                // 启动相机程序
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, 1);
                try {// 将拍摄的照片显示出来
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    picture.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;

            case CHOOSE_PHOTO: {
                //如果没有权限则申请权限
                if (ContextCompat.checkSelfPermission(image_album_showActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(image_album_showActivity.this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } //调用打开相册
                openAlbum();
            }

            default:
                break;
        }
    }

    private void openAlbum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO); // 打开相册
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                } break;
                default: break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (Show_Choice) {
            case 1:
                try {// 将拍摄的照片显示出来
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    picture.setImageBitmap(bitmap);
                } catch (Exception e) {
                e.printStackTrace();
            } break;
            case 2: // 判断手机系统版本号
                if (Build.VERSION.SDK_INT >= 19) { // 4.4及以上系统使用这个方法处理图片
                    if(data!=null) {
                        handleImageOnKitKat(data);
                    }
                } else { // 4.4以下系统使用这个方法处理图片
                    handleImageBeforeKitKat(data);
                }
                break;
                default: break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        Log.d("TAG", "handleImageOnKitKat: uri is " + uri);

        if (DocumentsContract.isDocumentUri(this, uri)) { // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            }
            else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                if (docId.startsWith("raw:")) {
                    imagePath = docId.replaceFirst("raw:", "");
                }
                else {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                    imagePath = getImagePath(contentUri, null);
                }
            }
        }
        else if ("content".equalsIgnoreCase(uri.getScheme())) { // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) { // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }

        displayImage(imagePath); // 根据图片路径显示图片
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null; // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }

        return path;
    }

    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            File file = new File(imagePath);
            if (!file.exists()) {
                Toast.makeText(this, "error", Toast.LENGTH_LONG).show();
                return;
            }
            else {
                String filename = file.getName();
                Toast.makeText(this, "begin", Toast.LENGTH_LONG).show();

                OkHttpUtils.post()//
                        .addFile("img", filename, file)
                        .url(upload_url)
                        .build()//
                        .connTimeOut(20000)
                        .readTimeOut(20000)
                        .writeTimeOut(20000)
                        .execute(new StringCallback() {
                                    @Override
                                    public void onError(Call call, Exception e, int id) {
                                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                    @Override
                                    public void onResponse(String response, int id) {
                                        //result.setText(response);
                                        res_image.setImageBitmap(base64ToBitmap(response));
                                        //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                                    }
                                });

                //Toast.makeText(this, imagePath, Toast.LENGTH_LONG).show();
            }
            picture.setImageBitmap(bitmap);
        }
        else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }




}

