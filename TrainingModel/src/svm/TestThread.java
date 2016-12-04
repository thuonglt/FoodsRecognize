package svm;

public class TestThread {
	static int a = 0;

	public static void main(String[] args) {

		Thread t1 = new Thread(new Runnable() {

			public void run() {
				for (int i = 0; i < 50; i++) {
					System.out.println("haha");
					a++;
//					try {
//						Thread.sleep(10);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
				}
			}
		});
		Thread t2 = new Thread(new Runnable() {

			public void run() {
				for (int i = 0; i < 50; i++) {
					System.out.println("hehe");
					a++;
//					try {
//						Thread.sleep(10);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
				}
			}
		});

		t1.start();
		t2.start();
		System.out.println("xxxxxxxxx");
//		try {
//			t1.join();
//			t2.join();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		System.out.println("done!");
		System.out.println(a);
	}
}
