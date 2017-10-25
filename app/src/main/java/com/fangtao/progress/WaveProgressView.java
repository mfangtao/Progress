package com.fangtao.progress;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

/**
 * 水波浪进度框
 * 作者：涛锅锅
 * 时间：2017/10/25 14:11
 * 邮箱：7490725@qq.com
 */

public class WaveProgressView extends View{

    private Paint wavePaint;//绘制波浪画笔
    private Path wavePath;//绘制波浪Path

    private float waveWidth;//波浪宽度
    private float waveHeight;//波浪高度

    private int waveNum;//波浪组的数量（一次起伏为一组）
    private int defaultSize;//自定义View默认的宽高
    private int maxHeight;//为了看到波浪效果，给定一个比填充物稍高的高度

    private int viewSize;//重新测量后View实际的宽高

    private WaveProgressAnim waveProgressAnim;
    private float percent;//进度条占比
    private float progressNum;//可以更新的进度条数值
    private float maxNum;//进度条最大值

    private float waveMovingDistance;//波浪平移的距离

    private Paint circlePaint;//圆形进度框画笔

    private Bitmap bitmap;//缓存bitmap
    private Canvas bitmapCanvas;

    private int waveColor;//波浪颜色
    private int bgColor;//背景进度框颜色

    public WaveProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }
    private void init(Context context,AttributeSet attrs){
        waveWidth = DpOrPxUtils.dip2px(context,15);
        waveHeight = DpOrPxUtils.dip2px(context,20);

        wavePath = new Path();

        wavePaint = new Paint();
        wavePaint.setColor(Color.GREEN);
        wavePaint.setAntiAlias(true);//设置抗锯齿

        waveWidth = DpOrPxUtils.dip2px(context,20);
        waveHeight = DpOrPxUtils.dip2px(context,10);
        defaultSize = DpOrPxUtils.dip2px(context,200);
        maxHeight = DpOrPxUtils.dip2px(context,250);
        waveNum =(int) Math.ceil(Double.parseDouble(String.valueOf(defaultSize / waveWidth / 2)));//波浪的数量需要进一取整，所以使用Math.ceil函数

        percent = 0;
        progressNum = 0;
        maxNum = 100;
        waveProgressAnim = new WaveProgressAnim();

        waveMovingDistance = 0;

        wavePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));//根据绘制顺序的不同选择相应的模式即可

        circlePaint = new Paint();
        circlePaint.setColor(Color.GRAY);
        circlePaint.setAntiAlias(true);//设置抗锯齿

        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.WaveProgressView);
        waveWidth = typedArray.getDimension(R.styleable.WaveProgressView_wave_width,DpOrPxUtils.dip2px(context,25));
        waveHeight = typedArray.getDimension(R.styleable.WaveProgressView_wave_height,DpOrPxUtils.dip2px(context,5));
        waveColor = typedArray.getColor(R.styleable.WaveProgressView_wave_color,Color.GREEN);
        bgColor = typedArray.getColor(R.styleable.WaveProgressView_bg_color,Color.GRAY);
        typedArray.recycle();

        wavePaint.setColor(waveColor);

        circlePaint.setColor(bgColor);

    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = measureSize(defaultSize, heightMeasureSpec);
        int width = measureSize(defaultSize, widthMeasureSpec);
        int min = Math.min(width, height);// 获取View最短边的长度
        setMeasuredDimension(min, min);// 强制改View为以最短边为长度的正方形
        viewSize = min;
        waveNum =(int) Math.ceil(Double.parseDouble(String.valueOf(viewSize / waveWidth / 2)));
    }

    private int measureSize(int defaultSize,int measureSpec) {
        int result = defaultSize;
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);

        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == View.MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawPath(getWavePath(),wavePaint);

        //这里用到了缓存技术
        bitmap = Bitmap.createBitmap(viewSize, viewSize, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmapCanvas.drawCircle(viewSize/2, viewSize/2, viewSize/2, circlePaint);
        bitmapCanvas.drawPath(getWavePath(),wavePaint);

        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    private Path getWavePath(){
        wavePath.reset();
        //移动到右上方，也就是p0点
        wavePath.moveTo(viewSize, (1-percent)*viewSize);//让p0p1的长度随percent的增加而增加（注意这里y轴方向默认是向下的）
        //移动到右下方，也就是p1点
        wavePath.lineTo(viewSize, viewSize);
        //移动到左下边，也就是p2点
        wavePath.lineTo(0, viewSize);
        //移动到左上方，也就是p3点
//        wavePath.lineTo(0, (1-percent)*viewSize);//让p3p2的长度随percent的增加而增加（注意这里y轴方向默认是向下的）
        //从p3开始向p0方向绘制波浪曲线
        wavePath.lineTo(-waveMovingDistance, (1-percent)*viewSize);
        //从p3开始向p0方向绘制波浪曲线（曲线宽度为原来的两倍也就是波浪数量*2）
        for (int i=0;i<waveNum*2;i++){
            wavePath.rQuadTo(waveWidth/2, waveHeight, waveWidth, 0);
            wavePath.rQuadTo(waveWidth/2, -waveHeight, waveWidth, 0);
        }
        //将path封闭起来
        wavePath.close();
        return wavePath;
    }

    public class WaveProgressAnim extends Animation {
        public WaveProgressAnim(){}
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            //波浪高度到达最大值后就不需要循环了，只需让波浪曲线平移循环即可
            if(percent < progressNum / maxNum){
                percent = interpolatedTime * progressNum / maxNum;
            }
            waveMovingDistance = interpolatedTime * waveNum * waveWidth * 2;
            postInvalidate();
        }
    }

    /**
     * 设置进度条数值
     * @param progressNum 进度条数值
     * @param time 动画持续时间
     */
    public void setProgressNum(final float progressNum, int time) {
        this.progressNum = progressNum;

        percent = 0;
        waveProgressAnim.setDuration(time);
        this.startAnimation(waveProgressAnim);
        waveProgressAnim.setRepeatCount(Animation.INFINITE);//让动画无限循环
        waveProgressAnim.setInterpolator(new LinearInterpolator());//让动画匀速播放，不然会出现波浪平移停顿的现象
        waveProgressAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {
                if(percent == progressNum / maxNum){
                    waveProgressAnim.setDuration(8000);
                }
            }
        });
    }

}
