package club.cookbean.sparrow.function.impl;

import club.cookbean.sparrow.function.PushFunction;

/**
 * Created by Bennett Dong <br>
 * Date : 2017/8/9 <br>
 * Mail: dongshujin.beans@gmail.com <br> <br>
 * Desc:
 */
public class MemoizingPushFunction<Key, Value> implements PushFunction<Key, Value> {

    private final PushFunction<Key, Value> function;
    private boolean handled;
    private Iterable<Value> result;

    public static <Key, Value> MemoizingPushFunction<Key, Value> memoize(PushFunction<Key, Value> function) {
        return new MemoizingPushFunction<>(function);
    }

    private MemoizingPushFunction(PushFunction<Key, Value> function) {
        this.function = function;
    }

    @Override
    public Iterable<Value> apply(Key key) {
        if (handled) {
            return result;
        }
        handled = true;
        result = function.apply(key);
        return result;
    }
}
