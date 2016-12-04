package svm;

import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvSVM;
import org.opencv.ml.CvSVMParams;

public class Main {
	private final int MAX_WIDTH = 400;
	private final int MAX_HEIGHT = 400;

	private final int MAX_ITER = 100;
	private final int CLUSTERS = 200;
	private int TOTAL_SAMPLE = 0;
	public int count_label = 0;

	Mat descriptors, labelTraining, dataTraining, totalKeypoit, totalLabels,
			totalCentroid;
	FeatureDetector featureDetector;
	MatOfKeyPoint matOfKeyPoint;
	DescriptorExtractor descriptorExtractor;
	CvSVM svm;
	File[] listFileApple, listFileOrange, listFileEggs, listFileChicken,
			listFileInput;

	public Main() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		innit();
		// Load all list file data
		addImage(listFileApple);
		addImage(listFileOrange);
		addImage(listFileChicken);
		addImage(listFileEggs);
		// -----------------------------

		System.out.println(totalKeypoit.size());
		totalLabels = new Mat(totalKeypoit.rows(), 1, CvType.CV_32FC1);
		totalCentroid = new Mat(CLUSTERS, 1, CvType.CV_32FC1);
		Core.kmeans(totalKeypoit, CLUSTERS, totalLabels, new TermCriteria(
				TermCriteria.EPS, MAX_ITER, 0.01), 3,
				Core.KMEANS_RANDOM_CENTERS, totalCentroid);

		System.out.println(totalLabels.size());
		System.out.println(totalCentroid.size());
		prepareData(listFileApple, 1.0);
		prepareData(listFileOrange, 2.0);
		prepareData(listFileChicken, 3.0);
		prepareData(listFileEggs, 4.0);
		// ---------------Training-----------
		CvSVMParams params = new CvSVMParams();
		params.set_kernel_type(CvSVM.LINEAR);
		params.set_svm_type(CvSVM.C_SVC);
		params.set_term_crit(new TermCriteria(TermCriteria.EPS, 50000, 1e-8));

		svm = new CvSVM();
		svm.train(dataTraining, labelTraining, new Mat(), new Mat(), params);
		svm.save("D://file_data_training.xml");
		System.out.println("Done training!");
		System.out.println("----------------------");
		// ----------------End Training-----------------------

		for (int i = 0; i < listFileInput.length; i++) {
			System.out.println(listFileInput[i].getAbsolutePath() + " : "
					+ predic(listFileInput[i].getAbsolutePath()));
		}

	}

	public void innit() {
		// -------------- init ------------------
		descriptors = new Mat();
		totalKeypoit = new Mat();
		featureDetector = FeatureDetector.create(FeatureDetector.SURF);
		matOfKeyPoint = new MatOfKeyPoint();
		descriptorExtractor = DescriptorExtractor
				.create(DescriptorExtractor.SURF);
		labelTraining = new Mat();
		dataTraining = new Mat();
		listFileApple = new File("D://Do An//Apple//").listFiles();
		listFileOrange = new File("D://Do An//Orange//").listFiles();
		listFileChicken = new File("D://Do An//Chicken//").listFiles();
		listFileEggs = new File("D://Do An//Eggs//").listFiles();
		listFileInput = new File("D://Do An//Input//").listFiles();
		TOTAL_SAMPLE = listFileApple.length + listFileOrange.length
				+ listFileChicken.length + listFileEggs.length;
		// labelTraining = new Mat(TOTAL_SAMPLE, 1, CvType.CV_32F);

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
	public void prepareData(File[] listFile, double label) {
		for (int i = 0; i < listFile.length; i++) {
			String path = listFile[i].getAbsolutePath();
			int[] histogram = processImage(path);
			Mat tempMat = new Mat(1, histogram.length, CvType.CV_32FC1);
			for (int j = 0; j < histogram.length; j++) {
				tempMat.put(0, j, histogram[j]);
			}
			Mat tempLabel = new Mat(1, 1, CvType.CV_32FC1);
			tempLabel.put(0, 0, label);

			labelTraining.push_back(tempLabel);
			dataTraining.push_back(tempMat);
			System.out
					.println(dataTraining.size() + ":" + labelTraining.size());
		}
	}

	// -----------------------------------------------------------------
	public void addImage(File[] files) {
		for (int i = 0; i < files.length; i++) {
			Mat image = Highgui.imread(files[i].getAbsolutePath(),
					Highgui.CV_LOAD_IMAGE_COLOR);
			// if (image.rows() > 800 || image.cols() > 800) {
			// Imgproc.resize(image, image, new Size(MAX_WIDTH, MAX_HEIGHT));
			// }
			featureDetector.detect(image, matOfKeyPoint);
			descriptorExtractor.compute(image, matOfKeyPoint, descriptors);
			totalKeypoit.push_back(descriptors);
		}
	}

	public int[] processImage(String path) {
		int[] histogram = new int[CLUSTERS];
		Mat img = Highgui.imread(path, Highgui.CV_LOAD_IMAGE_COLOR);
		// if (img.rows() > 800 || img.cols() > 800) {
		Imgproc.resize(img, img, new Size(MAX_WIDTH, MAX_HEIGHT));
		// }

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
				sum = sum
						+ Math.pow(
								(sample.get(0, i)[0] - totalPoint.get(j, i)[0]),
								2);
			}
			result = Math.sqrt(sum);
			if (j == 0 || result < min) {
				min = result;
				index = j;
			}
		}
		return index;
	}

	// -----------------------------------------------------------------------
	public static void main(String[] args) {
		new Main();
	}

}
