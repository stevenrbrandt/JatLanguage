package jat.ant;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.Assertions;
import org.apache.tools.ant.taskdefs.ExecuteJava;
import org.apache.tools.ant.taskdefs.Redirector;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.Java;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

import java.lang.reflect.Method;

import jat.lang.UnitTest;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Jat extends Task {
    Map<String,String> classFiles = new HashMap<String,String>();
    String currentJat;

    public void handleOutput(String s) {
        String pre = "class: ";
        if(s.startsWith(pre)) {
            String classFile = s.substring(pre.length()).trim();
            classFiles.put(currentJat,classFile);
        } else {
            log(s, Project.MSG_INFO);
        }
    }
    static class JatJavac extends Javac {
        JatJavac() {
            super();
            init();
        }
        public void compile(File file) {
            compileList = new File[]{file};
            super.compile();
        }
    }
    public void javac(final String file) throws BuildException {
        final String src = file.substring(0,file.length()-4)+".java";
        final File srcFile = new File(src);
        if(!srcFile.exists()) {
            return;
        }
        JatJavac javac = new JatJavac();
        javac.setTaskName("javac");
        javac.setClasspath(cp);
        javac.setProject(getProject());
        javac.setDebug(true);
        javac.setDestdir(new File("."));
        javac.createSrc();
        javac.compile(srcFile);
    }

    File find(String needle) throws IOException {
        for(String path : cp.toString().split(File.pathSeparator)) {
            File p = new File(path);
            if(p.isDirectory()) {
                File cf = new File(p,needle);
                if(cf.exists()) {
                    return cf;
                }
            } else {
                ZipFile zf = new ZipFile(p);
                Enumeration<? extends ZipEntry> e = zf.entries();
                while(e.hasMoreElements()) {
                    ZipEntry ze = e.nextElement();
                    if(ze.getName().equals(needle)) {
                        return p;
                    }
                }
            }
        }
        throw new BuildException("Cannot find "+needle);
    }

    /** Translate the Jat file to a Java file. */
    public void build(String file) {
        try {
            currentJat = file;
            File javaFile = new File(
                    file.substring(0,file.length()-3)+"java");
            File jatFile = new File(file);
            File jatAntClass = find("jat/ant/Jat.class");
            File jatCompClass = find("jat/comp/Jat.class");

            // Determine whether the translated file
            // is out of date.
            boolean upToDate = true;
            if(!javaFile.exists()) {
                upToDate = false;
            } else if(javaFile.lastModified() < jatFile.lastModified()) {
                upToDate = false;
            } else if(javaFile.lastModified() < jatAntClass.lastModified()) {
                upToDate = false;
            } else if(javaFile.lastModified() < jatCompClass.lastModified()) {
                upToDate = false;
            }
            if(upToDate)
                return;

            System.out.println("compile: "+file);

            Redirector redirector = new Redirector(this);
            CommandlineJava command = new CommandlineJava();
            command.createArgument().setValue(file);
            command.createVmArgument().setLine("-ea");
            command.createClasspath(getProject());
            command.setClassname("jat.comp.Jat");
            ExecuteJava exe = new ExecuteJava();
            exe.setClasspath(cp);
            exe.setJavaCommand(command.getJavaCommand());
            exe.setSystemProperties(command.getSystemProperties());
            redirector.createStreams();
            exe.execute(getProject());
            redirector.complete();
            if (exe.killedProcess()) {
                throw new BuildException("Timeout");
            } else {
                javac(file);
            }
        } catch(IOException ioe) {
            throw new BuildException("Error: "+ioe.getMessage());
        }
    }
    FileSet fs;
    public void addFileset(FileSet fs) {
        this.fs = fs;
    }
    Path cp;
    public void addClasspath(Path cp) {
        this.cp = cp;
    }
    public void init() {
    }
    @SuppressWarnings("unchecked")
    public void execute() {
        // There's no way to make the warning go away
        Iterator<Resource> iter = fs.iterator();
        while(iter.hasNext()) {
            Resource fr = iter.next();
            build(fr.toString());
        }
    }
}
