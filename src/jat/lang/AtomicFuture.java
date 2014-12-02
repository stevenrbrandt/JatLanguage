package jat.lang;

/**
 * AtomicFutures can only be set inside
 * transactions, and can be
 * read outside them. They preserve
 * the value last seen inside a
 * transaction.
 */
public class AtomicFuture<T> {
    private T data;
    private boolean isset = false;
    public void set(final T v,final Transaction t) {
        if(isset) throw new FutureAlreadySetException();
        data = v;
        isset = true;
    }
    public T get(Transaction t) {
        return data;
    }
    public T get() {
        return data;
    }
}
