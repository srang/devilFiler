package virtualdisk;

import common.Constants;
import dblockcache.DBuffer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class LocalVirtualDisk extends VirtualDisk {
    private Queue<IORequest> requestQueue;
    public LocalVirtualDisk(String volName, boolean format) throws FileNotFoundException, IOException {
        super(volName, format);
        requestQueue = new LinkedList<IORequest>();
    }

    public LocalVirtualDisk(boolean format) throws FileNotFoundException, IOException {
        super(format);
        requestQueue = new LinkedList<IORequest>();
    }

    public LocalVirtualDisk() throws FileNotFoundException, IOException {
        super();
        requestQueue = new LinkedList<IORequest>();
    }

    @Override
    public void startRequest(DBuffer buf, Constants.DiskOperationType operation) throws IllegalArgumentException, IOException {
        // add request to our queue
        requestQueue.add(new IORequest(buf, operation));
    }
    private void processRequests() throws IOException {
        while(!requestQueue.isEmpty()) {
            IORequest request = requestQueue.poll();
            DBuffer buf = request.buffer;
            // we pass in the DBuffer, and the requested operation
            if(request.operation == Constants.DiskOperationType.READ) {
                this.readBlock(buf);
                buf.isValid = true;
            } else if (request.operation == Constants.DiskOperationType.WRITE) {
                this.writeBlock(buf);
                buf.isClean = true;
            }
            buf.ioComplete();
        }
    }
    private class IORequest {
        private DBuffer buffer;
        private Constants.DiskOperationType operation;

        private IORequest(DBuffer buffer, Constants.DiskOperationType operation) {
            this.buffer = buffer;
            this.operation = operation;
        }
    }
}
