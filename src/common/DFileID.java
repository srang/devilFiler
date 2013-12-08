package common;

import java.util.ArrayList;

/* typedef DFileID to int */
public class DFileID {

	private int _dFID;
	public ArrayList<Inode> inode;
	public int fileSize;

	public DFileID(int dFID) {
		_dFID = dFID;
		inode = new ArrayList<Inode>();
	}

	public int getDFileID() {
		return _dFID;
	}
	
	public ArrayList<Inode> getINode(){
		return this.inode;
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
