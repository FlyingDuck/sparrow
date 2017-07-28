package club.cookbean.sparrow.util;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.ServiceLoader;

public class ClassLoading {

  private static final ClassLoader DEFAULT_CLASSLOADER;

  static {
    DEFAULT_CLASSLOADER = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
      @Override
      public ClassLoader run() {
        return new DefaultClassLoader();
      }
    });
  }

  public static ClassLoader getDefaultClassLoader() {
    return DEFAULT_CLASSLOADER;
  }

  public static <T> ServiceLoader<T> libraryServiceLoaderFor(Class<T> serviceType) {
    return ServiceLoader.load(serviceType, ClassLoading.class.getClassLoader());
  }

  private static class DefaultClassLoader extends ClassLoader {
    private static final ClassLoader THIS_LOADER = DefaultClassLoader.class.getClassLoader();

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();

      if (loader != null) {
        try {
          return loader.loadClass(name);
        } catch (ClassNotFoundException cnfe) {
          //
        }
      }

      return THIS_LOADER.loadClass(name);
    }

    @Override
    public URL getResource(String name) {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();

      if (loader != null) {
        URL res = loader.getResource(name);
        if (res != null) {
          return res;
        }
      }

      return THIS_LOADER.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();

      if (loader != null) {
        Enumeration<URL> resources = loader.getResources(name);
        if (resources != null && resources.hasMoreElements()) {
          return resources;
        }
      }

      return THIS_LOADER.getResources(name);
    }
  }
}
