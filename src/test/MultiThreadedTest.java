package test;

import dfs.LocalDFS;

public class MultiThreadedTest {
    public static void main(String[] args) throws InterruptedException {
        LocalDFS dfs = new LocalDFS();
        dfs.init();
        byte[] file1 = "The quick brown fox jumped over the lazy dog".getBytes();
        byte[] file2 = "What does the fox say?".getBytes();
        byte[] file3 = "sheep, Zip Cars, and threads, oh my!".getBytes();

        Thread clientA = new Thread(new Client(dfs, file1, 750));
        Thread clientB = new Thread(new Client(dfs, file2, 500));
        Thread clientC = new Thread(new Client(dfs, file3));
        clientA.start();
        clientB.start();
        clientC.start();
    }
}
