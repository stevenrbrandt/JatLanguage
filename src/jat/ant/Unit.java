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

import java.lang.reflect.Method;

import jat.lang.UnitTest;

public class Unit extends Task {
    /**
     * Attempt to run the target program. If the
     * program is not a UnitTest, the test is considered
     * successful. If it is a UnitTest, the runTest()
     * method is called. If the method completes without
     * throwing an exception, it is counted as successful.
     */
    public void run(String jatFile) {
        try {
            Redirector redirector = new Redirector(this);
            CommandlineJava command = new CommandlineJava();
            command.createVmArgument().setLine("-ea");
            command.createArgument().setLine(jatFile);
            command.setClassname("jat.lang.UnitTest");
            ExecuteJava exe = new ExecuteJava();
            exe.setClasspath(cp);
            exe.setJavaCommand(command.getJavaCommand());
            exe.setSystemProperties(command.getSystemProperties());
            redirector.createStreams();
            exe.execute(getProject());
            redirector.complete();
            if (exe.killedProcess()) {
                throw new BuildException("Timeout");
            }
        } catch(Exception e) {
            throw new BuildException(e);
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
            run(fr.toString());
        }
    }
}
