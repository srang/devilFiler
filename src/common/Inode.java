package common;

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
		//Make inode from byte[]
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
