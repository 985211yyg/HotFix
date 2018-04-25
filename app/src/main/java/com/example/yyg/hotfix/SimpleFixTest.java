package com.example.yyg.hotfix;

import android.content.Context;
import android.widget.Toast;

public class SimpleFixTest {
    public void test(Context context) {
        int a = 1;
        int b = 0;
        int c = a / b;
        Toast.makeText(context, "计算结果是：" + c, Toast.LENGTH_SHORT).show();
    }
}
