package org.getopt.luke.plugins;

import java.io.EOFException;
import java.io.IOException;
import java.util.Collection;

import org.apache.hadoop.fs.Path;
import org.apache.lucene.store.BufferedIndexInput;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteFSDirectory extends Directory {
    private static int bufferSize;

    private String getFilePath(String name) {
        return directory + "/" + name; // TODO
    }

    public RemoteFSDirectory(RemoteFileReader fileReader, String directory) {
        this.fileReader = fileReader;
        this.directory = directory;
    }

    private static final Logger LOG = LoggerFactory.getLogger(RemoteFSDirectory.class);

    private RemoteFileReader fileReader;
    private String directory;

    @Override
    public String[] listAll() throws IOException {
        return fileReader.listAllFiles(directory);
    }

    @Override
    public void deleteFile(String name) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long fileLength(String name) throws IOException {
        return fileReader.fileLength(getFilePath(name));
    }

    @Override
    public IndexOutput createOutput(String name, IOContext context)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public IndexOutput createTempOutput(String s, String s1, IOContext ioContext) throws IOException {
        return null;
    }

    @Override
    public void sync(Collection<String> names) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rename(String s, String s1) throws IOException {

    }

    @Override
    public void syncMetaData() throws IOException {

    }

    @Override
    public IndexInput openInput(String name, IOContext context)
            throws IOException {
        RemoteFile file = new RemoteFile(fileReader, getFilePath(name));
        return new RemoteFSIndexInput(name, file, context);
    }

    @Override
    public Lock obtainLock(String s) throws IOException {
        return null;
    }

    public Lock makeLock(final String name) {
        return new Lock() {
            public boolean obtain() {
                return true;
            }
            public void release() {
            }
            public boolean isLocked() {
                return false;
            }
            public String toString() {
                return "Lock@" + new Path(directory, name);
            }

            public void close() throws IOException {
            }

            @Override
            public void ensureValid() throws IOException {
            }

        };
    }

    @Override
    public void close() throws IOException {
        // TODO: close rmi connection?
    }

    static final class RemoteFSIndexInput extends BufferedIndexInput {
        protected final RemoteFile file;
        /** is this instance a clone and hence does not own the file to close it */
        boolean isClone = false;
        /** start offset: non-zero in the slice case */
        protected final long off;
        /** end offset (start+length) */
        protected final long end;

        public RemoteFSIndexInput(String resourceDesc, RemoteFile file, IOContext context) throws IOException {
            super(resourceDesc, context);
            this.file = file;
            this.off = 0L;
            this.end = file.length();
            setBufferSize(bufferSize);
        }

        public RemoteFSIndexInput(String resourceDesc, RemoteFile file, long off, long length, int bufferSize) {
            super(resourceDesc, bufferSize);
            this.file = file;
            this.off = off;
            this.end = off + length;
            this.isClone = true;
        }

        @Override
        protected void readInternal(byte[] b, int offset, int len)
                throws IOException {
            long position = off + getFilePointer();
            if (position + len > end) {
                throw new EOFException("read past EOF: " + this);
            }
            System.out.println(String.format("Reading %d bytes from %s position=%d", len, file.path, position));
            byte[] buf;
            if (file.isInitialized()) {
                buf = file.read(position, len);
                System.arraycopy(buf, 0, b, offset, len);
            } else {
                buf = file.read(position, len);
                System.arraycopy(buf, RemoteFileReaderImpl.INTEGER_SIZE, b, offset, len);
            }
        }

        @Override
        protected void seekInternal(long pos) throws IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void close() throws IOException {
            if (!isClone) {
                file.close();
            }
        }

        @Override
        public RemoteFSIndexInput clone() {
            RemoteFSIndexInput clone = (RemoteFSIndexInput) super.clone();
            clone.isClone = true;
            return clone;
        }

        //@Override
        // TODO: remove for 4.0.0 and lower?
        public IndexInput slice(String sliceDescription, long offset, long length) throws IOException {
            if (offset < 0 || length < 0 || offset + length > this.length()) {
                throw new IllegalArgumentException("slice() " + sliceDescription + " out of bounds: "  + this);
            }
            return new RemoteFSIndexInput(sliceDescription, file, off + offset, length, getBufferSize());
        }

        @Override
        public long length() {
            return end - off;
        }

    }

    public static final void setBufferSize(int newSize) {
        bufferSize = newSize;
    }

}