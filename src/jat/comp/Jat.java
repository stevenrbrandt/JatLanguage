package jat.comp;

import java.io.*;
import edu.lsu.cct.piraha.*;
import java.util.*;

/*
 * Todo:
 * (1) atomic class, can only have atomic variables
 *     or values as fields. Atomic type can only
 *     hold values or atomic classes. All fields
 *     final.
 * (2) values have an equals method, all final
 *     fields. All fields are primitives or other
 *     values.
 * (3) static variables can only be final and can
 *     only be atomic or value.
 * (4) asyncs instead of threads
 */
// TODO: Threads must be atomic or value
// objects, but the programmer must pick
// one. Maybe just use async() blocks?
public class Jat {
    /// Start Meta-Data 
    public String pkg;
    public String objType;
    public String objName;
    public String srcFile;
    public boolean isAtomic = false;

    public Set<Type> interfaces = new HashSet<Type>();
    public Type extend = Type.ObjectType;

    /** what package does a class live in? */
    public static Map<String,String> pkgs = new HashMap<String,String>();

    /** what class does a static method/field live in */
    public Map<String,String> members = new HashMap<String,String>();

    public Map<String,Field> fields = new HashMap<String,Field>();

    public String compFailMsg;
    public int compFailLine;
    /// End Meta-Data

    boolean atomField;
    boolean staticField;

    PrintWriter pw;
    Grammar g = new Grammar();

    public Jat(Jat jat) {
        g = jat.g;
    }
    public Jat() throws IOException {
        g.compileFile(new File("src/jat/comp/Jat.peg"));
        pkgs.put("System","java.lang");
        pkgs.put("Math","java.lang");
        pkgs.put("String","java.lang");
        pkgs.put("Runnable","java.lang");
        pkgs.put("Exception","java.lang");
        pkgs.put("RuntimeException","java.lang");
        pkgs.put("Error","java.lang");
        pkgs.put("Object","java.lang");

        pkgs.put("Integer","java.lang");
        pkgs.put("Double","java.lang");
        pkgs.put("Short","java.lang");
        pkgs.put("Long","java.lang");

        pkgs.put("Byte","java.lang");
        pkgs.put("Character","java.lang");
        pkgs.put("Boolean","java.lang");
        pkgs.put("Float","java.lang");

        pkgs.put("Value","jat.lang");
        pkgs.put("Atomic","jat.lang");
        pkgs.put("SnapShot","java.lang");
    }

    public static boolean eq(String nm,String... sl) {
        for(String s : sl) {
            if(nm.equals(s))
                return true;
        }
        return false;
    }

    void emitObj(Group g) {
        String s = g.substring();
        if(s.equals("int"))
            pw.print("Integer ");
        else if(s.equals("short"))
            pw.print("Short ");
        else if(s.equals("double"))
            pw.print("Double ");
        else if(s.equals("boolean"))
            pw.print("Boolean ");
        else if(s.equals("char"))
            pw.print("Character ");
        else if(s.equals("float"))
            pw.print("Float");
        else if(s.equals("long"))
            pw.print("Long");
        else if(s.equals("byte"))
            pw.print("Byte");
        else
            emit(g);
    }
    public static boolean eq(Group g,String... sl) {
        return eq(g.getPatternName(),sl);
    }

    void emit(Group g) {
        String pn = g.getPatternName();
        if(eq(pn,"classtype")) {
            objType = g.substring();
            if(eq(objType,"value"))
                pw.print("class");
            else
                pw.print(objType);
            pw.print(' ');
        } else if(eq(pn,"extends")) {
            extend = Type.mkType(g.group(0),this);
            if(extend.isa(Type.ValueType,this)) {
                if(objType.equals("value")) {
                    ;
                } else {
                    raiseFail( 
                        "Cannot extend a value with a class",
                        srcFile,g.getLine());
                }
            }
            if(extend.isa(Type.AtomicType,this)) {
                if(!isAtomic) {
                    raiseFail(
                        "Cannot extend an atomic class with a class or value",
                        srcFile,g.getLine());
                }
            }
            pw.print("extends ");
            pw.print(extend);
            pw.print(' ');
        } else if(eq(pn,"implements")) {
            for(int i=0;i<g.groupCount();i++) {
                interfaces.add(Type.mkType(g.group(i),this));
            }
            if(objType.equals("value")) {
                interfaces.add(Type.mkType("jat.lang.Value",this));
            }
            if(isAtomic) {
                interfaces.add(Type.mkType("jat.lang.AtomicClass",this));
            }
            String pre = " implements ";
            for(Type t : interfaces) {
                pw.print(pre);
                pw.print(t);
                pre = ",";
            }
            pw.print(' ');
        } else if(eq(pn,"name","aop","condop","mulop","addop",
                "array","dquot","squot","num","final","static",
                "public")) {
            pw.print(g.substring());
            pw.print(' ');
        } else if(eq(pn,"dotname")) {
            for(int i=0;i<g.groupCount();i++) {
                if(i > 0) pw.print('.');
                emit(g.group(i));
            }
        } else if(eq(pn,"argsdecl","exprlist")) {
            pw.print('(');
            for(int i=0;i<g.groupCount();i++) {
                if(i > 0) pw.print(',');
                emit(g.group(i));
            }
            pw.print(')');
        } else if(eq(pn,"template")) {
            pw.print('<');
            for(int i=0;i<g.groupCount();i++) {
                if(i>0) pw.print(',');
                emit(g.group(i));
            }
            pw.print('>');
        } else if(eq(pn,"import")) {
            int i = 0;
            String pre = "import ";
            if(g.group(i).getPatternName().equals("static")) {
                pre = "import static ";
                i++;
            }
            Group dot = g.group(i);
            StringBuffer pkg = new StringBuffer();
            for(int j=0;j+1<dot.groupCount();j++) {
                if(j > 0) pkg.append('.');
                pkg.append(dot.group(j).substring());
            }
            String cname = dot.group(dot.groupCount()-1).substring();
            String pname = pkg.toString();
            if(i == 0)
                pkgs.put(cname,pname);
            else
                members.put(cname,pname);
            pw.println(pre+pname+"."+cname+";");
        } else if(eq(pn,"fimport")) {
            int i=0;
            String pre = "import ";
            if(g.group(i).getPatternName().equals("static")) {
                pre = "import static ";
                i++;
            }
            String pname = g.group(i).substring();
            for(int j=i+1;j<g.groupCount();j++) {
                String cname = g.group(j).substring();
                if(i == 0)
                    pkgs.put(cname,pname);
                else
                    members.put(cname,pname);
                pw.println(pre+pname+"."+cname+";");
            }
        } else if(eq(pn,"cdecls","block")) { 
            pw.println("{");
            for(int i=0;i<g.groupCount();i++) {
                emit(g.group(i));
            }
            pw.println("}");
        } else if(eq(pn,"callstmt")) {
            if(g.group(0).substring().equals("async")) {
                if(g.group(1).groupCount() != 1) {
                    //System.out.pr
                }
                async(g.group(1).group(0));
                pw.println(';');
            } else {
                for(int i=0;i<g.groupCount();i++) {
                    emit(g.group(i));
                }
                pw.println(';');
            }
        } else if(eq(pn,"type")) {
            Type t = Type.mkType(g,this);
            pw.print(t.toString());
            pw.print(' ');
        } else if(eq(pn,"forstmt")) {
            pw.print("for(");
            emit(g.group(0));
            pw.print(';');
            emit(g.group(1));
            pw.print(';');
            emit(g.group(2));
            pw.print(") ");
            emit(g.group(3));
        } else if(eq(pn,"fdecl")) {
            // enforce final and interface Value
            // keep list of fields in meta data
            // methods too
            int n = 0;
            boolean finalFlag = staticField||isAtomic;
            if(eq(g.group(n),"final")) {
                n++;
                finalFlag = true;
            }
            boolean isValue = "value".equals(objType);
            if(finalFlag) {
                pw.print("final ");
                finalFlag = true;
            }
            Type t = Type.mkType(g.group(n),this);
            if(isValue && !t.checkValue(this))
               raiseFail(
                    "'"+t.toString()+"' not allowed in Value",g.group(n));
            if(staticField) {
                if(t.checkValue(this) || t.isa(Type.AtomicType,this))
                    System.out.println("pass "+t);
                else
                    raiseFail(
                        "Static fields must be Value or AtomicClass",g.group(n));
            }
            if(isAtomic) {
                if(t.checkValue(this) || t.isa(Type.AtomicType,this))
                    ;
                else
                    raiseFail(
                        "Static fields must be final values or atomics",g.group(n));
            }
            if(atomField) {
                pw.print("jat.lang.Atom<");
                pw.print(t.objectify());
                pw.print(' ');
                pw.print("> ");
            } else {
                pw.print(t);
                pw.print(' ');
            }
            emit(g.group(n+1));
            if(g.groupCount()==n+3) {
                pw.print('=');
                if(atomField) {
                    pw.print("new jat.lang.Atom<");
                    pw.print(t.objectify());
                    pw.print(">(");
                    emit(g.group(n+2));
                    pw.print(')');
                } else {
                    emit(g.group(n+2));
                }
            } else if(atomField) {
                pw.print("= new jat.lang.Atom</**/");
                pw.print(t.objectify());
                pw.print(">()");
            }
            pw.println(';');
        } else if(eq(pn,"ifstmt")) {
            int i;
            pw.print("if(");
            emit(g.group(0));
            pw.print(")");
            emit(g.group(1));
            for(i=2;i+1<g.groupCount();i+=2) {
               pw.print("else if("); 
               emit(g.group(i));
               pw.print(")");
               emit(g.group(i+1));
            }
            if(i < g.groupCount()) {
                pw.print("else");
                emit(g.group(i));
            }
        } else if(eq(pn,"whilestmt")) {
            pw.print("while(");
            emit(g.group(0));
            pw.print(')');
            emit(g.group(1));
        } else if(eq(pn,"forinit")) {
            for(int i=0;i<g.groupCount();i++) {
                if(i+1==g.groupCount()) pw.print('=');
                emit(g.group(i));
            }
        } else if(eq(pn,"paren")) {
            pw.print('(');
            emit(g.group(0));
            pw.print(')');
        } else if(eq(pn,"atomstmt")) {
            pw.println("/**begin atomic**/");
            pw.println("while(true) {");
            pw.println("try {");
            pw.println("final jat.lang.Transaction __tr__"+
                "= new jat.lang.Transaction();");
            AtomJat aj = new AtomJat(this,pw);
            Group g0 = g.group(0);
            for(int i=0;i<g0.groupCount();i++) {
                aj.emit(g0.group(i));
            }
            pw.println("__tr__.finish();");
            pw.println("break;");
            pw.println("} catch(jat.lang.TransactionRestart __res__) {");
            pw.println("continue;");
            pw.println("}");
            pw.println("}");
            pw.println("/**end atomic**/");
        } else if(eq(pn,"atomic")) {
            ;
        } else if(eq(pn,"ampexpr")) {
            if(g.groupCount()==1) {
                emit(g.group(0));
                return;
            }
            if(g.group(1).equals("!=")) pw.print("!");
            pw.print("jat.lang.Util.equals(");
            emit(g.group(0));
            pw.print(',');
            emit(g.group(2));
            pw.print(')');
        } else if(eq(pn,"cdecl")) {
            atomField = false;
            staticField = false;
            for(int i=0;i<g.groupCount();i++) {
                if(eq(g.group(i),"atomic")) {
                    atomField = true;
                } else if(eq(g.group(i),"static")) {
                    staticField = true;
                }
                emit(g.group(i));
            }
        } else if(eq(pn,"class")) {
            for(int i=0;i<g.groupCount();i++) {
                if(eq(g.group(i),"atomic")) {
                    isAtomic = true;
                }
                if(eq(g.group(i),"name")) {
                    objName = g.group(i).substring();
                    pkgs.put(objName,pkg);
                }
                emit(g.group(i));
            }
        } else if(eq(pn,"mdecl","dotindex",
                "argdecl","statement","ctor","call",
                "expr","andexpr","pipeexpr","carexpr",
                "eqexpr","condexpr","shiftexpr","aexpr","val",
                "forincr"
                )) {
            for(int i=0;i<g.groupCount();i++) {
                emit(g.group(i));
            }
        } else if(eq(pn,"assign")) {
            for(int i=0;i<g.groupCount();i++) {
                emit(g.group(i));
            }
            pw.println(';');
        } else if(eq(pn,"return")) {
            pw.print("return ");
            if(g.groupCount()==1)
                emit(g.group(0));
            pw.println(';');
        } else if(eq(pn,"chain")) {
            for(int i=0;i+1 < g.groupCount();i+=2) {
                pw.print('.');
                emit(g.group(i));
                emit(g.group(i+1));
            }
        } else if(eq(pn,"newxpr")) {
            pw.print("new ");
            Type t = Type.mkType(g.group(0),this);
            pw.print(t);
            if(t.isArray) {
                pw.print('{');
                for(int i=0;i<g.group(1).groupCount();i++) {
                    if(i>0) pw.print(',');
                    emit(g.group(1).group(i));
                }
                pw.print('}');
            } else {
                emit(g.group(1));
            }
            /*
            for(int i=1;i<g.groupCount();i++) {
                emit(g.group(i));
            }
            */
        } else if(eq(pn,"package")) {
            pw.print("package ");
            pkg = g.group(0).substring();
            pw.print(pkg);
            pw.println(';');
        } else if(eq(pn,"annotation")) {
            String nm = g.group(0).substring();
            if(eq(nm,"CompFail")) {
                compFailMsg = g.group(1).group(0).substring();
                compFailMsg = compFailMsg.substring(
                    1,compFailMsg.length()-2);
                compFailLine = Integer.parseInt(
                    g.group(1).group(1).substring());
            }
        } else {
            dumpMatches(g);
            System.out.println("Compilation Failed Near Line: "+g.getLine());
            throw new CompFailException("Not handled "+pn,this,g);
        }
    }

    public static void dumpMatches(Group g) {
        StringWriter sw = new StringWriter();
        DebugOutput db = new DebugOutput(new PrintWriter(sw));
        g.dumpMatches(db);
        db.flush();
        System.out.println(sw.toString());
    }

    public void compile(String src,boolean output) throws IOException {
        srcFile = src;
        Matcher m = g.matcher("fname",src);
        if(m.matches()) {
            String outfile = m.group(0).substring()+".java";
            if(output) {
                FileWriter fw = new FileWriter(outfile);
                BufferedWriter bw = new BufferedWriter(fw);
                pw = new PrintWriter(new IndentWriter(bw));
            } else {
                pw = new PrintWriter(new NullWriter());
            }
            String c = Grammar.readContents(new File(src));
            Matcher mc = g.matcher(c);
            if(mc.matches()) {
                try {
                    emit(mc);
                    if(compFailMsg != null) {
                        throw new CompFailException("Expected Failure: "+compFailMsg+":"+compFailLine);
                    }
                    pw.close();
                    System.out.println("class: "+pkg+"."+objName);
                } catch(CompFailException cfe) {
                    if(compFailMsg != null) {
                        if(cfe.getMessage().indexOf(compFailMsg) >= 0) {
                            if(cfe.line == compFailLine) {
                                System.out.println("Compilation Fail Confirmed");
                                File f = new File(outfile);
                                f.delete();
                                return;
                            }
                        }
                    }
                    System.out.println("compilation failed \""+cfe.getMessage()+"\"");
                    pw.close();
                    if(output) {
                        File f = new File(outfile);
                        f.delete();
                        new File(outfile).renameTo(f);
                    }
                    throw cfe;
                }
            } else {
                File f = new File(outfile);
                f.delete();
                throw new CompFailException(mc.near().toString());
            }
        } else {
            throw new IOException("Bad source file name "+src+" "+m.near());
        }
    }

    public void raiseFail(String msg,Group g) {
        raiseFail(msg,srcFile,g.getLine());
    }
    public void raiseFail(String msg,String file,int line) {
        /*
        if(compFailMsg != null) {
            if(msg.indexOf(compFailMsg) >= 0) {
                if(line == compFailLine) {
                    System.out.print("Confirmed Compilation Failure: ");
                    System.out.print(msg);
                    System.out.print(" at ");
                    System.out.print(file);
                    System.out.print(':');
                    System.out.println(line);
                    throw new Success();
                }
            } else {
                System.out.println("No mach for: "+compFailMsg+":"+compFailLine);
            }
        }
        */
        throw new CompFailException(msg,file,line);
    }

    public void async(Group g) {
        String pn = g.getPatternName();
        if(eq(pn,"exprlist")) {
            pw.print('(');
            for(int i=0;i<g.groupCount();i++) {
                if(i > 0) pw.print(',');
                async(g.group(i));
            }
            pw.print(')');
        } else {
            raiseFail("Async does not support '"+pn+"'",g);
        }
    }

    public static void main(String[] args) throws IOException {
        Jat jat = new Jat();
        jat.compile(args[0],true);
    }
}
