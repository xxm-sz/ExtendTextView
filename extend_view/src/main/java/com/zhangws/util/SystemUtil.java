package com.zhangws.util;

import android.content.Context;

public class SystemUtil {

    public static int dp2px(Context context, float dpValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5);
    }

}
