package club.cookbean.sparrow.config;


import club.cookbean.sparrow.builder.CacheManagerBuilder;
import club.cookbean.sparrow.cache.CacheManager;

public interface CacheManagerConfiguration<T extends CacheManager> {

    CacheManagerBuilder<T> builder(CacheManagerBuilder<? extends CacheManager> other);
}
