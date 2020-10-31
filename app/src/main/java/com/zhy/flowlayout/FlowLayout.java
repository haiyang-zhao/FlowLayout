package com.zhy.flowlayout;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlowLayout extends ViewGroup {

    private static final String TAG = "FlowLayout";
    private int mHorizontalSpacing = dp2px(16); //每个item横向间距
    private int mVerticalSpacing = dp2px(8); //每个item横向间距

    private List<List<View>> allLines = new ArrayList<>(); // 记录所有的行，一行一行的存储，用于layout
    List<Integer> lineHeights = new ArrayList<>(); // 记录每一行的行高，用于layout

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    private void clearMeasureParams() {

        allLines.clear();
        lineHeights.clear();
    }

    /**
     * @param widthMeasureSpec  父容器测出的MeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //防止内存抖动
        clearMeasureParams();
        //先度量孩子
        int childCount = getChildCount();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        //每行摆放的view
        List<View> lineViews = new ArrayList<>();
        //记录这行已经使用了多宽的size
        int lineWidthUsed = 0;
        // 一行的行高
        int lineHeight = 0;

        // measure过程中，子View要求的父ViewGroup的宽
        int parentNeededWidth = 0;
        // measure过程中，子View要求的父ViewGroup的高
        int parentNeededHeight = 0;

        int selfWidth = MeasureSpec.getSize(widthMeasureSpec);
        int selfHeight = MeasureSpec.getSize(heightMeasureSpec);

        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            LayoutParams childLps = childView.getLayoutParams();
            if (childView.getVisibility() != GONE) {
                //将layoutParams转变成为 measureSpec
                int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                        paddingLeft + paddingRight, childLps.width);
                int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                        paddingTop + paddingBottom, childLps.height);
                childView.measure(childWidthMeasureSpec, childHeightMeasureSpec);

                //获取子view的度量宽高
                int childViewMeasuredWidth = childView.getMeasuredWidth();
                int childViewMeasuredHeight = childView.getMeasuredHeight();

                //换行
                if (childViewMeasuredWidth + lineWidthUsed + mHorizontalSpacing > selfWidth) {
                    //一旦换行，我们就可以判断当前行需要的宽和高了，所以此时要记录下来
                    allLines.add(lineViews);
                    lineHeights.add(lineHeight);

                    parentNeededHeight = parentNeededHeight + lineHeight + mVerticalSpacing;
                    parentNeededWidth = Math.max(parentNeededWidth, lineWidthUsed + mHorizontalSpacing);

                    lineViews = new ArrayList<>();
                    lineWidthUsed = 0;
                    lineHeight = 0;

                }
                // view 是分行layout的，所以要记录每一行有哪些view，这样可以方便layout布局
                lineViews.add(childView);
                //每行都会有自己的宽和高
                lineWidthUsed = lineWidthUsed + childViewMeasuredWidth + mHorizontalSpacing;
                lineHeight = Math.max(lineHeight, childViewMeasuredHeight);
                //处理最后一行数据
                if (i == childCount - 1) {
                    allLines.add(lineViews);
                    lineHeights.add(lineHeight);
                    parentNeededHeight = parentNeededHeight + lineHeight + mVerticalSpacing;
                    parentNeededWidth = Math.max(parentNeededWidth, lineWidthUsed + mHorizontalSpacing);
                }

                //再度量自己,保存
                //根据子View的度量结果，来重新度量自己ViewGroup
                // 作为一个ViewGroup，它自己也是一个View,它的大小也需要根据它的父亲给它提供的宽高来度量
                int widthMode = MeasureSpec.getMode(widthMeasureSpec);
                int heightMode = MeasureSpec.getMode(heightMeasureSpec);

                int realWidth = (widthMode == MeasureSpec.EXACTLY) ? selfWidth : parentNeededWidth;
                int realHeight = (heightMode == MeasureSpec.EXACTLY) ? selfHeight : parentNeededHeight;
                setMeasuredDimension(realWidth, realHeight);
                Log.d(TAG, Arrays.toString(lineHeights.toArray()));
            }
        }

    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        //布局
        int lineSize = allLines.size();
        int curLeft = getPaddingLeft();
        int curTop = getPaddingTop();
        for (int i = 0; i < lineSize; i++) {
            List<View> lineViews = allLines.get(i);
            int lineHeight = lineHeights.get(i);
            for (int j = 0; j < lineViews.size(); j++) {
                View view = lineViews.get(j);
                int left = curLeft;
                int top = curTop;
                int right = left + view.getMeasuredWidth();
                int bottom = top + view.getMeasuredHeight();
                view.layout(left, top, right, bottom);
                curLeft = right + mHorizontalSpacing;
            }
            curLeft = getPaddingLeft();
            curTop = curTop + lineHeight + mVerticalSpacing;
        }
    }


    public static int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }
}
