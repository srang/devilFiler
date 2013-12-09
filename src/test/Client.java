package test;

import common.DFileID;
import dfs.DFS;

/**
 * Created by zmichaelov on 12/8/13.
 */
public class Client implements Runnable {
    private DFS dfs;
    private byte[] data;
    private int sleep = -1;
    public Client(DFS dfs, byte[] data) {
        this.dfs = dfs;
        this.data = data;
    }

    public Client(DFS dfs, byte[] data, int sleep) {
        this.dfs = dfs;
        this.data = data;
        this.sleep = sleep;
    }

    @Override
    public void run() {

        DFileID b = dfs.createDFile();
        dfs.write(b, data, 0, data.length);
        if (sleep != -1) {
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        byte[] out = new byte[data.length];
        dfs.read(b, out, 0, out.length);
        System.out.println(new String(out));
    }
}
