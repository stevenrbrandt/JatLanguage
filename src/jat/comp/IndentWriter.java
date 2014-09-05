package jat.comp;

import java.io.Writer;
import java.io.IOException;

public class IndentWriter extends Writer {
    private int indent;
    private boolean flag = false;
    private Writer w;
    public IndentWriter(Writer w) {
        this.w = w;
    }
    @Override
        public void write(char[] buf,int off,int len) throws IOException {
            int n = off + len;
            for(int i=off;i<n;i++) {
                if(buf[i] == '}') indent -= 2;
                if(flag) {
                    flag = false;
                    for(int j=0;j<indent;j++)
                        w.write(' ');
                }
                if(buf[i] == '{') indent += 2;
                w.write(buf[i]);
                if(buf[i] == '\n')
                    flag = true;
            }
        }
    @Override
        public void close() throws IOException {
            w.close();
        }
    @Override
        public void flush() throws IOException {
            w.flush();
        }
}
