package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import common.DFileID;
import dfs.*;

public class LoadingDiskTest {
	public LoadingDiskTest() {
		
	}
	
	public void run() 
	 {
		//File log = new File("DeFiler.log");
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream("Defiler.log",true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintStream output = System.out;
		PrintStream ps = new PrintStream(fos); // switch these interchangeably for terminal and log-file output
		System.setOut(ps);
		DFS d = new LocalDFS();
		d.init();
		System.out.println("Testing Persistence");
		List<DFileID> dfs = d.listAllDFiles();
		System.out.println("Listing files loaded from disk");
		for(int i = 0; i < dfs.size(); i++) {
			System.out.println(dfs.get(i).getDFileID());
		}
		System.out.println("LoadingDiskTest.java Persistence test complete");
        ps.close();
        output.close();
        try {
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		d.stop();
	}

}
