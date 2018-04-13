package me.ajax.siriview.widget;

import android.graphics.Point;
import android.graphics.PointF;

/**
 * Created by aj on 2018/4/7
 */

public class AJPoint extends PointF {

    public float initX;
    public float initY;
    public boolean needSpring;//是否弹起
    public float springFactor;//弹起比例

    public AJPoint() {
    }

    public AJPoint(float x, float y) {
        super(x, y);
        initX = x;
        initY = y;
    }

    public AJPoint(Point p) {
        super(p);
    }
}
