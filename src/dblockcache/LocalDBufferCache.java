package dblockcache;

import java.util.*;

// stores a list of DBuffers in-memory
public class LocalDBufferCache extends DBufferCache {

    private Map<Integer, DBuffer> buffers;
    private Queue<Integer> lru;
    private int cacheSize;

    public LocalDBufferCache(final int cacheSize) {// cache size is the number of DBuffers or blocks
        super(cacheSize);
        buffers = new HashMap<Integer, DBuffer>();
        lru = new LinkedList<Integer>();
        this.cacheSize = cacheSize;
    }

    @Override
    public synchronized DBuffer getBlock(int blockID) {
        if (buffers.containsKey(blockID)) {
            DBuffer entry = buffers.get(blockID);
            entry.state = DBuffer.BufferState.HELD; // held until released
            // existing blocks should be moved to the back of the queue
            moveToBack(blockID);
            return entry;
        }
        LocalDBuffer buff = new LocalDBuffer();
        buff.isValid = false; // this should be initialized in constructor
        buff.state = DBuffer.BufferState.HELD; // held until released
        if (buffers.size() == cacheSize){
            evict();
        }

        buffers.put(blockID, buff);
        lru.add(blockID); // new blocks have been most recently access

        return buff;
    }
    // evict the least recently used block this is not busy
    private void evict() {
        Iterator<Integer> iterator = lru.iterator();
        while (iterator.hasNext()) {
            int current = iterator.next();
            if(!buffers.get(current).isBusy()){
                iterator.remove();
                buffers.remove(current);
                return;
            }
        }
    }

    private void moveToBack(int blockID) {
        if (lru.contains(blockID)) {
            lru.remove(blockID);
        }
        lru.add(blockID);
    }

    @Override
    public synchronized void releaseBlock(DBuffer buf) {
        if (this.buffers.containsValue(buf)) {
            buf.state = DBuffer.BufferState.FREE;
            this.notifyAll();
        }
        // does a signal go here?
    }

    @Override
    public synchronized void sync() {
        // write dirty data to disk
        for (int i = 0; i < this.buffers.size(); i++) {
            if (!this.buffers.get(this.buffers.keySet().toArray()[i]).isClean) {
                this.buffers.get(this.buffers.keySet().toArray()[i]).startPush();
                this.buffers.get(this.buffers.keySet().toArray()[i]).waitClean(); // is this wait appropriate here?
                // some kind of signal here
            }
        }
    }

}
