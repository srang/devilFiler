package common;

import java.util.ArrayList;

/* typedef DFileID to int */
public class DFileID {

	private int _dFID;
	private ArrayList<Inode> inodeList;
	public int fileSize;

	public DFileID(int dFID) {
		_dFID = dFID;
		inodeList = new ArrayList<Inode>();
	}

	public int getDFileID() {
		return _dFID;
	}
	
	public ArrayList<Inode> getINodeList(){
		return this.inodeList;
	}
	    
	public boolean equals(Object other){
		DFileID otherID =  (DFileID) other;
		if(otherID.getDFileID() == _dFID){
			return true;
		}
		return false;
	}
	    
	public String toString(){
		return _dFID+"";
	}
}
