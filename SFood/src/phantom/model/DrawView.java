package phantom.model;

import java.util.ArrayList;

import phantom.activity.MainActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class DrawView extends View {

	Point[] points = new Point[4];
	int left, top, right, bottom;
	/**
	 * point1 and point 3 are of same group and same as point 2 and point4
	 */
	int groupId = -1;
	private ArrayList<ColorBall> colorballs = new ArrayList<ColorBall>();
	// array that holds the balls
	private int balID = 0;
	// variable to know what ball is being dragged
	Paint paint;
	Canvas canvas;
	boolean isMoveWindows = false;
	int isMoveIdEDGE = -1;
	int previosWindowX = 0;
	int previosWindowY = 0;
	int epsilonEDGE = 40;

	public DrawView(Context context) {
		super(context);
		paint = new Paint();
		setFocusable(true); // necessary for getting the touch events
		canvas = new Canvas();
	}

	public DrawView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public DrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		setFocusable(true); // necessary for getting the touch events
		canvas = new Canvas();
	}

	// the method that draws the balls
	@Override
	protected void onDraw(Canvas canvas) {
		if (points[3] == null)
			return;

		left = points[0].x;
		top = points[0].y;
		right = points[0].x;
		bottom = points[0].y;
		for (int i = 1; i < points.length; i++) {
			left = left > points[i].x ? points[i].x : left;
			top = top > points[i].y ? points[i].y : top;
			right = right < points[i].x ? points[i].x : right;
			bottom = bottom < points[i].y ? points[i].y : bottom;
		}
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeWidth(5);

		// draw stroke
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.YELLOW);
		paint.setStrokeWidth(8);
		canvas.drawRect(left, top, right, bottom, paint);

		// paint.setColor(Color.BLUE);
		// paint.setStrokeWidth(0);
		// paint.setStyle(Paint.Style.FILL);
		// for (int i = 0; i < colorballs.size(); i++) {
		// ColorBall ball = colorballs.get(i);
		// canvas.drawCircle(ball.getX(), ball.getY(), colorballs.get(i)
		// .getRadius(), paint);
		// }
	}

	public Rect getRectangle() {
		Rect rect = null;
		if (colorballs.size() > 3) {
			rect = new Rect(left, top, right, bottom);
		}
		return rect;
	}

	// events when touching the screen

	@SuppressLint("ClickableViewAccessibility")
	public boolean onTouchEvent(MotionEvent event) {
		int eventaction = event.getAction();

		int X = (int) event.getX();
		int Y = (int) event.getY();

		switch (eventaction) {

		case MotionEvent.ACTION_DOWN: // touch down so check if the finger is on
										// a ball
			if (points[0] == null) {
				// initialize rectangle.
				points[0] = new Point();
				points[0].x = X;
				points[0].y = Y;

				points[1] = new Point();
				points[1].x = X;
				points[1].y = Y + 30;

				points[2] = new Point();
				points[2].x = X + 30;
				points[2].y = Y + 30;

				points[3] = new Point();
				points[3].x = X + 30;
				points[3].y = Y;

				balID = 2;
				groupId = 1;
				// declare each ball with the ColorBall class
				for (Point pt : points) {
					colorballs.add(new ColorBall(pt, 40));
				}
				Log.i(MainActivity.TAG, ("Width of Ball "
						+ colorballs.get(0).getWidthOfBall() + ":" + colorballs
						.get(0).getHeightOfBall()));
			} else {
				// resize rectangle

				for (int i = colorballs.size() - 1; i >= 0; i--) {
					ColorBall ball = colorballs.get(i);
					// check if inside the bounds of the ball (circle)
					// get the center for the ball
					int centerX = ball.getX();
					int centerY = ball.getY();
					paint.setColor(Color.CYAN);
					// calculate the radius from the touch to the center of the
					// ball
					double radCircle = Math
							.sqrt((double) (((centerX - X) * (centerX - X)) + (centerY - Y)
									* (centerY - Y)));

					if (radCircle < ball.getWidthOfBall()) {

						balID = ball.getID();
						if (balID == 1 || balID == 3) {
							groupId = 2;
						} else {
							groupId = 1;
						}
						invalidate();
						break;
					}
				}
				if (X > left && X < right
						&& Y > (colorballs.get(2).getY() - epsilonEDGE)
						&& Y < (colorballs.get(2).getY() + epsilonEDGE)) {
					isMoveIdEDGE = 1;
					break;
				}
				if (X > left && X < right
						&& Y > (colorballs.get(0).getY() - epsilonEDGE)
						&& Y < (colorballs.get(0).getY() + epsilonEDGE)) {
					isMoveIdEDGE = 2;
					break;
				}
				if (Y > top && Y < bottom
						&& X > (colorballs.get(2).getX() - epsilonEDGE)
						&& X < (colorballs.get(2).getX() + epsilonEDGE)) {
					isMoveIdEDGE = 3;
					break;
				}
				if (Y > top && Y < bottom
						&& X > (colorballs.get(0).getX() - epsilonEDGE)
						&& X < (colorballs.get(0).getX() + epsilonEDGE)) {
					isMoveIdEDGE = 4;
					break;
				}
				if (X > left && X < right && Y > top && Y < bottom) {
					isMoveWindows = true;
					previosWindowX = X;
					previosWindowY = Y;
					break;
				}
				// invalidate();
			}
			break;

		case MotionEvent.ACTION_MOVE: // touch drag with the ball

			if (balID > -1) {
				// move the balls the same as the finger
				colorballs.get(balID).setX(X);
				colorballs.get(balID).setY(Y);

				if (groupId == 1) {
					colorballs.get(1).setX(colorballs.get(0).getX());
					colorballs.get(1).setY(colorballs.get(2).getY());
					colorballs.get(3).setX(colorballs.get(2).getX());
					colorballs.get(3).setY(colorballs.get(0).getY());
				} else if (groupId == 2) {
					colorballs.get(0).setX(colorballs.get(1).getX());
					colorballs.get(0).setY(colorballs.get(3).getY());
					colorballs.get(2).setX(colorballs.get(3).getX());
					colorballs.get(2).setY(colorballs.get(1).getY());
				}
				// Move EDGE
			} else if (isMoveIdEDGE > 0) {
				if (isMoveIdEDGE == 1) {
					colorballs.get(1).setY(Y);
					colorballs.get(2).setY(Y);
				} else if (isMoveIdEDGE == 2) {
					colorballs.get(0).setY(Y);
					colorballs.get(3).setY(Y);
				} else if (isMoveIdEDGE == 3) {
					colorballs.get(2).setX(X);
					colorballs.get(3).setX(X);
				} else if (isMoveIdEDGE == 4) {
					colorballs.get(1).setX(X);
					colorballs.get(0).setX(X);
				}
				// Move Windows
			} else if (isMoveWindows) {
				for (ColorBall ball : colorballs) {
					ball.setX(ball.getX() + (X - previosWindowX));
					ball.setY(ball.getY() + (Y - previosWindowY));
				}
				previosWindowX = X;
				previosWindowY = Y;
			}
			invalidate();
			break;

		case MotionEvent.ACTION_UP:
			// touch drop - just do things here after dropping
			balID = -1;
			groupId = -1;
			isMoveWindows = false;
			isMoveIdEDGE = -1;
			break;
		}
		// redraw the canvas
		invalidate();
		return true;

	}

	public static class ColorBall {

		Bitmap bitmap = null;
		Context mContext;
		Point point;
		int id;
		static int count = 0;
		int withOfBall, heightOfBall;
		int radius = 0;

		public ColorBall(Context context, int resourceId, Point point) {
			this.id = count++;
			bitmap = BitmapFactory.decodeResource(context.getResources(),
					resourceId);
			mContext = context;
			withOfBall = bitmap.getWidth();
			heightOfBall = bitmap.getWidth();
			this.point = point;
		}

		public ColorBall(Point point, int radius) {
			this.id = count++;
			this.point = point;
			this.radius = radius;
			withOfBall = heightOfBall = radius * 2;
		}

		public int getWidthOfBall() {
			return withOfBall;
		}

		public int getHeightOfBall() {
			return heightOfBall;
		}

		public Bitmap getBitmap() {
			return bitmap;
		}

		public int getX() {
			return point.x;
		}

		public int getY() {
			return point.y;
		}

		public int getID() {
			return id;
		}

		public void setX(int x) {
			point.x = x;
		}

		public void setY(int y) {
			point.y = y;
		}

		public int getRadius() {
			return radius;
		}

		public void setRadius(int radius) {
			this.radius = radius;
		}

	}
}
