package dfs;

import common.Constants;
import common.DFileID;
import common.Inode;

import java.io.IOException;
import java.util.*;

import dblockcache.DBufferCache;
import dblockcache.LocalDBufferCache;
import virtualdisk.LocalVirtualDisk;
import virtualdisk.VirtualDisk;

public class LocalDFS extends DFS {
    private List<DFileID> dfiles;
    private Queue<Integer> freeFileIDs;
    private Set<Integer> usedFileIDs;
    private DBufferCache _cache;
    
    private Queue<Inode> freeINodes;
    private Set<Inode> usedINodes;

    private Queue<Integer> freeBlocks;
    private Set<Integer> usedBlocks;
    @Override
    public void init() {
        dfiles = new ArrayList<DFileID>(Constants.MAX_DFILES);
        usedFileIDs = new HashSet<Integer>();
        freeFileIDs = new LinkedList<Integer>();
        
        usedINodes = new HashSet<Inode>();
        freeINodes = new LinkedList<Inode>();
        
        usedBlocks = new HashSet<Integer>();
        freeBlocks = new LinkedList<Integer>();
        
        
        for (int i = 0; i < Constants.MAX_DFILES; i++) {
            freeFileIDs.add(i * Constants.BYTE_OFFSET);
        }
        int maxNumberOfInodes = Math.ceil(Constants.MAX_DFILES/
        for (int j = 0; j < (Constants.INODE_SIZE/Constants.BLOCK_ADDRESS_SIZE); j++){
//        	((LinkedList<Inode>) freeINodes).addFirst(new Inode());
            Inode newInode = new Inode();
            newInode.setBlockID(j);
//        	for(int k = 0; k<Constants.MAX_DFILES; k++){
//        		for(int m = 0; m<(Constants.BLOCK_SIZE/Constants.INODE_SIZE); m++){
//                    newInode.setBlockID(k);
//        			freeINodes.peek().setBlockID(k);
//        			freeINodes.peek().setOffset(Constants.INODE_SIZE*m);
//        		}
//        	}
            freeINodes.add(newInode);
        }
        
        for(int n = 0; n<(Constants.NUM_OF_BLOCKS-(Constants.INODE_SIZE*(this.usedINodes.size()+this.freeINodes.size()))); n++){
        	freeBlocks.add(n);
        }

        // init virtual disk
        try {
            VirtualDisk disk = new LocalVirtualDisk();
            _cache = new LocalDBufferCache(Constants.NUM_OF_CACHE_BLOCKS, disk);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DFileID createDFile() {
        int fileId = freeFileIDs.poll();
        usedFileIDs.add(fileId);
        DFileID newDfile = new DFileID(fileId);
        dfiles.add(newDfile);
        
        Inode inode = freeINodes.poll();
        usedINodes.add(inode);
        Inode fileCopy = inode;
        fileCopy.setfileID(newDfile);
        newDfile.getINodeList().add(fileCopy);
        
        return newDfile;
        // associate with first iNode, make set of inodes to work with
    }

    @Override
    public void destroyDFile(DFileID dFID) {

    	for (int i = 0; i<dFID.getINodeList().size(); i++){
        	Inode hold = dFID.getINodeList().get(i);
        	// memory block freeing
        	for (int j = 0; j<hold.getBlockMap().size(); j++){
        		usedBlocks.remove(hold.getBlockMap().get(j));
        		freeBlocks.add(hold.getBlockMap().get(j));
        	}
        	// disassociate all blocks with this iNode
        	for(int k = 0; k<hold.getBlockMap().size(); k++){
        		hold.getBlockMap().remove(hold.getBlockMap().get(0));
        	}
        	// remove iNodes from respective spots
        	usedINodes.remove(hold);
        	freeINodes.add(hold);
        	dFID.getINodeList().remove(hold);
        }
    	
    	this.dfiles.remove(dFID);
        this.usedFileIDs.remove(dFID); // file Id is no longer used
        this.freeFileIDs.add(dFID.getDFileID()); // let file descriptor be reused
    }

    @Override
    public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
    	/* File bounds check */
    	if(dFID.fileSize < startOffset + count) {
    		return -1; 
    	}    	
    	for (int j = 0; j<dFID.getINodeList().size(); j++){
    		for (int i = 0; i<dFID.getINodeList().get(j).getBlockMap().size(); i++){
        		_cache.getBlock(dFID.getINodeList().get(j).getBlockMap().get(i)).read(buffer, (startOffset+(i*Constants.BLOCK_SIZE)), (count-(i*Constants.BLOCK_SIZE)));
        	}
    	}
    	return 0; // return statement?
    }

    @Override
    public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
    	boolean expandFile = dFID.fileSize < count;
    	ArrayList<Inode> fileINodes = dFID.getINodeList();
    	/*
    	 * Following section assumes (x+n-1)/n = ceil(x/n)
    	 */
    	if(expandFile && (dFID.fileSize+Constants.BLOCK_SIZE-1)/Constants.BLOCK_SIZE < (count+Constants.BLOCK_SIZE-1)/Constants.BLOCK_SIZE) {//need another block 
    		if(((dFID.fileSize+Constants.BLOCK_SIZE-1)/Constants.BLOCK_SIZE)/(Constants.INODE_SIZE/Constants.BLOCK_ADDRESS_SIZE-2)>
    		((count+Constants.BLOCK_SIZE-1)/Constants.BLOCK_SIZE)/(Constants.INODE_SIZE/Constants.BLOCK_ADDRESS_SIZE-2)){
    			fileINodes.add(freeINodes.poll());
    		}
    		fileINodes.get(fileINodes.size()-1).add(freeBlocks.poll());
    	}
    	
    	for (int j = 0; j<fileINodes.size(); j++){
    		for (int i = 0; i<fileINodes.get(j).getBlockMap().size(); i++){
        		_cache.getBlock(fileINodes.get(j).getBlockMap().get(i)).write(buffer, (startOffset+(i*Constants.BLOCK_SIZE)), (count-(i*Constants.BLOCK_SIZE)));
        		// allocate block as used -- is this right?
        		if (freeBlocks.contains(fileINodes.get(j).getBlockMap().get(i))){
        			usedBlocks.add(fileINodes.get(j).getBlockMap().get(i));
        			freeBlocks.remove(fileINodes.get(j).getBlockMap().get(i));
        		}
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
