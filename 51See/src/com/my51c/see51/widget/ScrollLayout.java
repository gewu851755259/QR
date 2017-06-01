package com.my51c.see51.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

public class ScrollLayout extends ViewGroup {

	private static final String TAG = "ScrollLayout";
	private Scroller mScroller;
	/**滑动速度跟踪类*/
	private VelocityTracker mVelocityTracker;

	private int mCurScreen;
	private int mDefaultScreen = 0;

	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_SCROLLING = 1;

	private static final int SNAP_VELOCITY = 600;

	private int mTouchState = TOUCH_STATE_REST;
	private int mTouchSlop;
	private float mLastMotionX;
	private float mLastMotionY;
	private OnScreenChangeListener onScreenChangeListener;
	private OnScreenChangeListenerDataLoad onScreenChangeListenerDataLoad;
	private int currentScreenIndex = 0;


	public ScrollLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public ScrollLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mScroller = new Scroller(context);

		mCurScreen = mDefaultScreen;
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.e(TAG, "onMeasure");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("ScrollLayout only canmCurScreen run at EXACTLY mode!");
		}

		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode != MeasureSpec.EXACTLY) {
			/*throw new IllegalStateException("ScrollLayout only can run at EXACTLY mode!");*/
		}

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
		scrollTo(mCurScreen * width, 0);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		int childLeft = 0;
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View childView = getChildAt(i);
			if (childView.getVisibility() != View.GONE) {
				final int childWidth = childView.getMeasuredWidth();
				childView.layout(childLeft, 0, childLeft + childWidth,childView.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}

	public int getCurScreen() {
		return mCurScreen;
	}
	
	public int getCurrentScreenIndex() {
		return currentScreenIndex;
	}

	public void setCurrentScreenIndex(int currentScreenIndex) {
		this.currentScreenIndex = currentScreenIndex;
	}

	public void snapToDestination() {
		final int screenWidth = getWidth();
		final int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
		snapToScreen(destScreen);
	}

	public void snapToScreen(int whichScreen) {
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		if (getScrollX() != (whichScreen * getWidth()))
		{
			final int delta = whichScreen * getWidth() - getScrollX();
			mScroller.startScroll(getScrollX(), 0, delta, 0,Math.abs(delta) * 2);
			mCurScreen = whichScreen;
			invalidate(); // Redraw the layout
			this.setCurrentScreenIndex(whichScreen);
		}
	}

	public void setToScreen(int whichScreen){
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		mCurScreen = whichScreen;
		scrollTo(whichScreen * getWidth(), 0);
	}

	@Override
	public void computeScroll() {//当parent改变child的mScrollX或mScrollY时触发，典型的触发方式：child通过scroller完成滑动动画
		// TODO Auto-generated method stub
		if (mScroller.computeScrollOffset()) {//返回true表示滑动动画还未完成
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);

		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			Log.e(TAG, "event down!");
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mLastMotionX = x;
			System.out.println("onTouchEvent--ACTION_DOWN");
			break;

		case MotionEvent.ACTION_MOVE:
			int deltaX = (int) (mLastMotionX - x);
			mLastMotionX = x;

			scrollBy(deltaX, 0);
			System.out.println("onTouchEvent--ACTION_MOVE");
			break;

		case MotionEvent.ACTION_UP:
			System.out.println("onTouchEvent--ACTION_UP");
			final VelocityTracker velocityTracker = mVelocityTracker;
			velocityTracker.computeCurrentVelocity(1000);
			int velocityX = (int) velocityTracker.getXVelocity();


			if (velocityX > SNAP_VELOCITY && mCurScreen > 0) 
			{
				onScreenChangeListener.onScreenChange(mCurScreen - 1);
				snapToScreen(mCurScreen - 1);
			} else if (velocityX < -SNAP_VELOCITY&& mCurScreen < getChildCount() - 1) 
			{
				onScreenChangeListener.onScreenChange(mCurScreen + 1);
				onScreenChangeListenerDataLoad.onScreenChange(mCurScreen+1);
				snapToScreen(mCurScreen + 1);
			} else
			{
				snapToDestination();
			}

			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			// }
			mTouchState = TOUCH_STATE_REST;
			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
			break;
		}

		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub

		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}

		final float x = ev.getX();
		final float y = ev.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			System.out.println("onInterceptTouchEvent--ACTION_DOWN");
			mLastMotionX = x;
			mLastMotionY = y;
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
			break;
		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(mLastMotionX - x);
			if (xDiff > mTouchSlop) {
				mTouchState = TOUCH_STATE_SCROLLING;
			}
			System.out.println("onInterceptTouchEvent--ACTION_MOVE");
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			System.out.println("onInterceptTouchEvent--ACTION_UP");
			mTouchState = TOUCH_STATE_REST;
			break;
		}

		return mTouchState != TOUCH_STATE_REST;
	}

	public interface OnScreenChangeListener {
		void onScreenChange(int currentIndex);
	}

	public void setOnScreenChangeListener(
			OnScreenChangeListener onScreenChangeListener) {
		this.onScreenChangeListener = onScreenChangeListener;
	}
	
	public interface OnScreenChangeListenerDataLoad {
		void onScreenChange(int currentIndex);
	}

	public void setOnScreenChangeListenerDataLoad(OnScreenChangeListenerDataLoad onScreenChangeListenerDataLoad) {
		this.onScreenChangeListenerDataLoad = onScreenChangeListenerDataLoad;
	}
}
