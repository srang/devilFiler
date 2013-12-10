package test;

import common.DFileID;
import dfs.LocalDFS;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LargeFileTest {
	
	public LargeFileTest(){
		
	}
	public void run(){
		//File log = new File("Test.log");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream("DeFiler.log",true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintStream output = System.out;
		PrintStream ps = new PrintStream(fos); // switch these interchangeably for terminal and log-file output
		System.setOut(ps);
		
		System.out.println("2. LargeFileTest.java: create a file and write a large text file to it (Bartelby the Scrivener)," +
		"requiring it to use multiple blocks of memory, then read that file out\n (Feel free to scroll past the large block of" +
				"text and move on to the output from our third test)\n");
        List<Integer> list = new ArrayList<Integer>();
        File file = new File("src/test/melville.txt");
        BufferedReader reader = null;
        StringBuffer melville = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String text;
            while ((text = reader.readLine()) != null) {
                melville.append(text);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }

        LocalDFS dfs = new LocalDFS();
        dfs.init();

        byte[] file1 = melville.toString().getBytes();
        DFileID c = dfs.createDFile();
        dfs.write(c, file1, 0, file1.length);
        byte[] out = new byte[file1.length];
        dfs.read(c, out, 0, file1.length);
        System.out.println(new String(out));
        
        System.out.println("\nEnd of LargeFileTest.java\n");
        
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
