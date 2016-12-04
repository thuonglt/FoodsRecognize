package phantom.asynctask;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.achartengine.ChartFactory;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvSVM;

import phantom.activity.LabActivity;
import phantom.activity.MainActivity;
import phantom.lib.NonfreeJNILib;
import phantom.model.Food;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.phantom.sfood.R;

public class PredicImageAsynctask extends AsyncTask<String, String, Void> {

	Activity context;
	private View mChart;
	public int count_label = 0;
	private int mHistSizeNum = 35;

	Mat descriptors, labelTraining, dataTraining, totalKeypoit, totalLabels,
			totalCentroid;
	CvSVM svm;

	ImageView imageView;
	TextView tvNamFood, tvTotalCalorie;
	String PATH_DATA_BOW, PATH_DATA_SVM, PATH_IMAGE;
	ProgressDialog dialog;

	private MatOfInt mChannels[];
	private MatOfFloat mRanges;
	private Mat mMat0;
	private MatOfInt mHistSize;
	private float mBuff[];

	Food myFood = null;

	public PredicImageAsynctask(Activity context) {
		this.context = context;
		Log.i(MainActivity.TAG, "Start AsyncTask!");
	}

	@Override
	protected Void doInBackground(String... params) {
		innit();
		PATH_IMAGE = params[0];

		totalCentroid = loadBOW(PATH_DATA_BOW);
		Log.i(MainActivity.TAG, "LOAD Susccess BOW! " + totalCentroid.size());
		svm = new CvSVM();
		svm.load(PATH_DATA_SVM);
		Log.i(MainActivity.TAG, "LOAD Susccess SVM!");

		double result = predic(PATH_IMAGE);
		Log.i(MainActivity.TAG, "RESULT : " + (int) result);
		Log.i(MainActivity.TAG, MainActivity.databas.size() + " hehe");
		myFood = MainActivity.databas.get((int) result);
		LabActivity.myFood = myFood;

		publishProgress("");
		return null;
	}

	public void innit() {
		descriptors = new Mat();
		totalKeypoit = new Mat();
		labelTraining = new Mat();
		dataTraining = new Mat();

		PATH_DATA_BOW = LabActivity.PATH_DATA_BOW;
		PATH_DATA_SVM = LabActivity.PATH_DATA_SVM;
		tvNamFood = (TextView) context.findViewById(R.id.txtNameFood);
		// --------Histogram--------
		mChannels = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1),
				new MatOfInt(2) };
		mRanges = new MatOfFloat(0f, 256f);
		mMat0 = new Mat();
		mChannels = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1),
				new MatOfInt(2) };
		mBuff = new float[mHistSizeNum];
		mHistSize = new MatOfInt(mHistSizeNum);
		mRanges = new MatOfFloat(0f, 256f);
		mMat0 = new Mat();

	}

	public double predic(String path) {
		double result = -1;
		double[] histogram = processImage(path);
		Mat tempMat = new Mat(1, histogram.length, CvType.CV_32FC1);
		for (int i = 0; i < histogram.length; i++) {
			tempMat.put(0, i, histogram[i]);
		}
		Log.i(MainActivity.TAG, "Start predict!");
		result = svm.predict(tempMat);
		Log.i(MainActivity.TAG, "End predict!");

		return result;
	}

	public double[] processImage(String path) {
		int sizeArray = totalCentroid.rows() + mHistSizeNum * 1;
		double[] histogram = new double[sizeArray];
		int CLUSTERS = totalCentroid.rows();

		Mat img = Highgui.imread(path, Highgui.CV_LOAD_IMAGE_COLOR);

		if (img.cols() > 400) {
			int cols = (400 * img.cols()) / img.rows();
			Imgproc.resize(img, img, new Size(cols, 400));
		}
		NonfreeJNILib.surfCompute(img.getNativeObjAddr(),
				descriptors.getNativeObjAddr());
		Log.i(MainActivity.TAG, "Size description: " + descriptors.size() + "");
		if (descriptors.rows() > 700) {
			Mat tempLabel = new Mat(descriptors.rows(), 1, CvType.CV_32FC1);
			Core.kmeans(descriptors, 700, tempLabel, new TermCriteria(
					TermCriteria.EPS, 100000, 1e-8), 3, Core.KMEANS_PP_CENTERS,
					descriptors);
			Log.i(MainActivity.TAG, "KMEAN 700");
		}
		Log.i(MainActivity.TAG, "descriptor: " + descriptors.rows());
		for (int i = 0; i < descriptors.rows(); i++) {
			int index = -1;
			Mat row = descriptors.row(i);
			index = NonfreeJNILib.calculateDistance(row.getNativeObjAddr(),
					totalCentroid.getNativeObjAddr());
			histogram[index] = histogram[index] + 4;
			Log.i(MainActivity.TAG, "JAVA row: " + i + " Index: " + index);
		}
		// --------------------------
		// --------------------------

		Mat hist = new Mat();
		Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2HSV_FULL);

		Imgproc.calcHist(Arrays.asList(img), mChannels[0], mMat0, hist,
				mHistSize, mRanges);
		Core.normalize(hist, hist, 150, 0, Core.NORM_INF);
		hist.get(0, 0, mBuff);
		for (int i = 0; i < mBuff.length; i++) {
			histogram[CLUSTERS + i] = mBuff[i];
		}
		return histogram;
	}

	public Mat loadBOW(String path) {
		Mat mat = null;
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(
					new FileInputStream(path)));
			String temp = r.readLine();
			String[] temp2 = temp.split(" ");
			int row = Integer.parseInt(temp2[0].trim());
			int col = Integer.parseInt(temp2[1].trim());
			mat = new Mat(row, col, CvType.CV_32FC1);

			for (int i = 0; i < row; i++) {
				String s1 = r.readLine();
				String[] s = s1.split(" ");
				for (int j = 0; j < s.length; j++) {
					double data = Double.parseDouble(s[j].trim());
					mat.put(i, j, data);
				}

			}
			r.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return mat;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		dialog = new ProgressDialog(context);
		dialog.setMessage("waiting...");
		dialog.setCancelable(false);
		dialog.show();
	}

	@Override
	protected void onPostExecute(Void result) {
		dialog.dismiss();
		super.onPostExecute(result);
	}

	@Override
	protected void onProgressUpdate(String... values) {
		tvNamFood.setText(myFood.name + "\n" + "Hàm lượng Calorie: "
				+ myFood.calorie + " kcal");
		openChart(myFood);
	}

	private void openChart(Food food) {
		String[] code = new String[] { "Total Fat", "Total Carbohydrate",
				"Protein", "Other" };
		double valueOther = 100 - (food.fat + food.protein + food.carbohydrate);
		double[] distribution = { food.fat, food.carbohydrate, food.protein,
				valueOther };
		int[] colors = { Color.BLUE, Color.GREEN, Color.RED, Color.MAGENTA };

		CategorySeries distributionSeries = new CategorySeries(
				"Thanh Phan Dinh Duong Moi 100gr " + food.name);
		for (int i = 0; i < distribution.length; i++) {
			distributionSeries.add(code[i], distribution[i]);
		}
		// Instantiating a renderer for the Pie Chart
		DefaultRenderer defaultRenderer = new DefaultRenderer();
		for (int i = 0; i < distribution.length; i++) {
			SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
			seriesRenderer.setColor(colors[i]);
			seriesRenderer.setHighlighted(true);
			defaultRenderer.addSeriesRenderer(seriesRenderer);
		}
		defaultRenderer.setBackgroundColor(Color.TRANSPARENT);
		defaultRenderer.setApplyBackgroundColor(true);
		defaultRenderer.setChartTitle("Thanh Phan Dinh Duong Moi 100gr "
				+ food.name);
		defaultRenderer.setChartTitleTextSize(50);
		defaultRenderer.setLabelsTextSize(40);
		defaultRenderer.setDisplayValues(false);
		defaultRenderer.setZoomEnabled(false);
		defaultRenderer.setShowLegend(false);
		defaultRenderer.setPanEnabled(false);
		// defaultRenderer.setExternalZoomEnabled(true);
		// defaultRenderer.setLegendTextSize(30);
		// defaultRenderer.setLegendHeight(100);
		// defaultRenderer.setFitLegend(true);

		LinearLayout chartContainer = (LinearLayout) context
				.findViewById(R.id.chart);
		chartContainer.removeAllViews();
		mChart = ChartFactory.getPieChartView(context, distributionSeries,
				defaultRenderer);
		chartContainer.addView(mChart);

	}
}
