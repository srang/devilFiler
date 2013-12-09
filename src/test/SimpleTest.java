package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import common.DFileID;
import dfs.LocalDFS;

public class SimpleTest {

	public SimpleTest() {
		
	}
	
	public void run() {
		File log = new File("Test.log");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(log);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintStream output = System.out;
		PrintStream ps = new PrintStream(fos); // switch these interchangeably for terminal and log-file output
		System.setOut(ps);
		
		System.out.println("1. SimpleTest.java: test file creation for two files, writing to single file twice, reading" +
		"from two separate files, and file deletion for one file\n");
		LocalDFS dfs = new LocalDFS();
		dfs.init();
		System.out.println("Create first file and write integers 12, 1, 3 to it, then read them out\n");
		DFileID b = dfs.createDFile();
		byte[] buffer = new byte[]{12,1,3};
		byte[] buffer2 = new byte[]{20,5,9,13};
		dfs.write(b, buffer, 0, 3);
		byte[] out = new byte[3];
		byte[] out2 = new byte[4];
		dfs.read(b, out, 0, 3);
		for(int i = 0; i < 3; i++) {
			System.out.println(out[i]);
		}
		
		System.out.println("\nOverwrite current integers in this file, with 20, 5, 9, and 13, then read those out\n");
		dfs.write(b, buffer2, 0, 4);
		dfs.read(b, out2, 0, 4);
		for(int j = 0; j<4; j++){
			System.out.println(out2[j]);
		}

		System.out.println("\nCreate second file and write a string to it and read it out\n");
        DFileID c = dfs.createDFile();
        byte[] file1 = "The quick brown fox jumped over the lazy dog".getBytes();
        dfs.write(c, file1, 0, file1.length);
        out = new byte[file1.length];
        dfs.read(c, out, 0, file1.length);
        System.out.println(new String(out));
        
        System.out.println("\nAll DFileIDs in DFiler:");
        for (DFileID dFileID : dfs.listAllDFiles()) {
            System.out.println(dFileID);
        }
        System.out.println("\nDestroying the first DFileID");
        dfs.destroyDFile(b);
        
        System.out.println("\nDFileIDs remaining in DFiler:");
        for (DFileID dFileID : dfs.listAllDFiles()) {
            System.out.println(dFileID);
        }
        
        System.out.println("\nEnd of SimpleTest.java\n");
        ps.close();
        output.close();
        try {
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
