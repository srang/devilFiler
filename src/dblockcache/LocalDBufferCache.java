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
            return entry.buffer;
        }
        buffers.put(blockID,new CacheEntry(new LocalDBuffer()));
        return buffers.get(blockID).buffer;
    }
    // this may no longer need to be explicitly called.
    @Override
    public void releaseBlock(DBuffer buf) {

    }

    @Override
    public void sync() {
        // write dirty data to disk
    }

    private static class CacheEntry {
        private boolean isDirty;
        private DBuffer buffer;

        private CacheEntry(DBuffer buffer) {
            this.buffer = buffer;
            this.isDirty = false;
        }
    }
}
