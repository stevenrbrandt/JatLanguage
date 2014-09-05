package jat.lang;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class Transaction {
    public Set<Atom<?>> set = new HashSet<Atom<?>>();
    public void finish() {
        for(Runnable r : tasks) {
            r.run();
        }
    }

    List<Runnable> tasks = new ArrayList<Runnable>();
    public void addTask(Runnable r) {
        tasks.add(r);
    }
}
