package jat.lang;

public final class Util {
    public static final boolean equals(int a,int b) { return a == b; }
    public static final boolean equals(Integer a,int b) { return a == b; }
    public static final boolean equals(int a,Integer b) { return a == b; }
    public static final boolean equals(double a,double b) { return a == b; }
    public static final boolean equals(short a,short b) { return a == b; }
    public static final boolean equals(long a,long b) { return a == b; }
    public static final boolean equals(char a,char b) { return a == b; }
    public static final boolean equals(byte a,byte b) { return a == b; }
    public static final boolean equals(float a,float b) { return a == b; }
    public static final boolean equals(Object a,Object b) { return a == b; }
    public static final boolean equals(Number a,Number b) { return a.doubleValue() == b.doubleValue(); }
    public static final boolean equals(boolean a,boolean b) { return a == b; }
    public static final boolean equals(String a,String b) {
        if(a == null)
            return b == null;
        return a.equals(b);
    }
    public static final boolean equals(Value a,Value b) {
        if(a == null)
            return b == null;
        return a.equals(b);
    }
}
