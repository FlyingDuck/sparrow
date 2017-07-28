package club.cookbean.sparrow.operation.impl;


import club.cookbean.sparrow.operation.SingleWriteOperation;
import club.cookbean.sparrow.writer.CacheWriter;

public class DeleteOperation implements SingleWriteOperation {
    private final String key;
    private final long creationTime;

    public DeleteOperation(String key) {
        this(key, System.currentTimeMillis());
    }

    public DeleteOperation(String key, long creationTime) {
        this.key = key;
        this.creationTime = creationTime;
    }

    @Override
    public void performOperation(CacheWriter cacheWriter) throws Exception {
        cacheWriter.delete(key);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DeleteOperation &&
                getCreationTime() == ((DeleteOperation) other).getCreationTime() &&
                getKey().equals(((DeleteOperation) other).getKey());
    }

}
