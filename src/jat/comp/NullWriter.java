package jat.comp;

public class NullWriter extends java.io.Writer {
    @Override
    public void write(char[] a,int b,int c) {
    }
    @Override
    public void flush() {}
    @Override
    public void close() {}
}
