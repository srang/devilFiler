package test;

import common.DFileID;
import dfs.LocalDFS;

public class MultiThreadedTest {
    public static void main(String[] args) throws InterruptedException {
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
        for (DFileID dFileID : dfs.listAllDFiles()) {
            System.out.println(dFileID);
        }
    }
}
