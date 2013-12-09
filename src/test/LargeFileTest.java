package test;

import common.DFileID;
import dfs.LocalDFS;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LargeFileTest {
    public static void main(String[] args) {
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
    }
}
