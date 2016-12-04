// A simple demo of JNI interface to implement SIFT detection for Android application using nonfree module in OpenCV4Android.
// Created by Guohui Wang 
// Email: robertwgh_at_gmail_com
// Data: 2/26/2014

package phantom.lib;

public class NonfreeJNILib {
	public NonfreeJNILib() {
		// TODO Auto-generated constructor stub
	}

	static {
		try {
			// Load necessary libraries.
			System.loadLibrary("opencv_java");
			System.loadLibrary("nonfree");
			System.loadLibrary("nonfree_jni");
			System.loadLibrary("nonfree_test");
		} catch (UnsatisfiedLinkError e) {
			System.err.println("Native code library failed to load.\n" + e);
		}
	}

	// public static native void runDemo();
	public static native void surfCompute(long image, long mkeypoints);

	public static native int calculateDistance(long sample, long total);

}