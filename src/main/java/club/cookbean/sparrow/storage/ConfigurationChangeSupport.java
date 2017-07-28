package club.cookbean.sparrow.storage;


import club.cookbean.sparrow.listener.CacheConfigurationChangeListener;

import java.util.List;

public interface ConfigurationChangeSupport {

    /**
     * Returns the {@link List} of {@link CacheConfigurationChangeListener} defined.
     *
     * @return a list of {@code CacheConfigurationChangeListener}
     */
    List<CacheConfigurationChangeListener> getConfigurationChangeListeners();
}
