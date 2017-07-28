package club.cookbean.sparrow.operation.impl;


import club.cookbean.sparrow.operation.SingleWriteOperation;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.writer.CacheWriter;

public class WriteOperation implements SingleWriteOperation {

    private final String key;
    private final Cacheable value;

    public WriteOperation(String key, Cacheable value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public void performOperation(CacheWriter cacheWriter) throws Exception {
        cacheWriter.write(key, value);
    }

    @Override
    public String getKey() {
        return key;
    }

    public Cacheable getValue() {
        return value;
    }

    @Override
    public long getCreationTime() {
        return value.getCreationTime();
    }

    @Override
    public int hashCode() {
        int hash = (int) value.getCreationTime();
        hash = hash * 31 + getKey().hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof WriteOperation &&
                getCreationTime() == ((WriteOperation) other).getCreationTime() &&
                getKey().equals(((WriteOperation) other).getKey());
    }
}
