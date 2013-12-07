package common;

/* typedef DFileID to int */
public class DFileID {

	private int _dFID;
	private Inode _inode;

	public DFileID(int dFID) {
		_dFID = dFID;
		_inode = new Inode();
	}

	public int getDFileID() {
		return _dFID;
	}
	
	public Inode getINode(){
		return _inode;
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
