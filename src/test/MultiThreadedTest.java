package test;

import dfs.LocalDFS;

public class MultiThreadedTest {
    public static void main(String[] args) {
        LocalDFS dfs = new LocalDFS();
        dfs.init();
        byte[] file1 = "The quick brown fox jumped over the lazy dog".getBytes();
        byte[] file2 = "What does the fox say?".getBytes();

        Thread clientA = new Thread(new Client(dfs, file1));
        clientA.start();
        Thread clientB = new Thread(new Client(dfs, file2));
        clientB.start();
    }
}
