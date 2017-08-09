package club.cookbean.sparrow.function.impl;

import club.cookbean.sparrow.function.RangeFunction;

import java.util.List;

/**
 * Created by Bennett Dong <br>
 * Date : 2017/8/7 <br>
 * Mail: dongshujin.beans@gmail.com <br> <br>
 * Desc:
 */
public class MemoizingRangeFunction<Key, Start, End, Value> implements RangeFunction<Key, Start, End, Value> {

    private final RangeFunction<Key, Start, End, Value> function;
    private boolean handled;
    private List<Value> values;

    public static <Key, Start, End, Value> MemoizingRangeFunction<Key, Start, End, Value> memoize(RangeFunction<Key, Start, End, Value> function) {
        return new MemoizingRangeFunction<>(function);
    }

    private MemoizingRangeFunction(RangeFunction<Key, Start, End, Value> function) {
        this.function = function;
    }

    @Override
    public List<Value> apply(Key key, Start start, End end) {
        if (handled) {
            return values;
        }
        handled = true;
        values = this.function.apply(key, start, end);
        return values;
    }
}
