package jat.lang;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

public class Transaction {

  Set<AtomicState<?>> states = new TreeSet<AtomicState<?>>();

  public <T> void add(AtomicState<T> t) {
    states.add(t);
  }
  public void finish() {
    boolean succeed = true;
    List<AtomicState<?>> list = new ArrayList<AtomicState<?>>();
    for(AtomicState<?> astate : states) {
      astate.atom.lock.lock();
      list.add(astate);
      if(astate.atype == AType.READ && !Util.equals(astate.origData,astate.atom.data)) {
        succeed = false;
        break;
      }
    }
    if(succeed) {
      for(AtomicState<?> astate : states) {
        astate.set();
      }
    }
    for(AtomicState<?> astate : states) {
      astate.atom.lock.unlock();
    }
    if(!succeed) {
      throw new TransactionRestart();
    }
    for(Runnable r : tasks) {
      r.run();
    }
  }

  List<Runnable> tasks = new ArrayList<Runnable>();
  public void addTask(Runnable r) {
    tasks.add(r);
  }
}
