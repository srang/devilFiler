package dblockcache;

import java.io.IOException;

import common.Constants;
import common.Constants.DiskOperationType;
import virtualdisk.VirtualDisk;

public class LocalDBuffer extends DBuffer {
    // a DBuffer is an abstraction over a byte array and block id
	private VirtualDisk disk;
	private int blockID;
	private byte[] _buffer;
	
	public LocalDBuffer(int blockID, VirtualDisk disk) {
		_buffer = new byte[Constants.BLOCK_SIZE];
        this.blockID = blockID;
	    this.disk = disk;
	}
	
	@Override
    public void startFetch() {
    	try {
    		state = DBuffer.BufferState.PINNED;
			disk.startRequest(this, DiskOperationType.READ);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    @Override
    public void startPush() {
    	try {
    		state = DBuffer.BufferState.PINNED;
			disk.startRequest(this, DiskOperationType.WRITE);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    @Override
    public synchronized boolean checkValid() {
        return this.isValid;
    }

    @Override
    public synchronized boolean waitValid() {
		//this.isValid = true;
    	while (!this.isValid){
    		try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
        return this.checkValid();
    }

    @Override
    public synchronized boolean checkClean() {
        return this.isClean;
    }

    @Override
    public synchronized boolean waitClean() {
    	while (!this.isClean){
    			try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    	}
        return this.checkClean();
    }

    @Override
    public boolean isBusy() {
    	return (this.state != BufferState.FREE);
    }

    @Override
    public synchronized int read(byte[] buffer, int startOffset, int count) {
    	if(!isValid){
    		startFetch();
    		waitValid();
    	}
    	isValid = true;
    	state = BufferState.PINNED;
    	for(int i = 0; i < count; i++) {
    		int y = startOffset%Constants.BLOCK_SIZE + i;
    		/*
    		 * Start offset for the local buffer will be smaller than the passed in buffer
    		 */
    		buffer[startOffset + i] = _buffer[startOffset%Constants.BLOCK_SIZE + i]; 
    	}
    	state = BufferState.HELD;
    	this.notifyAll();
    	return 0;
    }

    @Override
    public synchronized int write(byte[] buffer, int startOffset, int count) {
    	this.isClean = false; // mark as dirty
    	state = BufferState.PINNED;
    	for(int i = 0; i < count; i++) {
    		_buffer[startOffset%Constants.BLOCK_SIZE + i] = buffer[startOffset + i];
    	}
        state = BufferState.HELD;
        this.notifyAll();
        return 0;
    }
    // upcall from VirtualDisk
    @Override
    public synchronized void ioComplete() {
    	state = BufferState.HELD;
    	this.notifyAll();
    }

    @Override
    public int getBlockID() {
        return this.blockID;
    }

    @Override
    public byte[] getBuffer() {
        return this._buffer;
    }
}
