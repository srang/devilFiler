package dblockcache;

import java.util.LinkedHashMap;
import java.util.Map;

// stores a list of DBuffers in-memory
public class LocalDBufferCache extends DBufferCache{

    private LinkedHashMap<Integer, CacheEntry> buffers;
    public LocalDBufferCache(final int cacheSize) {// cache size is the number of DBuffers or blocks
        super(cacheSize);
        buffers = new LinkedHashMap<Integer, CacheEntry>(cacheSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, CacheEntry> eldest) {
                // When to remove the eldest buffer
                // extra eviction logic about being pinned or held should go here

            		return size() > cacheSize; // Size exceeded the max allowed.
            }
        };
    }

    @Override
    public DBuffer getBlock(int blockID) {
        if (buffers.containsKey(blockID)) {
            CacheEntry entry = buffers.get(blockID);
            entry.buffer.busy = true; // held until released
            return entry.buffer;
        }
        buffers.put(blockID,new CacheEntry(new LocalDBuffer()));
        buffers.get(blockID).buffer.busy = true; // held until released
        return buffers.get(blockID).buffer;
    }
    // this may no longer need to be explicitly called.
    @Override
    public void releaseBlock(DBuffer buf) {
    	if (this.buffers.containsValue(buf))
    		buf.busy = false;
    		this.notifyAll();
    	// does a signal go here?
    }

    @Override
    public void sync() {
        // write dirty data to disk
    	for (int i = 0; i<this.buffers.size(); i++){
    		if(!this.buffers.get(this.buffers.keySet().toArray()[i]).buffer.isClean){
    			this.buffers.get(this.buffers.keySet().toArray()[i]).buffer.startPush();
    			this.buffers.get(this.buffers.keySet().toArray()[i]).buffer.waitClean(); // is this wait appropriate here?
    			// some kind of signal here
    		}
    	}
    }

    private static class CacheEntry {
        private LocalDBuffer buffer;

        private CacheEntry(LocalDBuffer buffer) {
            this.buffer = buffer;
        }
    }
}
