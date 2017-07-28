package club.cookbean.sparrow.clz;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Bennett Dong <br>
 * E-Mail: dongshujin@xiaomi.com <br>
 * Date: 17-7-20. <br><br>
 * Desc:
 */
public class ClassInstanceConfiguration<T> {

    private final T instance;

    private final Class<? extends T> clazz;
    private final List<Object> arguments;

    public ClassInstanceConfiguration(Class<? extends T> clazz, Object... arguments) {
        this.clazz = clazz;
        this.arguments = Arrays.asList(arguments);
        this.instance = null;
    }

    public ClassInstanceConfiguration(T instance) {
        this.instance = instance;
        @SuppressWarnings("unchecked")
        Class<? extends T> instanceClass = (Class<? extends T>) instance.getClass();
        this.clazz = instanceClass;
        this.arguments = null;
    }

    public Class<? extends T> getClazz() {
        return clazz;
    }

    public Object[] getArguments() {
        return arguments.toArray();
    }

    public T getInstance() {
        return instance;
    }
}
