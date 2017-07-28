package club.cookbean.sparrow.cache;


/**
 * Enumeration of {@link CacheManager} and {@link ManagedCache} statuses.
 * <p>
 * Instances are allowed the following {@code Status} transitions:
 * <dl>
 *   <dt>{@link #UNINITIALIZED} to {@link #AVAILABLE}</dt>
 *   <dd>In case of transition failure, it will remain {@code UNINITIALIZED}</dd>
 *   <dt>{@link #AVAILABLE} to {@link #UNINITIALIZED}</dt>
 *   <dd>In case of transition failure, it still ends up {@code UNINITIALIZED}</dd>
 *   <dt>{@link #UNINITIALIZED} to {@link #MAINTENANCE}</dt>
 *   <dd>In case of transition failure, it will remain {@code UNINITIALIZED}</dd>
 *   <dt>{@link #MAINTENANCE} to {@link #UNINITIALIZED}</dt>
 *   <dd>In case of transition failure, it still ends up {@code UNINITIALIZED}</dd>
 * </dl>
 * As such the {@code UNINITIALIZED} state is the fallback state.
 */
public enum Status {

    /**
     * Uninitialized, indicates it is not ready for use.
     */
    UNINITIALIZED,

    /**
     * Maintenance, indicates exclusive access to allow for restricted operations.
     */
    MAINTENANCE,

    /**
     * Available, indicates it is ready for use.
     */
    AVAILABLE,;


}