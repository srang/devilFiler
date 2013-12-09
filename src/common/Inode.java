package common;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Inode {
	private int blockID;
	private int offset;
	private DFileID fileID;
	private ArrayList<Integer> blockMap;
	private int inodeSize;//Size of space on disk referenced by inode
	
	public Inode() {
		blockMap = new ArrayList<Integer>();
		inodeSize = 0;
	}
	
	public Inode (byte[] b) {
	    // first 3 bytes are the dFileID
//        ByteBuffer byteBuffer = ByteBuffer.wrap(b, 0, 4);
        ByteBuffer byteBuffer = ByteBuffer.wrap(b);
        this.fileID = new DFileID(byteBuffer.getInt(0));
        // 4th byte is the order in the file
        // next 4 bytes are inode size
//        this.inodeSize = ByteBuffer.wrap(b, 4, 4).getInt();
        this.inodeSize = byteBuffer.getInt(4);

        // remaining bytes are block addresses
        blockMap = new ArrayList<Integer>();
        for(int i = 8; i < inodeSize*4 + 8; i += 4) {
            blockMap.add(byteBuffer.getInt(i));
        }
    }
	
	// set methods
	public void setBlockID(int block){
		this.blockID = block;
	}
	
	public void setOffset(int off){
		this.offset = off;
	}
	
	public void setfileID(DFileID ID){
		this.fileID = ID;
	}
	
	public void setInodeSize(int size){
		inodeSize = size;
	}
	
	public void add(int fileBlock){
		this.blockMap.add(fileBlock);
	}
	
	// get methods
	public int getInodeSize(){
		return inodeSize;
	}
	
	public int getBlockID(){
		return this.blockID;
	}
	
	public int getOffset(){
		return this.offset;
	}
	
	public int getFileID(){
		return this.fileID.getDFileID();
	}
	
	public ArrayList<Integer> getBlockMap(){
		return this.blockMap;
	}
}
