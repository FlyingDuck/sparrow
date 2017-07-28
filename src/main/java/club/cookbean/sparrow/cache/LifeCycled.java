package club.cookbean.sparrow.cache;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-8. <br><br>
 * Desc:
 * Internal interface to register hooks with the life cycle of {@link com.xiaomi.miui.global.redis.Cache} or
 * {@link org.ehcache.CacheManager} instances.
 */
public interface LifeCycled {

    /**
     * Callback used by internal life cycling infrastructure when transitioning from
     * {@link com.xiaomi.miui.global.redis.Status#UNINITIALIZED} to {@link com.xiaomi.miui.global.redis.Status#AVAILABLE}
     * <br />
     * Throwing an Exception here, will fail the transition
     *
     * @throws Exception to veto transition
     */
    void init() throws Exception;

    /**
     * Callback used by internal life cycling infrastructure when transitioning from
     * {@link com.xiaomi.miui.global.redis.Status#AVAILABLE} to {@link com.xiaomi.miui.global.redis.Status#UNINITIALIZED}
     * <br />
     * Throwing an Exception here, will fail the transition
     *
     * @throws Exception to veto transition
     */
    void close() throws Exception;
}

