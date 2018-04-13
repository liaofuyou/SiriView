package me.ajax.siriview.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Random;

/**
 * Created by aj on 2018/4/2
 */

public class SiriView extends View {

    //三条曲线上的点
    AJPoint[] rowOnePoints = new AJPoint[11];
    AJPoint[] rowTwoPoints = new AJPoint[11];
    AJPoint[] rowThreePoints = new AJPoint[11];

    //四支画笔们
    Paint rowOnePaint;
    Paint rowTwoPaint;
    Paint rowThreePaint;
    Paint linePaint;

    //根据三条曲线上的点划出三根线
    Path rowOnePath = new Path();
    Path rowTwoPath = new Path();
    Path rowThreePath = new Path();

    //一个随机数
    Random random = new Random();

    //屏幕宽度
    int screenWidth = getResources().getDisplayMetrics().widthPixels;

    //振幅高度
    float itemHeight = dp2Dx(40);

    public SiriView(Context context) {
        super(context);
        init();
    }

    public SiriView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SiriView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);//关闭硬件加速

        //画笔
        rowOnePaint = new Paint();
        rowOnePaint.setColor(0xFFFF00FF);
        rowOnePaint.setStrokeWidth(5);
        rowOnePaint.setTextSize(dp2Dx(14));
        rowOnePaint.setStyle(Paint.Style.STROKE);
        rowOnePaint.setPathEffect(new CornerPathEffect(dp2Dx(20)));
        rowOnePaint.setStyle(Paint.Style.FILL);

        rowTwoPaint = new Paint(rowOnePaint);
        rowThreePaint = new Paint(rowOnePaint);

        rowOnePaint.setShader(new LinearGradient(
                screenWidth / 2, 0, screenWidth / 2, -itemHeight,
                0x88FFFFFF, 0x96FF0000, Shader.TileMode.CLAMP));
        rowTwoPaint.setShader(new LinearGradient(
                screenWidth / 2, 0, screenWidth / 2, -itemHeight,
                0x88FFFFFF, 0x9600FFE1, Shader.TileMode.CLAMP));
        rowThreePaint.setShader(new LinearGradient(
                screenWidth / 2, 0, screenWidth / 2, -itemHeight,
                0x88FFFFFF, 0x960000FF, Shader.TileMode.CLAMP));


        linePaint = new Paint();
        linePaint.setStrokeWidth(dp2Dx(6));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setShader(new LinearGradient(
                screenWidth / 2, -linePaint.getStrokeWidth() / 2, screenWidth / 2, 0,
                0x00FFFFFF, 0x55FFFFFF, Shader.TileMode.MIRROR));

        //计算点路径
        computeInitPosition(rowOnePoints, 1);
        computeInitPosition(rowTwoPoints, 0.3F);
        computeInitPosition(rowThreePoints, -0.4F);

        //计算弹起的点和百分比
        computeSpring(rowOnePoints);
        computeSpring(rowTwoPoints);
        computeSpring(rowThreePoints);

        //自动开始
        post(new Runnable() {
            @Override
            public void run() {
                launchAnimation();
            }
        });
    }

    //启动动画
    public void launchAnimation() {

        //动画 1
        geneValueAnimator(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                //计算弹起的点和百分比
                computeSpring(rowOnePoints);
            }

        }, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //计算x,y的运动轨迹
                computeMotionTrack(rowOnePoints, (float) animation.getAnimatedValue());
                invalidateView();
            }
        }).start();

        //动画 2
        geneValueAnimator(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                //计算弹起的点和百分比
                computeSpring(rowTwoPoints);
            }

        }, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //计算x,y的运动轨迹
                computeMotionTrack(rowTwoPoints, (float) animation.getAnimatedValue());
            }
        }).start();

        //动画 3
        geneValueAnimator(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                //计算弹起的点和百分比
                computeSpring(rowThreePoints);
            }

        }, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //计算x,y的运动轨迹
                computeMotionTrack(rowThreePoints, (float) animation.getAnimatedValue());
            }
        }).start();
    }

    //生成动画
    private ValueAnimator geneValueAnimator(AnimatorListenerAdapter animatorListenerAdapter
            , ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {

        ValueAnimator animator = ValueAnimator.ofFloat(0F, 1F, 0F);
        animator.setDuration(600 + random.nextInt(600));
        animator.setRepeatCount(Integer.MAX_VALUE - 1);
        animator.addListener(animatorListenerAdapter);
        animator.addUpdateListener(animatorUpdateListener);

        return animator;
    }

    //计算弹起的点和百分比
    private void computeSpring(AJPoint[] rowPoints) {
        for (int i = 1; i < rowPoints.length - 1; i++) {
            rowPoints[i].needSpring = false;
            rowPoints[i].springFactor = 1F;
            if (!rowPoints[i - 1].needSpring) {//前面一个是没有弹起的
                rowPoints[i].needSpring = random.nextBoolean();
                rowPoints[i].springFactor = (30 + random.nextInt(70)) / 100F;
            }
        }
    }

    //计算初始化位置
    private void computeInitPosition(AJPoint[] points, float offsetX) {

        int lastIndex = points.length - 1;

        for (int i = 0; i < lastIndex; i++) {
            //Y
            float midIndex = lastIndex / 2f;
            float y = -itemHeight * (((midIndex - Math.abs(midIndex - i)) / midIndex));

            //X
            int itemWidth = screenWidth / lastIndex;
            float x = i * itemWidth + itemWidth * offsetX;

            //初始化
            points[i] = new AJPoint(x, y);
        }
        points[lastIndex] = new AJPoint(screenWidth, 0);
    }

    //计算x,y的运动轨迹
    private void computeMotionTrack(AJPoint[] points, float animatedValue) {

        for (int i = 0; i < 11; i++) {

            if (points[i].needSpring) {//需要弹起

                //Y
                float factor = points[i].springFactor;
                //float sy1 = points[i].initY * factor * animatedValue;
                //float sy2 = points[i].initY * factor * (1- animatedValue);
                //points[i].y = (int) (factor * 10) % 2 == 0 ? sy1 : sy2;
                points[i].y = points[i].initY * factor * animatedValue;

                //X
                int itemWidth = screenWidth / points.length - 1;
                float sx1 = points[i].initX + itemWidth * animatedValue / 2;
                float sx2 = points[i].initX - itemWidth * animatedValue / 2;

                if (i > 0 && i < 5) {
                    points[i].x = sx1;
                } else if (i > 5 && i < 10) {
                    points[i].x = sx2;
                }
            } else {
                points[i].y = 0;
                points[i].x = points[i].initX;
            }
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int mWidth = getWidth();
        int mHeight = getHeight();

        canvas.save();
        canvas.translate(dp2Dx(0), mHeight / 2);
        canvas.drawLine(0, 0, mWidth, 0, linePaint);

        //绘制音频线
        rowOnePath.reset();
        rowTwoPath.reset();
        rowThreePath.reset();
        for (int i = 0; i < rowOnePoints.length; i++) {
            rowOnePath.lineTo(rowOnePoints[i].x, rowOnePoints[i].y);
            rowTwoPath.lineTo(rowTwoPoints[i].x, rowTwoPoints[i].y);
            rowThreePath.lineTo(rowThreePoints[i].x, rowThreePoints[i].y);
        }
        canvas.drawPath(rowOnePath, rowOnePaint);
        canvas.drawPath(rowTwoPath, rowTwoPaint);
        canvas.drawPath(rowThreePath, rowThreePaint);

        //反向

        canvas.rotate(180, mWidth / 2, 0);
        canvas.scale(-1F, 1F, mWidth / 2, 0);

        canvas.drawPath(rowOnePath, rowOnePaint);
        canvas.drawPath(rowTwoPath, rowTwoPaint);
        canvas.drawPath(rowThreePath, rowThreePaint);

        canvas.restore();
    }


    private int dp2Dx(int dp) {
        return (int) (getResources().getDisplayMetrics().density * dp);
    }

    private void l(Object o) {
        Log.e("######", o.toString());
    }

    private void t(Object o) {
        Toast.makeText(getContext(), o.toString(), Toast.LENGTH_SHORT).show();
        Log.e("######", o.toString());
    }


    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            //  当前线程是主UI线程，直接刷新。
            invalidate();
        } else {
            //  当前线程是非UI线程，post刷新。
            postInvalidate();
        }
    }
}
