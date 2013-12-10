package test;

public class Main {
	public static void main (String[] args) {
		SimpleTest test1 = new SimpleTest();
		LargeFileTest test2 = new LargeFileTest();
		MultiThreadedTest test3 = new MultiThreadedTest();
		LoadingDiskTest test4 = new LoadingDiskTest();
		synchronized(test1) {
			test1.run();
		}
		System.out.println("\n");
		synchronized(test2){
			test2.run();
		}
		System.out.println("\n");
		synchronized(test3){
			try {
				test3.run();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		synchronized(test4) {
			test4.run();
		}
	}
}
