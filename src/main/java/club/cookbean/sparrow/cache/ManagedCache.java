package club.cookbean.sparrow.cache;


import club.cookbean.sparrow.exception.StateTransitionException;

import java.io.Closeable;


public interface ManagedCache extends Cache, Closeable{

    void init() throws StateTransitionException;

    @Override
    void close() throws StateTransitionException;

    /**
     * Returns the current {@link Status} of this {@code ManagedCache}.
     *
     * @return the current {@code Status}
     */
    Status getStatus();
}
