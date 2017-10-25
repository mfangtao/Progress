package com.fangtao.progress;

import android.content.Context;

/**
 * dp和px相互转换的工具类
 * 作者：涛锅锅
 * 时间：2017/10/25 14:16
 * 邮箱：7490725@qq.com
 */

public class DpOrPxUtils {
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
