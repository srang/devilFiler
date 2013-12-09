package test;

import common.DFileID;
import dfs.DFS;

/**
 * Created by zmichaelov on 12/8/13.
 */
public class Client implements Runnable {
    private DFS dfs;
    private byte[] data;
    public Client(DFS dfs, byte[] data) {
        this.dfs = dfs;
        this.data = data;
    }

    @Override
    public void run() {

        DFileID b = dfs.createDFile();
        dfs.write(b, data, 0, data.length);
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        byte[] out = new byte[data.length];
        dfs.read(b, out, 0, out.length);
        System.out.println(new String(out));
    }
}
