package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

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
		File file = new File("DFiler.log");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintStream output = System.out;
		PrintStream ps = new PrintStream(fos); // switch these interchangeably for terminal and log-file output
		System.setOut(output);
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


        DFileID c = dfs.createDFile();
        byte[] file1 = "The quick brown fox jumped over the lazy dog".getBytes();
        dfs.write(c, file1, 0, file1.length);
        out = new byte[file1.length];
        dfs.read(c, out, 0, file1.length);
        System.out.println(new String(out));

    }

}
