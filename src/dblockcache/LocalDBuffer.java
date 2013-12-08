package dblockcache;

import java.io.IOException;

import common.Constants;
import common.Constants.DiskOperationType;
import virtualdisk.LocalVirtualDisk;

public class LocalDBuffer extends DBuffer {
    // a DBuffer is an abstraction over a byte array and block id
	private LocalVirtualDisk disk;
	private int blockID;
	private byte[] buffer;
	
	@Override
    public void startFetch() {
    	try {
			disk.startRequest(this, DiskOperationType.READ);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Override
    public void startPush() {
    	try {
			disk.startRequest(this, DiskOperationType.WRITE);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Override
    public boolean checkValid() {
        return this.isValid;
    }

    @Override
    public boolean waitValid() {
    	while (!this.isValid){
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
        return this.checkValid();
    }

    @Override
    public boolean checkClean() {
        return this.isClean;
    }

    @Override
    public boolean waitClean() {
    	while (!this.isClean){
    			try {
					this.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	}
        return this.checkClean();
    }

    @Override
    public boolean isBusy() {
    	return this.busy;
    }

    @Override
    public int read(byte[] buffer, int startOffset, int count) {
    	this.busy = true; // pinned until ioComplete
    	//if (!this.checkValid())
    	// this.waitValid()
    	//	read
    	return 0;
    }

    @Override
    public int write(byte[] buffer, int startOffset, int count) {
        this.busy = true; // pinned until ioComplete
        for(int i = 0; i<count; i++) {
        	buffer[startOffset+i] = buffer[startOffset+i];
        }
        try {
			disk.startRequest(this, Constants.DiskOperationType.WRITE);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        this.isClean = false; // mark as dirty
    	return 0;
    }
    // upcall from VirtualDisk
    @Override
    public void ioComplete() {
    	this.busy = false;
    	this.notifyAll();
    }

    @Override
    public int getBlockID() {
        return this.blockID;
    }

    @Override
    public byte[] getBuffer() {
        return this.buffer;
    }
}
