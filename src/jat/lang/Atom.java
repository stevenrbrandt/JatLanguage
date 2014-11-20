package jat.lang;

import java.util.concurrent.locks.ReentrantLock;

public class Atom<T> implements Comparable<T> {
    T data;

    final ThreadLocal<AtomicState<T>> state = new ThreadLocal<AtomicState<T>>();
    final ReentrantLock lock = new ReentrantLock();

    public Atom() {}

    public Atom(T t) {
        data = t;
    }

    /**
     * Get a variable within a transaction.
     */
    public T get(Transaction tr) {
      AtomicState<T> astate = state.get();
      if(astate == null) {
        astate = new AtomicState<T>(data,AType.READ,this);
        state.set(astate);
        tr.add(astate);
      }
      return astate.data;
    }

    /**
     * Set a variable within a transaction.
     **/
    public void set(T t,Transaction tr) {
      AtomicState<T> astate = state.get();
      if(astate == null) {
        astate = new AtomicState<T>(data,AType.WRITE,this);
        state.set(astate);
        tr.add(astate);
      }
      astate.data = t;
    }

    public int compareTo(T t) {
      int a = System.identityHashCode(this);
      int b = System.identityHashCode(t);
      if(a > b) return 1;
      else if(a < b) return -1;
      else return 0;
    }
}
