package phantom.activity;

import java.io.File;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.phantom.sfood.R;

import phantom.model.DrawView;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

public class CameraActivity extends Activity implements CvCameraViewListener2 {
	CameraBridgeViewBase mCameraBridgeViewBase;
	Button btnHello;
	DrawView myView;
	Mat imageShow, subImage;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(MainActivity.TAG, "OpenCV loaded successfully");
				mCameraBridgeViewBase.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public CameraActivity() {
		Log.i(MainActivity.TAG, "Instance " + this.getClass());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_my_camera);
		mCameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.cameraView);
		mCameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
		mCameraBridgeViewBase.setCvCameraViewListener(this);
		btnHello = (Button) findViewById(R.id.activity_my_camara_btnHello);
		myView = (DrawView) findViewById(R.id.activity_my_camera_my_view);
		Animation helloAnimation = AnimationUtils.loadAnimation(this,
				R.anim.my_rotation);
		btnHello.startAnimation(helloAnimation);

	}

	public void clickHello(View v) {
		Rect rect = myView.getRectangle();
		// takePhoto(imageShow);
		// Rect rect = null;
		if (rect != null) {
			int imageWidth = imageShow.width();
			int imageHeight = imageShow.height();

			int MAX_HEIGHT = myView.getHeight();
			int MAX_WIDTH = myView.getWidth();

			Toast.makeText(this, "Size : " + imageWidth + "-" + imageHeight,
					Toast.LENGTH_LONG).show();
			float scaleX = (float) imageWidth / MAX_WIDTH;
			float scaleY = (float) imageHeight / MAX_HEIGHT;
			int rowStart = 0, rowEnd = 0, colStart = 0, colEnd = 0;
			rowStart = (int) (rect.top * scaleX);
			rowEnd = (int) (rect.bottom * scaleX);

			colStart = (int) (rect.left * scaleY);
			colEnd = (int) (rect.right * scaleY);
			// .3.
			if (rowStart < 0) {
				rowStart = 0;
			}
			if (rowEnd > imageHeight) {
				rowEnd = imageHeight;
			}
			if (colStart < 0) {
				colStart = 0;
			}
			if (colEnd > imageWidth) {
				colEnd = imageWidth;
			}

			subImage = imageShow.submat(rowStart, rowEnd, colStart, colEnd);

			double rows = subImage.rows();
			double cols = subImage.cols();

			// System.out.println(rows + "-" + cols);
			Mat M = Imgproc.getRotationMatrix2D(new Point(rows / 2, rows / 2),
					-90, 1.0);
			Imgproc.warpAffine(subImage, subImage, M, new Size(rows, cols));
			takePhoto(subImage);
		}
	}

	private void takePhoto(final Mat rgba) {

		// Determine the path and metadata for the photo.
		final long currentTimeMillis = System.currentTimeMillis();
		final String appName = getString(R.string.app_name);
		final String galleryPath = Environment
				.getExternalStoragePublicDirectory(
						Environment.DIRECTORY_PICTURES).toString();
		final String albumPath = galleryPath + File.separator + appName;
		final String photoPath = albumPath + File.separator + currentTimeMillis
				+ LabActivity.PHOTO_FILE_EXTENSION;
		final ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, photoPath);
		values.put(Images.Media.MIME_TYPE, LabActivity.PHOTO_MIME_TYPE);
		values.put(Images.Media.TITLE, appName);
		values.put(Images.Media.DESCRIPTION, appName);
		values.put(Images.Media.DATE_TAKEN, currentTimeMillis);

		// Ensure that the album directory exists.
		File album = new File(albumPath);
		if (!album.isDirectory() && !album.mkdirs()) {
			Log.e(MainActivity.TAG, "Failed to create album directory at "
					+ albumPath);
			// onTakePhotoFailed();
			return;
		}

		Mat mBgr = new Mat();
		// Try to create the photo.
		Imgproc.cvtColor(rgba, mBgr, Imgproc.COLOR_RGBA2BGR, 3);
		if (!Highgui.imwrite(photoPath, mBgr)) {
			Log.e(MainActivity.TAG, "Failed to save photo to " + photoPath);
			// onTakePhotoFailed();
		}
		Log.d(MainActivity.TAG, "Photo saved successfully to " + photoPath);

		// Try to insert the photo into the MediaStore.
		Uri uri;
		try {
			uri = getContentResolver().insert(
					Images.Media.EXTERNAL_CONTENT_URI, values);
		} catch (final Exception e) {
			Log.e(MainActivity.TAG, "Failed to insert photo into MediaStore");
			e.printStackTrace();

			// Since the insertion failed, delete the photo.
			File photo = new File(photoPath);
			if (!photo.delete()) {
				Log.e(MainActivity.TAG, "Failed to delete non-inserted photo");
			}

			// onTakePhotoFailed();
			return;
		}

		// Open the photo in LabActivity.
		final Intent intent = new Intent(this, LabActivity.class);
		intent.putExtra(LabActivity.EXTRA_PHOTO_URI, uri);
		intent.putExtra(LabActivity.EXTRA_PHOTO_DATA_PATH, photoPath);
		startActivity(intent);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mCameraBridgeViewBase != null) {
			mCameraBridgeViewBase.disableView();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mCameraBridgeViewBase != null) {
			mCameraBridgeViewBase.disableView();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mCameraBridgeViewBase != null) {
			mCameraBridgeViewBase.disableView();
		}
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		Toast.makeText(this, "Camera Startad!", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onCameraViewStopped() {

	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		imageShow = inputFrame.rgba();
		return imageShow;
	}

}
