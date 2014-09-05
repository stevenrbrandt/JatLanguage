package jat.comp;

import edu.lsu.cct.piraha.Group;
import java.io.PrintWriter;

public class AtomJat {
    PrintWriter pw;
    public AtomJat(PrintWriter pw) {
        this.pw = pw;
    }
    public void emit(Group g) {
        String pn = g.getPatternName();
        if(pn.equals("block")) {
            pw.println("{");
            for(int i=0;i<g.groupCount();i++)
                emit(g.group(i));
            pw.println("}");
        } else if(Jat.eq(pn,"argsdecl","exprlist")) {
            pw.print('(');
            for(int i=0;i<g.groupCount();i++) {
                emit(g.group(i));
                pw.print(',');
            }
            pw.print("__tr__)");
        } else if(Jat.eq(pn,"assign")) {
            String aop = g.group(1).substring();
            if(aop.equals("+=")) {
                emit(g.group(0));
                pw.print(".set(");
                emit(g.group(0));
                pw.print(".get(__tr__)+");
                emit(g.group(2));
                pw.println(",__tr__);");
            } else if(aop.equals("=")) {
                emit(g.group(0));
                pw.print(".set(");
                emit(g.group(2));
                pw.println(",__tr__);");
            } else {
                g.dumpMatches();
                throw new Error("not handled "+aop);
            }
        } else if(Jat.eq(pn,"statement","dotindex","expr",
            "condexpr","shiftexpr","aexpr","val","call",
            "eqexpr","ampexpr","carexpr","andexpr","pipeexpr")) {
            for(int i=0;i<g.groupCount();i++)
                emit(g.group(i));
        } else if(Jat.eq(pn,"name","num","dquot")) {
            pw.print(g.substring());
        } else if(Jat.eq(pn,"dotname")) {
            for(int i=0;i<g.groupCount();i++) {
                if(i>0) pw.print('.');
                emit(g.group(i));
            }
        } else {
            g.dumpMatches();
            System.out.println(g.substring());
            throw new Error("not handled '"+pn+"'");
        }
    }
}
