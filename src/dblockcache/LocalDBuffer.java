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
        return false;
    }

    @Override
    public boolean waitValid() {
        return false;
    }

    @Override
    public boolean checkClean() {
        return false;
    }

    @Override
    public boolean waitClean() {
        return false;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public int read(byte[] buffer, int startOffset, int count) {
        return 0;
    }

    @Override
    public int write(byte[] buffer, int startOffset, int count) {
        return 0;
    }
    // upcall from VirtualDisk
    @Override
    public void ioComplete() {

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
