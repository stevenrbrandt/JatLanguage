package jat.lang;

import jat.comp.Jat;

public abstract class UnitTest {
    public class UnitTestFail extends RuntimeException {
        public UnitTestFail(String msg) { super(msg); }
    }
    public abstract void runTest();
    public void assertTrue(boolean b,String msg) {
        if(!b) throw new UnitTestFail("AssertTrue: "+msg);
    }
    public void assertFalse(boolean b,String msg) {
        if(b) throw new UnitTestFail("AssertFalse: "+msg);
    }
    public static void main(String[] args) throws Exception {
        Jat j = new Jat();
        j.compile(args[0],false);
        String className = j.pkg + "." + j.objName;
        Class<?> c = Class.forName(className);
        try {
            UnitTest ut = (UnitTest)c.newInstance();
            ut.runTest();
        } catch(ClassCastException cce) {
            return;
        }
    }
}
