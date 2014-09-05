package jat.lang;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class ValueArray<T> implements Value {
    public final int length;
    Object[] data;
    public ValueArray(T[] init) {
        length = init.length;
        data = new Object[init.length];
        for(int i=0;i<init.length;i++)
            data[i] = init[i];
    }
    public ValueArray(List<T> list) {
        length = list.size();
        data = new Object[list.size()];
        for(int i=0;i<data.length;i++)
            data[i] = list.get(i);
    }
    public ValueArray(Iterator<T> iter) {
        List<T> list = new ArrayList<T>();
        while(iter.hasNext())
            list.add(iter.next());
        length = list.size();
        data = new Object[list.size()];
        for(int i=0;i<data.length;i++)
            data[i] = list.get(i);
    }
    public ValueArray(int n,ArrayInit<T> ai) {
        length = n;
        data = new Object[n];
        for(int i=0;i<n;i++)
            data[i] = ai.call(i);
    }
    @SuppressWarnings("unchecked")
    public T get(int n) {
        return (T)data[n];
    }
    public int size() {
        return length;
    }
}
