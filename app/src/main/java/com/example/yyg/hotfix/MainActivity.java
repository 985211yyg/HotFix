package com.example.yyg.hotfix;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 123;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //需要解释
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                //不需要解释，请求权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //权限请求成功
                } else {
                    //权限不拒绝
                }
        }

    }

    public void onCalculate(View view) {
        SimpleFixTest test = new SimpleFixTest();
        test.test(this);

    }

    /**
     * 修复
     *
     * @param view
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException
     */
    public void onFix(View view) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        FixUtils.loadFixedDex(this, Environment.getExternalStorageDirectory());
    }
}
