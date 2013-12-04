package dfs;

import common.Constants;
import common.DFileID;

import java.util.*;

public class LocalDFS extends DFS {
    private List<DFileID> dfiles;
    private Queue<Integer> free;
    private Set<Integer> used;

    @Override
    public void init() {
        dfiles = new ArrayList<DFileID>(Constants.MAX_DFILES);
        used = new HashSet<Integer>();
        free = new LinkedList<Integer>();
        for (int i = 0; i < Constants.MAX_DFILES; i++) {
            free.add(i);
        }
    }

    @Override
    public DFileID createDFile() {
        int fileId = free.poll();
        used.add(fileId);
        DFileID newDfile = new DFileID(fileId);
        dfiles.add(newDfile);
        return newDfile;
    }

    @Override
    public void destroyDFile(DFileID dFID) {
        dfiles.remove(dFID);
        used.remove(dFID); // file Id is no longer used
        free.add(dFID.getDFileID()); // let file descriptor be reused
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
        return dfiles;
    }

    @Override
    public void sync() {

    }
}
