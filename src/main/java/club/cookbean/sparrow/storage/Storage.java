package club.cookbean.sparrow.storage;


import club.cookbean.sparrow.annotation.PluralService;
import club.cookbean.sparrow.exception.StorageAccessException;
import club.cookbean.sparrow.function.SingleFunction;
import club.cookbean.sparrow.redis.Cacheable;
import club.cookbean.sparrow.redis.RedisConnector;
import club.cookbean.sparrow.redis.RedisResource;
import club.cookbean.sparrow.service.Service;

public interface Storage extends ConfigurationChangeSupport {

    void release();

    String get(String key) throws StorageAccessException;

    void set(String key, Cacheable value) throws StorageAccessException;

    void handleWriteSingle(String key, SingleFunction<String, Cacheable> setFunction) throws StorageAccessException;

    String handleLoadSingle(String key, SingleFunction<String, Cacheable> getFunction) throws StorageAccessException;

    // TODO ... more functions

    String normalizeKey(String key);

    @PluralService
    interface Provider extends Service {
        // TODO
        Storage createStorage(Configuration storageConfig);

        void releaseStorage(Storage storage);


        void initStorage(Storage storage);

        boolean choose(RedisResource.ResourceType type);
    }

    interface Configuration {

        /**
         * The concurrency level of the dispatcher that processes events
         */
        int getDispatcherConcurrency();

        ClassLoader getClassLoader();

        RedisResource getResource();

        RedisConnector getConnector();

        /**
         * The serializer for key instances

        Serializer getKeySerializer();*/

        /**
         * The serializer for value instances

        Serializer getValueSerializer();*/
    }
}
