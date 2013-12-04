package dfs;

import common.Constants;
import common.DFileID;

import java.util.List;

public class LocalDFS extends DFS {
    private DFileID[] dfiles;


    @Override
    public void init() {
        dfiles = new DFileID[Constants.MAX_DFILES];
    }

    @Override
    public DFileID createDFile() {
        DFileID dFID = new DFileID();
    	return dFID;
    }

    @Override
    public void destroyDFile(DFileID dFID) {
        dFID.getDFileID();
    }

    @Override
    public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
        return 0;
    }

    @Override
    public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
        return 0;
    }

    @Override
    public int sizeDFile(DFileID dFID) {
        return 0;
    }

    @Override
    public List<DFileID> listAllDFiles() {
        return null;
    }

    @Override
    public void sync() {

    }
}
