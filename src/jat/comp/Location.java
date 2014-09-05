package jat.comp;

public class Location {
    final String srcFile;
    final int line;
    public Location(String srcFile,int line) {
        this.srcFile = srcFile;
        this.line = line;
    }
    public String toString() {
        return srcFile + ":" + (line <= 0 ? "?" : line);
    }
}
