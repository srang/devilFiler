package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import common.DFileID;
import dfs.LocalDFS;

public class MultiThreadedTest {
    public MultiThreadedTest() {
    
    }
    
    public void run()  throws InterruptedException{
    	
		File log = new File("MultiThreadedTest.log");
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
    	System.out.println("3. Access DFiler with three client threads, writing 3 strings to three separate files," +
    			"and then read those out.  Finally, print out all used DFileIDs seen by the DFiler\n");
        LocalDFS dfs = new LocalDFS();
        dfs.init();
        byte[] file1 = "The quick brown fox jumped over the lazy dog".getBytes();
        byte[] file2 = "What does the fox say?".getBytes();
        byte[] file3 = "sheep, Zip Cars, and threads, oh my!".getBytes();

        Client client = new Client(dfs, file1);
        Thread t1 = new Thread(client);
        Client client1 = new Client(dfs, file2);
        Thread t2 = new Thread(client1);
        Client client2 = new Client(dfs, file3);
        Thread t3 = new Thread(client2);
        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        System.out.println(client.read());
        System.out.println(client1.read());
        System.out.println(client2.read());
        System.out.println("\nDFileIDs in the DFiler:");
        for (DFileID dFileID : dfs.listAllDFiles()) {
            System.out.println(dFileID);
        }
        
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
