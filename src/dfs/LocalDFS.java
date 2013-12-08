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
            freeFileIDs.add(i << 8);//shift bits over to allow for inode index
        }

        initInodes();

        // our free blocks start after the inode region
        for (int n = Constants.INODE_REGION_SIZE + 1; n < Constants.NUM_OF_BLOCKS; n++) {
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

    private void initInodes() {
        int maxNumberOfInodes = Constants.BLOCK_SIZE/Constants.INODE_SIZE * Constants.INODE_REGION_SIZE;
        int blockIndex = 1;
        int blockOffset = 0;
        for (int i = 0; i < maxNumberOfInodes; i++) {
            Inode newInode = new Inode();
            newInode.setBlockID(blockIndex); // assign the inode a block
            newInode.setOffset(blockOffset);// assign the inode an offset within the block
            // if we are past the end of a block, go to the next one
            if (blockOffset + Constants.INODE_SIZE >= Constants.BLOCK_SIZE) {
                blockOffset = 0;
                blockIndex++;
            } else {
                blockOffset += Constants.INODE_SIZE;
            }

            freeINodes.add(newInode);
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
        inode.setfileID(newDfile);
        newDfile.getINodeList().add(inode);
        updateDiskINodes(inode);
        
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
    	int num_blocks_needed = (count+Constants.BLOCK_SIZE-1)/Constants.BLOCK_SIZE;
    	int num_blocks_have   = (dFID.fileSize+Constants.BLOCK_SIZE-1)/Constants.BLOCK_SIZE;
    	int num_inodes_needed = ((dFID.fileSize+Constants.BLOCK_SIZE-1)/Constants.BLOCK_SIZE)/(Constants.INODE_SIZE/Constants.BLOCK_ADDRESS_SIZE-2);
    	int num_inodes_have   = ((count+Constants.BLOCK_SIZE-1)/Constants.BLOCK_SIZE)/(Constants.INODE_SIZE/Constants.BLOCK_ADDRESS_SIZE-2);
    	if(expandFile) {//need another block 
    		if(num_inodes_needed > num_inodes_have){//need more inodes & blocks
    			for(int i = 0; i < (num_inodes_needed-num_inodes_have); i++) {
    				Inode inode = freeINodes.poll();
    				usedINodes.add(inode);
    				inode.setfileID(dFID);
    				fileINodes.add(inode);
    				for(int j = 0; j < Constants.BLOCK_ADDRESSES_PER_INODE; j++) {
    					int bID = freeBlocks.poll();
    		    		usedBlocks.add(bID);
    		    		fileINodes.get(fileINodes.size()-1).add(bID);	
    				}
    				updateDiskINodes(fileINodes.get(fileINodes.size()-1));
    			}
    		} else if(num_blocks_needed > num_blocks_have){//just need more blocks
    			for(int i = 0; i < num_blocks_needed-num_blocks_have; i++) {
    	   			int bID = freeBlocks.poll();
            		usedBlocks.add(bID);
            		fileINodes.get(fileINodes.size()-1).add(bID);
    			}
    			updateDiskINodes(fileINodes.get(fileINodes.size()-1));
    		}    		
    	}
    	
    	for (int j = 0; j<fileINodes.size(); j++){
    		ArrayList<Integer> fileBlockMap = fileINodes.get(j).getBlockMap();
    		for (int i = 0; i<fileBlockMap.size(); i++){
        		_cache.getBlock(fileBlockMap.get(i)).write(buffer, (startOffset+(i*Constants.BLOCK_SIZE)), (count-(i*Constants.BLOCK_SIZE)));
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
    
    private void updateDiskINodes(Inode i) {
    	_cache.getBlock(i.getBlockID());
    	//byte[] inodeInfo = new byte[]
    }
}
