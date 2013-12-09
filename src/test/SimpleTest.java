package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

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


        DFileID c = dfs.createDFile();
        byte[] file1 = "The quick brown fox jumped over the lazy dog".getBytes();
        String str = "";
        try {
			Scanner s = new Scanner(new File("src/test/melville.txt"));
			while(s.hasNext()){
				str += s.next();
			}
			System.out.println(str.length());
		} catch (IOException e) {
			e.printStackTrace();
		}
        byte[] file2 = str.getBytes();
        DFileID file2ID = dfs.createDFile();
        dfs.write(c, file1, 0, file1.length);
        dfs.write(file2ID, file2, 0, file2.length);
        byte[] out2 = new byte[file2.length];
        out = new byte[file1.length];
        dfs.read(c, out, 0, file1.length);
        dfs.read(file2ID, out2, 0, out2.length);
        System.out.println(new String(out));
        System.out.println(new String(out2));

    }

}
