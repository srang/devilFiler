package test;

import common.DFileID;

import dfs.LocalDFS;

public class SimpleTest {
	public static void main (String[] args) {
		SimpleTest test = new SimpleTest();
		synchronized(test) {
			test.run();
		}
	}


	public SimpleTest() {
		
	}
	
	public void run() {
		LocalDFS dfs = new LocalDFS();
		dfs.init();
		DFileID b = dfs.createDFile();
		byte[] buffer = new byte[]{12,1,3};
		dfs.write(b, buffer, 0, 3);
		byte[] out = new byte[3];
		dfs.read(b, out, 0, 3);
		for(int i = 0; i < 3; i++) {
			System.out.println(out[i]);
		}
	}

}
