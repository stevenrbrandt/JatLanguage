package jat.lang;

public class Tasks {
    public static void schedule(Runnable r) {
        new Thread(r).start();
    }
}
