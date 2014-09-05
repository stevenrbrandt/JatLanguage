package jat.lang;

public class Atom<T> {
    T data;
    ThreadLocal<T> tdata = new ThreadLocal<T>();

    public Atom() {}

    public Atom(T t) {
        data = t;
    }

    /**
     * Get a variable within a transaction.
     */
    public T get(Transaction tr) {
        tr.set.add(this);
        return data;
    }

    /**
     * Set a variable within a transaction.
     **/
    public void set(T t,Transaction tr) {
        data = t;
        tr.set.add(this);
    }
}
