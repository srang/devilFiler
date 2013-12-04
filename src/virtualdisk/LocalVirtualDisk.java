package virtualdisk;

import common.Constants;
import dblockcache.DBuffer;

import java.io.FileNotFoundException;
import java.io.IOException;

public class LocalVirtualDisk extends VirtualDisk {

    public LocalVirtualDisk(String volName, boolean format) throws FileNotFoundException, IOException {
        super(volName, format);
    }

    public LocalVirtualDisk(boolean format) throws FileNotFoundException, IOException {
        super(format);
    }

    public LocalVirtualDisk() throws FileNotFoundException, IOException {
        super();
    }

    @Override
    public void startRequest(DBuffer buf, Constants.DiskOperationType operation) throws IllegalArgumentException, IOException {
        // we pass in the DBuffer, and the requested operation
    }
}
