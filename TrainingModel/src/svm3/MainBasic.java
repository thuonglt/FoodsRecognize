package svm3;

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

public class MainBasic {
	private final int MAX_WIDTH = 500;

	private final int MAX_ITER = 100;
	private final int CLUSTERS = 50;
	private int TOTAL_SAMPLE = 0;
	public int count_label = 0;
	double w, h;
	int row_count = 0;

	Mat descriptors, labelTraining, dataTraining, totalKeypoit, totalLabels,
			totalCentroid;
	FeatureDetector featureDetector;
	MatOfKeyPoint matOfKeyPoint;
	DescriptorExtractor descriptorExtractor;
	CvSVM svm;

	Mat[] arrayData, arrayLabel;
	Thread[] arrayThread;

	File[] listSample, listFileInput;

	private int TOTAL_INPUT;
	private int TOTAL_INPUT_TRUE;

	public MainBasic() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		innit();
		// Load all list file data
		for (int i = 0; i < listSample.length; i++) {
			File[] listData = listSample[i].listFiles();
			int label = Integer.parseInt(listSample[i].getName());
			System.out.println("Add folder " + label);
			addImage(listData, label);
			TOTAL_SAMPLE = TOTAL_SAMPLE + listData.length;
		}
		System.out.println("TOTAL SAMPLE: " + TOTAL_SAMPLE);

		// ---------------Training-----------
		System.out.println("Start Training!");
		CvSVMParams params = new CvSVMParams();
		params.set_kernel_type(CvSVM.LINEAR);
		params.set_svm_type(CvSVM.C_SVC);
		params.set_term_crit(new TermCriteria(TermCriteria.EPS
				| TermCriteria.COUNT, 100000, 1e-8));

		svm = new CvSVM();
		svm.train(dataTraining, labelTraining, new Mat(), new Mat(), params);
		svm.save("hello.xml");
		System.out.println("Done training!");
		System.out.println("----------------------");
		// ----------------End Training-----------------------
		// --------------- Start Predic--------------
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
	public void addImage(File[] files, int label) {
		for (int i = 0; i < files.length; i++) {
			matOfKeyPoint.release();
			descriptors.release();
			totalKeypoit.release();
			Mat image = Highgui.imread(files[i].getAbsolutePath(),
					Highgui.CV_LOAD_IMAGE_GRAYSCALE);
			w = MAX_WIDTH;
			h = ((double) image.height() / (double) image.width()) * MAX_WIDTH;

			Imgproc.resize(image, image, new Size(w, h));

			featureDetector.detect(image, matOfKeyPoint);
			descriptorExtractor.compute(image, matOfKeyPoint, descriptors);
			// totalKeypoit.push_back(descriptors);
			Mat tempLabelsKmean = new Mat(totalKeypoit.rows(), 1,
					CvType.CV_32FC1);
			Mat tempCentroid = new Mat(CLUSTERS, 1, CvType.CV_32FC1);
			Core.kmeans(descriptors, CLUSTERS, tempLabelsKmean,
					new TermCriteria(TermCriteria.EPS, MAX_ITER, 1e-8), 3,
					Core.KMEANS_PP_CENTERS, tempCentroid);
			Mat tempData = new Mat(1, CLUSTERS * 64, CvType.CV_32FC1);
			Mat tempLabel = new Mat(1, 1, CvType.CV_32FC1);
			int tempCount_cols = 0;
			for (int j = 0; j < tempCentroid.rows(); j++) {
				for (int j2 = 0; j2 < tempCentroid.cols(); j2++) {
					tempData.put(row_count, tempCount_cols,
							tempCentroid.get(j, j2)[0]);
					tempCount_cols++;
				}
			}
			dataTraining.push_back(tempData);
			tempLabel.put(0, 0, label);
			labelTraining.push_back(tempLabel);
			row_count++;
			System.out.println("Add image:" + row_count + " data size: "
					+ dataTraining.size() + " labelTraining: "
					+ labelTraining.size() + " - " + label);
		}
		// totalCentroid.push_back(tempCentroid);
		System.out.println("data Trainning: " + dataTraining.size());
	}

	public double predic(String path) {
		double result = -1;
		matOfKeyPoint.release();
		descriptors.release();
		totalKeypoit.release();
		Mat image = Highgui.imread(path, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		w = MAX_WIDTH;
		h = ((double) image.height() / (double) image.width()) * MAX_WIDTH;
		Imgproc.resize(image, image, new Size(w, h));
		featureDetector.detect(image, matOfKeyPoint);
		descriptorExtractor.compute(image, matOfKeyPoint, descriptors);

		Mat tempLabelsKmean = new Mat(totalKeypoit.rows(), 1, CvType.CV_32FC1);
		Mat tempCentroid = new Mat(CLUSTERS, 1, CvType.CV_32FC1);
		Core.kmeans(descriptors, CLUSTERS, tempLabelsKmean, new TermCriteria(
				TermCriteria.EPS, MAX_ITER, 1e-6), 3,
				Core.KMEANS_RANDOM_CENTERS, tempCentroid);
		Mat tempData = new Mat(1, CLUSTERS * 64, CvType.CV_32FC1);
		int tempCount_cols = 0;
		for (int j = 0; j < tempCentroid.rows(); j++) {
			for (int j2 = 0; j2 < tempCentroid.cols(); j2++) {
				tempData.put(0, tempCount_cols, tempCentroid.get(j, j2)[0]);
				tempCount_cols++;
			}
		}
		System.out.println("Size Input: " + tempData.size());
		result = svm.predict(tempData);
		return result;
	}

	// -----------------------------------------------------------------------
	public static void main(String[] args) {
		new MainBasic();
	}

}
