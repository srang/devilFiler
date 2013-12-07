package dfs;

import common.Constants;
import common.DFileID;
import common.Inode;

import java.util.*;

import dblockcache.DBufferCache;
import dblockcache.LocalDBufferCache;

public class LocalDFS extends DFS {
    private List<DFileID> dfiles;
    private Queue<Integer> free;
    private Set<Integer> used;
    private DBufferCache _cache;
    
    private Queue<Inode> freeINodes;
    private Set<Inode> usedINodes;

    @Override
    public void init() {
        dfiles = new ArrayList<DFileID>(Constants.MAX_DFILES);
        used = new HashSet<Integer>();
        free = new LinkedList<Integer>();
        
        usedINodes = new HashSet<Inode>();
        freeINodes = new LinkedList<Inode>();
        
        for (int i = 0; i < Constants.MAX_DFILES; i++) {
            free.add(i);
        }
        
        for (int j = 0; j < (Constants.INODE_SIZE/4); j++){
        	((LinkedList<Inode>) freeINodes).addFirst(new Inode());
        	for(int k = 0; k<Constants.NUM_OF_BLOCKS; k++){
        		for(int m = 0; m<(Constants.BLOCK_SIZE/Constants.INODE_SIZE); m++){
        			freeINodes.peek().setBlockID(k);
        			freeINodes.peek().setOffset(Constants.INODE_SIZE*m);
        		}
        	}
        }
        
    }

    @Override
    public DFileID createDFile() {
        int fileId = free.poll();
        used.add(fileId);
        DFileID newDfile = new DFileID(fileId);
        dfiles.add(newDfile);
        
        Inode inode = freeINodes.poll();
        usedINodes.add(inode);
        Inode fileCopy = inode;
        fileCopy.setfileID(newDfile);
        newDfile.getINode().add(fileCopy);
        
        return newDfile;
        // associate with first iNode, make set of inodes to work with
    }

    @Override
    public void destroyDFile(DFileID dFID) {
        for (int i = 0; i<dFID.getINode().size(); i++){
        	Inode hold = dFID.getINode().get(i);
        	usedINodes.remove(hold);
        	freeINodes.add(hold);
        	dFID.getINode().remove(hold);
        }
    	
    	this.dfiles.remove(dFID);
        this.used.remove(dFID); // file Id is no longer used
        this.free.add(dFID.getDFileID()); // let file descriptor be reused
    }

    @Override
    public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
    	/* File bounds check */
    	if(dFID.fileSize < startOffset + count) {
    		return -1; 
    	}    	
    	for (int j = 0; j<dFID.getINode().size(); j++){
    		for (int i = 0; i<dFID.getINode().get(j).getBlockMap().size(); i++){
        		_cache.getBlock(dFID.getINode().get(j).getBlockMap().get(i)).read(buffer, (startOffset+(i*Constants.BLOCK_SIZE)), (count-(i*Constants.BLOCK_SIZE)));
        	}
    	}
    	return 0; // return statement?
    }

    @Override
    public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
    	boolean expandFile = dFID.fileSize < count;
    	/*
    	 * Following section assumes (x+n-1)/n = ceil(x/n)
    	 */
    	if(expandFile && (dFID.fileSize+Constants.BLOCK_SIZE-1)/Constants.BLOCK_SIZE < (count+Constants.BLOCK_SIZE-1)/Constants.BLOCK_SIZE) {//need another block 
    		if(((dFID.fileSize+Constants.BLOCK_SIZE-1)/Constants.BLOCK_SIZE)/(Constants.INODE_SIZE/Constants.BLOCK_ADDRESS_SIZE-2)>
    		((count+Constants.BLOCK_SIZE-1)/Constants.BLOCK_SIZE)/(Constants.INODE_SIZE/Constants.BLOCK_ADDRESS_SIZE-2)){
    			//append inode
    		}
    		//append memory block
    	}
    	
    	for (int j = 0; j<dFID.getINode().size(); j++){
    		for (int i = 0; i<dFID.getINode().get(j).getBlockMap().size(); i++){
        		_cache.getBlock(dFID.getINode().get(j).getBlockMap().get(i)).write(buffer, (startOffset+(i*Constants.BLOCK_SIZE)), (count-(i*Constants.BLOCK_SIZE)));
        	}
    	}
    	dFID.fileSize = (expandFile) ? count : dFID.fileSize;
    	return 0;
    }

    @Override
    public int sizeDFile(DFileID dFID) {
    	return dFID.fileSize;
    }

    @Override
    public List<DFileID> listAllDFiles() {
        return this.dfiles;
    }

    @Override
    public void sync() {
    	this._cache.sync();

    }
}
