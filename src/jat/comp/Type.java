package jat.comp;

import edu.lsu.cct.piraha.*;
import java.util.*;
import java.io.File;

public class Type {
    boolean isPrimitive;
    boolean isInterface = false;
    boolean pseudoPrimitive;
    String name;
    String pkg;
    Location location;
    boolean isArray;
    Set<Type> templates = new HashSet<Type>();
    private Set<Type> interfaces;

    public void check(Jat jat) {
        if(pkg.equals("jat.lang")) {
            if(name.equals("Atom")) {
                for(Type t : templates) {
                    if(t.isPrimitive) {
                        ;
                    } else if(t.pseudoPrimitive) {
                        ;
                    } else if(t.isa(ValueType,jat)) {
                        ;
                    } else if(location != null) {
                        throw new CompFailException("jat.lang.Atom<T> cannot contain '"+t+"'",location.srcFile,location.line);
                    } else {
                        throw new CompFailException("jat.lang.Atom<T> cannot contain '"+t+"'");
                    }
                }
            }
        }
    }

    Set<Type> getInterfaces(Jat jat) {
        if(interfaces != null)
            return interfaces;
        interfaces = new HashSet<Type>();
        try {
            Class c = Class.forName(pkg+"."+name);
            for(Class ifc : c.getInterfaces()) {
                interfaces.add(mkType(ifc,jat));
            }
        } catch(Exception ex) {
            File src = new File(jat.srcFile);
            File alt = new File(src.getParent(),name+".jat");
            Jat jalt = new Jat(jat);
            try {
                jalt.compile(alt.getAbsolutePath(),false);
                interfaces = jalt.interfaces;
            } catch(java.io.IOException ioe) {}
        }
        return interfaces;
    }

    public static Type mkType(Class c,Jat jat) {
        Type t = new Type();
        t.pkg = c.getPackage().getName();
        t.name = c.getName();
        int n = t.name.lastIndexOf('.');
        if(n >= 0) t.name = t.name.substring(n+1);
        t.isInterface = c.isInterface();
        t.isArray = c.isArray();
        t.check(jat);
        return t.pseudo();
    }

    private Type(String pkg,String name) {
        this.name = name;
        this.pkg = pkg;
        pseudo();
    }
    private Type() {}
    static Type mkType(String s,Jat jat) {
        Matcher m = jat.g.matcher("type",s);
        if(m.matches()) {
            return mkType(m,jat);
        } else {
            throw new Error("Not a type: "+s);
        }
    }
    Type pseudo() {
        if("java.lang".equals(pkg) &&
                Jat.eq(name,"String","Integer","Short","Long","Byte",
                "Float","Double","Boolean","Character")) {
            pseudoPrimitive = true;
        }
        return this;
    }
    boolean isa(Type t,Jat jat) {
        if(t.equals(this))
            return true;
        for(Type ifc : getInterfaces(jat)) {
            if(ifc.isa(t,jat))
                return true;
        }
        return false;
    }
    static Type mkType(Group g,Jat jat) {
        String pn = g.getPatternName();
        if(Jat.eq(pn,"dotname")) {
            if(g.groupCount()==1) {
                return mkType(g.group(0),jat);
            } else {
                Type t = new Type();
                t.location = new Location(jat.srcFile,g.getLine());
                StringBuffer pkg = new StringBuffer();
                for(int i=0;i+1<g.groupCount();i++) {
                    if(i > 0) pkg.append('.');
                    pkg.append(g.group(i).substring());
                }
                t.name = g.group(g.groupCount()-1).substring();
                if(pkg.length()>0) {
                    t.pkg = pkg.toString();
                } else {
                    t.pkg = getPackage(t.name,jat);
                }
                t.check(jat);
                return t.pseudo();
            }
        } else if(Jat.eq(pn,"type")) {
            Type t = mkType(g.group(0),jat);
            t.location = new Location(jat.srcFile,g.getLine());
            int j = 1;
            if(j < g.groupCount()) {
                if(Jat.eq(g.group(j),"template")) {
                    Group tpl = g.group(j);
                    for(int i=0;i<tpl.groupCount();i++) {
                        t.templates.add(mkType(tpl.group(i),jat));
                    }
                    j++;
                }
            }
            if(j < g.groupCount()) {
                t.isArray = true;
            }
            t.check(jat);
            return t.pseudo();
        } else if(Jat.eq(pn,"name")) {
            Type t = new Type();
            t.location = new Location(jat.srcFile,g.getLine());
            t.name = g.substring();
            if(Jat.eq(t.name,
                    "void",
                    "int","boolean","double","char",
                    "short","long","byte","float")) {
                t.isPrimitive = true;
                t.pkg = "";
            } else {
                t.pkg = getPackage(t.name,jat);
            }
            t.check(jat);
            return t.pseudo();
        } else {
            throw new Error("unknown type '"+pn+"'");
        }
    }

    static String getPackage(String name,Jat jat) {
        String ret = jat.pkgs.get(name);
        if(ret != null) {
            return ret;
        }
        String[] pkgpath = null;
        if(jat.pkg == null)
            pkgpath = new String[]{"jat.lang","java.lang"};
        else
            pkgpath = new String[]{"jat.lang","java.lang",jat.pkg};
        for(String look : pkgpath) {
            Class<?> c = null;
            try {
                c = Class.forName(look+"."+name);
                ret = c.getPackage().getName();
                jat.pkgs.put(name,ret);
                return ret;
            } catch(Exception ex) {}
        }
        File f = new File(jat.srcFile);
        File alt = new File(f.getParentFile(),name+".jat");
        if(alt.exists()) {
            ret = jat.pkg;
            jat.pkgs.put(name,ret);
            return ret;
        }
        throw new CompFailException("No package for class '"+name+"'");
    }

    String rep;

    @Override
    public int hashCode() {
        if(rep == null)
            toString();
        return rep.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Type) {
            return o.toString().equals(toString());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        if(rep != null)
            return rep;
        StringBuffer sb = new StringBuffer();
        if(!pkg.equals("")) {
            sb.append(pkg);
            sb.append('.');
        }
        sb.append(name);
        if(templates.size()>0) {
            sb.append('<');
            int i=0;
            for(Type t : templates) {
                if(i++ > 0) sb.append(',');
                sb.append(t.objectify());
            }
            sb.append('>');
        }
        if(isArray) {
            sb.append("[]");
        }
        return rep = sb.toString();
    }

    final static Type IntegerType = new Type("java.lang","Integer");
    final static Type ShortType = new Type("java.lang","Short");
    final static Type FloatType = new Type("java.lang","Float");
    final static Type DoubleType = new Type("java.lang","Double");
    final static Type CharType = new Type("java.lang","Character");
    final static Type ByteType = new Type("java.lang","Byte");
    final static Type BooleanType = new Type("java.lang","Boolean");
    final static Type LongType = new Type("java.lang","Long");

    final static Type ValueType = new Type("jat.lang","Value");
    final static Type AtomicType = new Type("jat.lang","AtomicClass");
    final static Type ObjectType = new Type("java.lang","Object");

    public Type objectify() {
        if(isPrimitive) {
            String tn = name;
            if("int".equals(tn)) {
                return IntegerType;
            } else if("short".equals(tn)) {
                return ShortType;
            } else if("float".equals(tn)) {
                return FloatType;
            } else if("double".equals(tn)) {
                return DoubleType;
            } else if("char".equals(tn)) {
                return CharType;
            } else if("byte".equals(tn)) {
                return ByteType;
            } else if("boolean".equals(tn)) {
                return BooleanType;
            } else if("long".equals(tn)) {
                return LongType;
            } else {
                throw new CompFailException("Not a primitive type "+tn);
            }
        }
        return this;
    }
    public boolean checkValue(Jat jat) {
        if(isArray) return false;
        if(isPrimitive) return true;
        if(pseudoPrimitive) return true;
        return isa(ValueType,jat);
    }
}
