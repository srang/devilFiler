package dfs;

import common.Constants;
import common.DFileID;
import common.Inode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import dblockcache.DBuffer;
import dblockcache.DBufferCache;
import dblockcache.LocalDBufferCache;
import virtualdisk.LocalVirtualDisk;
import virtualdisk.VirtualDisk;

public class LocalDFS extends DFS {
    private DFileID[] dfiles;
    private Queue<Integer> freeFileIDs;
    private Set<Integer> usedFileIDs;
    private DBufferCache _cache;
    
    private Queue<Inode> freeINodes;
    private Set<Inode> usedINodes;

    private Queue<Integer> freeBlocks;
    private Set<Integer> usedBlocks;
    @Override
    public void init() {
        dfiles = new DFileID[Constants.MAX_DFILES];
        
        usedFileIDs = new HashSet<Integer>();
        freeFileIDs = new LinkedList<Integer>();
        
        usedINodes = new HashSet<Inode>();
        freeINodes = new LinkedList<Inode>();
        
        usedBlocks = new HashSet<Integer>();
        freeBlocks = new LinkedList<Integer>();
        
        
        for (int i = 1; i < Constants.MAX_DFILES; i++) {
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
        rebuild();
    }

    private void rebuild() {
    	for(int i = 0; i < Constants.INODE_REGION_SIZE; i++) {
    		DBuffer buff = _cache.getBlock(i + 1); //Ignore the first block but still read right num blocks
    		byte[] block = new byte[Constants.BLOCK_SIZE];
    		buff.read(block, 0, Constants.BLOCK_SIZE);
    		for(int j = 0; j < Constants.BLOCK_SIZE/Constants.INODE_SIZE; j++) {//Inodes per block
    			byte[] inodeBytes = new byte[Constants.INODE_SIZE];
    			for(int x = 0; x < inodeBytes.length; x++) {
    				inodeBytes[x] = block[j * Constants.INODE_SIZE + x];
    			}
    			Inode inode = new Inode(inodeBytes);
    			int fid = inode.getFileID();
    			if(fid != 0) {
    				//int inodeIndex = fid & 255;
    				int realFileID = (fid>>8)<<8;
    				if(!freeFileIDs.contains(realFileID)) {
    					if(realFileID == 0) {
    						System.out.println("Issue");
    					}
    					DFileID realID = dfiles[(realFileID>>8)-1];
    					realID.getINodeList().add(inode);
    					inode.setfileID(realID);
    				} else {
    					freeFileIDs.remove(realFileID);
    					usedFileIDs.add(realFileID);
    					DFileID FID = new DFileID(realFileID);
    					dfiles[(realFileID>>8)-1] = FID;
    				}
    			}
    		}
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
    public synchronized DFileID createDFile() {
        int fileId = freeFileIDs.poll();
        usedFileIDs.add(fileId);
        DFileID newDfile = new DFileID(fileId);
        dfiles[(newDfile.getDFileID()>>8)-1] = newDfile;
        
        Inode inode = freeINodes.poll();
        usedINodes.add(inode);
        inode.setfileID(newDfile);
        newDfile.getINodeList().add(inode);
        updateFileDes(newDfile);
        
        return newDfile;
        // associate with first iNode, make set of inodes to work with
    }

    @Override
    public synchronized void destroyDFile(DFileID dFID) {
    	this.dfiles[dFID.getDFileID()>>8-1] = null;
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
    			int intermed = (count < Constants.BLOCK_SIZE) ? count : Constants.BLOCK_SIZE;
        		_cache.getBlock(dFID.getINodeList().get(j).getBlockMap().get(i)).read(buffer, (startOffset+(i*Constants.BLOCK_SIZE)), (intermed));
        		count-=intermed;
        	}
    	}
    	return 0; // return statement?
    }

    @Override
    public synchronized int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
    	boolean expandFile = dFID.fileSize < count;
    	ArrayList<Inode> fileINodes = dFID.getINodeList();
    	/*
    	 * Following section assumes (x+n-1)/n = ceil(x/n)
    	 */
    	int num_blocks_needed = (count+Constants.BLOCK_SIZE-1)/Constants.BLOCK_SIZE;
    	int num_blocks_have   = (dFID.fileSize+Constants.BLOCK_SIZE-1)/Constants.BLOCK_SIZE;
    	int num_inodes_needed = ((count+Constants.BLOCK_SIZE-1)/Constants.BLOCK_SIZE)/(Constants.BLOCK_ADDRESSES_PER_INODE);
    	int num_inodes_have   = dFID.getINodeList().size();
    	if(expandFile) {//need another block 
    		dFID.fileSize = count;
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
    			}
    		} else if(num_blocks_needed > num_blocks_have){//just need more blocks
    			for(int i = 0; i < num_blocks_needed-num_blocks_have; i++) {
    	   			int bID = freeBlocks.poll();
            		usedBlocks.add(bID);
            		fileINodes.get(fileINodes.size()-1).add(bID);
    			}
    		}  
    		updateFileDes(dFID);
    	}
    	
    	for (int j = 0; j<fileINodes.size(); j++){
    		ArrayList<Integer> fileBlockMap = fileINodes.get(j).getBlockMap();
    		for (int i = 0; i<fileBlockMap.size(); i++){
    			int intermed = (count < Constants.BLOCK_SIZE) ? count : Constants.BLOCK_SIZE;
        		_cache.getBlock(dFID.getINodeList().get(j).getBlockMap().get(i)).write(buffer, (startOffset+(i*Constants.BLOCK_SIZE)), (intermed));
        		count-=intermed;
    		}
    		if(j < fileINodes.size()-1) {//not the last mem_block 
    			fileINodes.get(j).setInodeSize(Constants.BLOCK_ADDRESSES_PER_INODE * Constants.BLOCK_SIZE);    			
    		} else {
    			fileINodes.get(j).setInodeSize(count%Constants.BLOCK_ADDRESSES_PER_INODE * Constants.BLOCK_SIZE);
    		}
    	}
    	return 0;
    }

    @Override
    public int sizeDFile(DFileID dFID) {
    	return dFID.fileSize;
    }

    @Override
    public List<DFileID> listAllDFiles() {
        ArrayList<DFileID> dfilesAsList = new ArrayList<DFileID>(Arrays.asList(dfiles));
        ArrayList<DFileID> ret = new ArrayList<DFileID>();
        for(DFileID a : dfilesAsList) {
        	if(a!=null){
        		ret.add(a);
        	}
        }
        return ret;
    }

    @Override
    public void sync() {
    	this._cache.sync();
    }
    
    private void updateFileDes(DFileID id) {
    	DBuffer dbuffer = _cache.getBlock(id.getINodeList().get(0).getBlockID());
    	int remainingFileSize = id.fileSize;
    	for(int i = 0; i < id.getINodeList().size(); i++) {
    		byte[] buff = new byte[Constants.BLOCK_SIZE];
    		Inode node = id.getINodeList().get(i);
    		if(i > 0) {
    			dbuffer = _cache.getBlock(node.getBlockID());
    		}
    		int inodeIndexedFileID = node.getFileID()+i;
    		byte[] FID = ByteBuffer.allocate(4).putInt(inodeIndexedFileID).array();
    		for(int j = 0; j < FID.length; j++) {
    			buff[j + node.getOffset()] = FID[j];//writing out fileID with the inode index
    		}
    		node.setInodeSize((remainingFileSize < Constants.BLOCK_ADDRESSES_PER_INODE*Constants.BLOCK_SIZE) ? 
    				remainingFileSize : Constants.BLOCK_ADDRESSES_PER_INODE*Constants.BLOCK_SIZE);
    		remainingFileSize-=node.getInodeSize();
    		byte[] size = ByteBuffer.allocate(4).putInt(node.getInodeSize()).array();
    		for(int j = 0; j < size.length; j++) {
    			buff[j + Constants.BLOCK_ADDRESS_SIZE + node.getOffset()] = size[j];//writing out size in bytes
    		} 
    		ArrayList<Integer> blockMap = node.getBlockMap();
    		for(int x = 0; x < Constants.BLOCK_ADDRESSES_PER_INODE; x++) {
    			if(x < blockMap.size()) {
    				int blockAddress = blockMap.get(x);
    				byte[] byteBlockAddress = ByteBuffer.allocate(4).putInt(blockAddress).array();
    				for(int j = 0; j < byteBlockAddress.length; j++) {
    					buff[j + (x + 2) * Constants.BLOCK_ADDRESS_SIZE + node.getOffset()] = byteBlockAddress[j];
    				}
    			} else {
    				byte[] zeros = {0, 0, 0, 0};
    				for(int j = 0; j < zeros.length; j++) {
    					buff[j + (x + 2) * Constants.BLOCK_ADDRESS_SIZE + node.getOffset()] = zeros[j];
    				}
    			}
    		}
    		dbuffer.write(buff, node.getOffset(), Constants.INODE_SIZE);  
    		_cache.sync();
    		_cache.releaseBlock(dbuffer);
    	}
    	
    }   
}
