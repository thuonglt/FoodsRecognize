package svm2;

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

public class Predic {
	private final int MAX_WIDTH = 400;
	private final int MAX_HEIGHT = 400;

	private final int MAX_ITER = 100;
	private final int CLUSTERS = 350;
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

	public Predic() {
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
		// -----------------------------

		System.out.println(totalKeypoit.size());
		totalLabels = new Mat(totalKeypoit.rows(), 1, CvType.CV_32FC1);
		totalCentroid = new Mat(CLUSTERS, 1, CvType.CV_32FC1);
		Core.kmeans(totalKeypoit, CLUSTERS, totalLabels, new TermCriteria(
				TermCriteria.EPS, MAX_ITER, 1e-5), 3,
				Core.KMEANS_RANDOM_CENTERS, totalCentroid);

		System.out.println(totalLabels.size());
		System.out.println(totalCentroid.size());
		// ------------------------
		// ---------------Training-----------
		svm = new CvSVM();
		svm.load("file_data_training_400.xml");
		System.out.println("Done training!");
		System.out.println("----------------------");
		// ----------------End Training-----------------------

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
		featureDetector = FeatureDetector.create(FeatureDetector.SIFT);
		matOfKeyPoint = new MatOfKeyPoint();
		descriptorExtractor = DescriptorExtractor
				.create(DescriptorExtractor.SIFT);
		labelTraining = new Mat();
		dataTraining = new Mat();
		listSample = new File("Sample").listFiles();
		listFileInput = new File("Input").listFiles();

		arrayData = new Mat[listSample.length];
		arrayLabel = new Mat[listSample.length];
		arrayThread = new Thread[listSample.length];
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

	// -----------------------------------------------------------------
	public void addImage(File[] files) {
		for (int i = 0; i < files.length; i++) {
			Mat image = Highgui.imread(files[i].getAbsolutePath(),
					Highgui.CV_LOAD_IMAGE_COLOR);
			featureDetector.detect(image, matOfKeyPoint);
			descriptorExtractor.compute(image, matOfKeyPoint, descriptors);
			totalKeypoit.push_back(descriptors);
		}
	}

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
			histogram[index] = histogram[index] + 3;
		}
		return histogram;
	}

	public int calculateDistance(Mat sample, Mat totalPoint) {
		double result = 0, max = -1;
		int index = 0;
		if (sample.cols() != totalPoint.cols()) {
			return -1;
		}
		for (int j = 0; j < totalPoint.rows(); j++) {
			double temp1 = 0, temp2 = 0, temp3 = 0;
			for (int i = 0; i < sample.cols(); i++) {
				double a = sample.get(0, i)[0];
				double b = totalPoint.get(j, i)[0];

				temp1 = temp1 + a * b;
				temp2 = temp2 + a * a;
				temp3 = temp3 + b * b;

			}

			result = temp1 / (Math.sqrt(temp2) * Math.sqrt(temp3));
//			System.out.println("result: " + result);
			if (result > max) {
				max = result;
				index = j;
			}
		}
		return index;
	}

	// -----------------------------------------------------------------------
	public static void main(String[] args) {
		new Predic();
	}

}
