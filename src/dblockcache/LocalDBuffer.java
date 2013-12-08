package dblockcache;

import common.Constants;
import common.Constants.DiskOperationType;
import virtualdisk.VirtualDisk;

import java.io.IOException;

public class LocalDBuffer extends DBuffer {
    // a DBuffer is an abstraction over a byte array and block id
    private VirtualDisk disk;
    private int blockID;
    private byte[] _buffer;

    public LocalDBuffer(VirtualDisk disk) {
        _buffer = new byte[Constants.BLOCK_SIZE];
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
    public boolean checkValid() {
        return this.isValid;
    }

    @Override
    public boolean waitValid() {
        while (!this.isValid) {
            try {
                this.wait();
            } catch (InterruptedException e) {
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
        while (!this.isClean) {
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
        return (this.state != DBuffer.BufferState.FREE);
    }

    @Override
    public int read(byte[] buffer, int startOffset, int count) {
        if (!isValid) {
            startFetch();
            waitValid();
        }
        isValid = true;
        for (int i = 0; i < count; i++) {
            buffer[startOffset + i] = _buffer[startOffset + i];
        }
        this.notifyAll();
        return 0;
    }

    @Override
    public int write(byte[] buffer, int startOffset, int count) {
        byte[] hold = new byte[_buffer.length + count];
        for (int j = 0; j < _buffer.length; j++) {
            hold[j] = _buffer[j];
        }
        _buffer = hold;// add on and expand buffer if subsequent write
        for (int i = 0; i < count; i++) {
            _buffer[startOffset + i] = buffer[startOffset + i];
        }
        this.isClean = false; // mark as dirty
        this.isValid = true;
        //notify?
        return 0;
    }

    // upcall from VirtualDisk
    @Override
    public void ioComplete() {
        state = DBuffer.BufferState.HELD;
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
