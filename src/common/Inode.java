package common;

import java.util.ArrayList;

public class Inode {
	private int blockID;
	private int offset;
	private DFileID fileID;
	private ArrayList<Integer> blockMap;
	private int fileSize;
	
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
	
	public void setFileSize(int size){
		this.fileSize = size;
	}
	
	public void add(int fileBlock){
		this.blockMap.add(fileBlock);
	}
	
	// get methods
	public int Size(){
		return Constants.INODE_SIZE;
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
