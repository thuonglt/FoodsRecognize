package svm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

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

public class Main_Ver2 {
	private final int MAX_WIDTH = 400;
	private final int MAX_HEIGHT = 400;

	private final int MAX_ITER = 100;
	private final int CLUSTERS = 450;
	private int TOTAL_SAMPLE = 0;
	public int count_label = 0;

	Mat descriptors, labelTraining, dataTraining, totalKeypoit, totalLabels,
			totalCentroid;
	FeatureDetector featureDetector;
	MatOfKeyPoint matOfKeyPoint;
	DescriptorExtractor descriptorExtractor;
	CvSVM svm;

	Mat[] arrayData, arrayLabel;

	File[] listSample, listFileInput;

	public Main_Ver2() {
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

		totalLabels = new Mat(totalKeypoit.rows(), 1, CvType.CV_32FC1);
		totalCentroid = new Mat(CLUSTERS, 1, CvType.CV_32FC1);
		System.out.println("TOTALKEYPOINT: " + totalKeypoit.size());
		Core.kmeans(totalKeypoit, CLUSTERS, totalLabels, new TermCriteria(
				TermCriteria.EPS, MAX_ITER, 1e-5), 3, Core.KMEANS_PP_CENTERS,
				totalCentroid);
		writeBOW(totalCentroid, "BOW_SURF.DAT");
		System.out.println(totalLabels.size());
		System.out.println(totalCentroid.size());
		// ------------------------
		for (int i = 0; i < listSample.length; i++) {
			File[] listData = listSample[i].listFiles();
			prepareData(listData, Float.parseFloat(listSample[i].getName()));
			System.out.println("Prepare folder " + i);
		}

		System.out.println(dataTraining.size());
		System.out.println(labelTraining.size());
		// ---------------Training-----------
		CvSVMParams params = new CvSVMParams();
		params.set_kernel_type(CvSVM.LINEAR);
		params.set_svm_type(CvSVM.C_SVC);
		params.set_C(0.1);
		params.set_term_crit(new TermCriteria(TermCriteria.EPS
				| TermCriteria.COUNT, (int) 1e7, 1e-6));

		svm = new CvSVM();
		svm.train(dataTraining, labelTraining, new Mat(), new Mat(), params);
		svm.save("file_data_training_450_SURF.xml");
		System.out.println("Done training!");
		System.out.println("Support vector count: "
				+ svm.get_support_vector_count());
		System.out.println("----------------------");

		// ----------------End Training-----------------------

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
		listSample = new File("Sample").listFiles();
		listFileInput = new File("Input").listFiles();
		arrayData = new Mat[listSample.length];
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
			featureDetector.detect(image, matOfKeyPoint);
			descriptorExtractor.compute(image, matOfKeyPoint, descriptors);
			totalKeypoit.push_back(descriptors);
		}
	}

	public int[] processImage(String path) {
		int[] histogram = new int[CLUSTERS];
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
			if (result > max) {
				max = result;
				index = j;
			}
		}
		return index;
	}

	public void writeBOW(Mat mat, String path) {
		try {
			System.out.println(mat.rows() + "-:-" + mat.cols());
			PrintWriter pr = new PrintWriter(new File(path));
			pr.println(mat.rows() + " " + mat.cols());
			for (int i = 0; i < mat.rows(); i++) {
				for (int j = 0; j < mat.cols(); j++) {
					pr.print(mat.get(i, j)[0]);
					pr.print(" ");
				}
				pr.println();
			}
			pr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// -----------------------------------------------------------------------
	public static void main(String[] args) {
		new Main_Ver2();
	}

}
