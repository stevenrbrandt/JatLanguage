package jat.comp;

import edu.lsu.cct.piraha.Group;

public class CompFailException extends RuntimeException {
    public int line;
    CompFailException(String msg,Jat j,Group g) {
        this(msg,j.srcFile,g.getLine());
        this.line = g.getLine();
    }
    CompFailException(String msg) {
        super(msg);
    }
    CompFailException(String msg,String file,int line) {
        super(msg+" at "+file+":"+line);
        this.line = line;
    }
}
