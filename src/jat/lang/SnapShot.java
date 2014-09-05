package jat.lang;

/**
 * Snapshots can only be set inside
 * transactions, and can be
 * read outside them. They preserve
 * the value last seen inside a
 * transaction.
 */
public class SnapShot<T> {
    private T data;
    public void set(final T v,final Transaction t) {
        data = v;
    }
    public T get(Transaction t) {
        return data;
    }
    public T get() {
        return data;
    }
}
