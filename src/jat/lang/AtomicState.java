package jat.lang;

public class AtomicState<T> implements Comparable<AtomicState<T>> {
  AType atype;
  T data;
  T origData;
  Atom<T> atom;
  public AtomicState(T data,AType atype,Atom<T> atom) {
    this.data = this.origData = data;
    this.atype = atype;
    this.atom = atom;
  }
  public int compareTo(AtomicState<T> that) {
    int a = System.identityHashCode(this);
    int b = System.identityHashCode(that);
    if(a < b) return 1;
    else if(a > b) return -1;
    else return 0;
  }
  public void set() {
    atom.data = data;
  }
}
