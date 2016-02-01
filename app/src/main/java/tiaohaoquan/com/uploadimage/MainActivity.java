package tiaohaoquan.com.uploadimage;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button select, upload;
    TextView result;
    ImageView image;
    int REQUEST_CODE_OPENPICS = 11;
    int REQUEST_CODE_CAMERA = 22;
    String path;//照片实际路径
    String tempImgName;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }

    private void initView() {
        select = (Button) findViewById(R.id.select);
        upload = (Button) findViewById(R.id.upload);
        result = (TextView) findViewById(R.id.result);
        image = (ImageView) findViewById(R.id.image);

        select.setOnClickListener(this);
        upload.setOnClickListener(this);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        tempImgName = getImageSaveName();
       file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"天好圈");
        if (!file.exists()) {
            file.mkdir();
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(file, tempImgName)));
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    private void openPhones() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);//从列表中选择某项并返回所选数据
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");//得到系统图片的信息
        startActivityForResult(intent, REQUEST_CODE_OPENPICS);
    }

    public String getImageSaveName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String name = sdf.format(new java.util.Date()) + ".jpg";
        return name;
    }

    public String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    private void uploadimages() {
        String url = "http://192.168.10.99:8080/wodangjia/Upload";
        try {
            HttpPostUtil httpPostUtil = new HttpPostUtil(url);
            httpPostUtil.setContentType("image/jpeg");
//            httpPostUtil.addTextParameter("PARAMS", "add_goods");//服务端判断,根据自己服务端添加
            httpPostUtil.addFileParameter("upload", new File(path));
            httpPostUtil.send();
            Log.e("-----------",path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
//            Uri uriPhones = data.getData();
//            path = getRealFilePath(this, uriPhones);
//            image.setImageURI(uriPhones);
            File file1 = new File(file, tempImgName);
            Uri uriCamera = Uri.fromFile(file1);
            path = getRealFilePath(this, uriCamera);
            image.setImageURI(uriCamera);
            MediaScannerConnection.scanFile(this,
                    new String[]{file.getPath()}, null, null);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select:
                openCamera();
                break;
            case R.id.upload:
                uploadimages();
                break;
            default:
                break;
        }
    }
}
