package svm3;

import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.TermCriteria;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;
import org.opencv.ml.CvSVM;
import org.opencv.ml.CvSVMParams;

public class Predic2 {
	private final int MAX_WIDTH = 400;
	private final int MAX_HEIGHT = 400;

	private final int MAX_ITER = 100;
	private final int CLUSTERS = 100;
	private int TOTAL_SAMPLE = 0;
	private int TOTAL_INPUT = 0;
	private int TOTAL_INPUT_TRUE = 0;
	public int count_label = 0;

	Mat descriptors, labelTraining, dataTraining, totalKeypoit, totalLabels,
			totalCentroid;
	FeatureDetector featureDetector;
	MatOfKeyPoint matOfKeyPoint;
	DescriptorExtractor descriptorExtractor;
	CvSVM svm;

	Mat[] arrayData, arrayLabel;
	Thread[] arrayThread;

	File[] listSample, listFileInput;

	public Predic2() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		innit();
		// Load all list file data
		for (int i = 0; i < listSample.length; i++) {
			File[] listData = listSample[i].listFiles();
			addImage(listData);
			System.out.println("Add folder " + i);
			TOTAL_SAMPLE = TOTAL_SAMPLE + listData.length;
		}
		System.out.println("TOTAL SAMPLE: " + TOTAL_SAMPLE);

		svm = new CvSVM();
		svm.load("xml//training2_cluster_per_100.xml");
		System.out.println("Done training!");
		System.out.println("----------------------");

		for (int i = 0; i < listFileInput.length; i++) {
			File[] subList = listFileInput[i].listFiles();
			String trueResult = listFileInput[i].getName();
			for (int j = 0; j < subList.length; j++) {
				TOTAL_INPUT++;
				String path = subList[j].getAbsolutePath();
				int result = (int) predic(path);
				System.out.print(path + " ---> " + result);
				if (trueResult.equals(result + "")) {
					System.out.println(" TRUE");
					TOTAL_INPUT_TRUE++;
				} else {
					System.out.println(" FALSE");
				}
			}

		}
		System.out.println("Tỷ Lệ: " + TOTAL_INPUT_TRUE + "/" + TOTAL_INPUT);
	}

	public void innit() {
		// -------------- init ------------------
		descriptors = new Mat();
		totalKeypoit = new Mat();
		totalCentroid = new Mat();
		featureDetector = FeatureDetector.create(FeatureDetector.SURF);
		matOfKeyPoint = new MatOfKeyPoint();
		descriptorExtractor = DescriptorExtractor
				.create(DescriptorExtractor.SURF);
		labelTraining = new Mat();
		dataTraining = new Mat();
		listSample = new File("Sample").listFiles();
		listFileInput = new File("Input").listFiles();
		arrayData = new Mat[listSample.length];
	}

	// --------------------------------------------------
	public void addImage(File[] files) {
		matOfKeyPoint.release();
		descriptors.release();
		totalKeypoit.release();
		for (int i = 0; i < files.length; i++) {
			Mat image = Highgui.imread(files[i].getAbsolutePath(),
					Highgui.CV_LOAD_IMAGE_COLOR);
			featureDetector.detect(image, matOfKeyPoint);
			descriptorExtractor.compute(image, matOfKeyPoint, descriptors);
			totalKeypoit.push_back(descriptors);
		}
		Mat tempLabels = new Mat(totalKeypoit.rows(), 1, CvType.CV_32FC1);
		Mat tempCentroid = new Mat(CLUSTERS, 1, CvType.CV_32FC1);
		Core.kmeans(totalKeypoit, CLUSTERS, tempLabels, new TermCriteria(
				TermCriteria.EPS, MAX_ITER, 1e-5), 3,
				Core.KMEANS_RANDOM_CENTERS, tempCentroid);
		totalCentroid.push_back(tempCentroid);
		System.out.println("totalCentroid: " + totalCentroid.size());
	}

	// -----------------------------------------------------------------

	public int[] processImage(String path) {
		int[] histogram = new int[totalCentroid.rows()];
		Mat img = Highgui.imread(path, Highgui.CV_LOAD_IMAGE_COLOR);

		matOfKeyPoint.release();
		descriptors.release();

		featureDetector.detect(img, matOfKeyPoint);
		descriptorExtractor.compute(img, matOfKeyPoint, descriptors);
		for (int i = 0; i < descriptors.rows(); i++) {
			Mat row = descriptors.row(i);
			int index = calculateDistance(row, totalCentroid);
			histogram[index] = histogram[index] + 1;
		}
		return histogram;
	}

	public int calculateDistance(Mat sample, Mat totalPoint) {
		double sum = 0, result = 0, min = 0;
		int index = 0;
		if (sample.cols() != totalPoint.cols()) {
			return -1;
		}
		for (int j = 0; j < totalPoint.rows(); j++) {
			sum = 0;
			for (int i = 0; i < sample.cols(); i++) {
				double temp = sample.get(0, i)[0] - totalPoint.get(j, i)[0];
				sum = sum + temp * temp;
			}
			result = Math.sqrt(sum);
			if (result < min || j == 0) {
				min = result;
				index = j;
			}
		}
		return index;
	}

	// -------------------------------predic---------------------------------
	public double predic(String path) {
		double result = -1;
		int[] histogram = processImage(path);
		Mat tempMat = new Mat(1, histogram.length, CvType.CV_32FC1);
		double sum = 0;
		for (int i = 0; i < histogram.length; i++) {
			tempMat.put(0, i, histogram[i]);
			sum = sum + tempMat.get(0, i)[0];
		}
		System.out.print("Sum: " + sum);
		result = svm.predict(tempMat);

		return result;
	}

	// -----------------------------------------------------------------------
	public static void main(String[] args) {
		new Predic2();
	}

}
