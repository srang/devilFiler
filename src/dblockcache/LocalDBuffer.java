package dblockcache;

public class LocalDBuffer extends DBuffer {
    // a DBuffer is an abstraction over a byte array and block id
    @Override
    public void startFetch() {

    }

    @Override
    public void startPush() {

    }

    @Override
    public boolean checkValid() {
        return this.isValid;
    }

    @Override
    public boolean waitValid() {
    	// while (!this.isValid)
    	//		this.wait()
        return false;
    }

    @Override
    public boolean checkClean() {
        return this.isClean;
    }

    @Override
    public boolean waitClean() {
    	// while (!this.isClean)
    	//		this.wait()
        return false;
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
        // while(writing)
        //		this.isValid = false;
        this.isClean = false; // mark as dirty
    	return 0;
    }
    // upcall from VirtualDisk
    @Override
    public void ioComplete() {
    	this.busy = false;
    }

    @Override
    public int getBlockID() {
        return 0;
    }

    @Override
    public byte[] getBuffer() {
        return new byte[0];
    }
}
