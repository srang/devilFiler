package dfs;

import common.Constants;
import common.DFileID;

import java.util.*;

import dblockcache.DBufferCache;
import dblockcache.LocalDBufferCache;

public class LocalDFS extends DFS {
    private List<DFileID> dfiles;
    private Queue<Integer> free;
    private Set<Integer> used;
    private DBufferCache dbuff;

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
        this.dfiles.remove(dFID);
        this.used.remove(dFID); // file Id is no longer used
        this.free.add(dFID.getDFileID()); // let file descriptor be reused
    }

    @Override
    public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
        // don't know if this is quite right...
    	// basically DFS read/write calls just call specific DBUffer read/writes
    	
    	for (int i = 0; i<dFID.getINode().getBlockMap().size(); i++){
        	this.dbuff.getBlock(dFID.getINode().getBlockMap().get(i)).read(buffer, (startOffset+(i*Constants.BLOCK_SIZE)), (count-(i*Constants.BLOCK_SIZE)));
        }
    	return 0; // return statement?
    }

    @Override
    public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
    	for (int i = 0; i<dFID.getINode().getBlockMap().size(); i++){
        	this.dbuff.getBlock(dFID.getINode().getBlockMap().get(i)).write(buffer, (startOffset+(i*Constants.BLOCK_SIZE)), (count-(i*Constants.BLOCK_SIZE)));
        }
    	
    	dFID.getINode().fileSize+=buffer.length;
    	
    	return 0;
    }

    @Override
    public int sizeDFile(DFileID dFID) {
    	return dFID.getINode().fileSize;
    }

    @Override
    public List<DFileID> listAllDFiles() {
        return this.dfiles;
    }

    @Override
    public void sync() {
    	this.dbuff.sync();

    }
}
