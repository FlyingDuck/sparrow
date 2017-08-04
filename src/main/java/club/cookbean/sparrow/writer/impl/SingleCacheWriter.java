package club.cookbean.sparrow.writer.impl;

import club.cookbean.sparrow.exception.BulkCacheWritingException;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.writer.CacheWriter;

import java.util.Map;

/**
 * Created by Bennett Dong <br>
 * Date : 2017/8/4 <br>
 * Mail: dongshujin.beans@gmail.com <br> <br>
 * Desc:
 */
public abstract class SingleCacheWriter implements CacheWriter {


    @Override
    public void writeAll(Iterable<? extends Map.Entry<String, Cacheable>> entries) throws BulkCacheWritingException, Exception {
        throw new UnsupportedOperationException("SingleCacheWriter cannot apply write all operation");
    }

    @Override
    public void delete(String key) throws Exception {
        throw new UnsupportedOperationException("SingleCacheWriter cannot apply delete operation");
    }

    @Override
    public void deleteAll(Iterable<String> keys) throws BulkCacheWritingException, Exception {
        throw new UnsupportedOperationException("SingleCacheWriter cannot apply delete all operation");

    }
}
