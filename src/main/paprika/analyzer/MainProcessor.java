package analyzer;

import entities.PaprikaApp;
import entities.PaprikaClass;
import entities.PaprikaMethod;
import spoon.Launcher;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by sarra on 21/02/17.
 */
public class MainProcessor {

    static PaprikaApp currentApp ;
    static PaprikaClass currentClass;
    static PaprikaMethod currentMethod ;
    static ArrayList<URL> paths;
    String appPath;
    String jarsPath;
    String sdkPath;
    public MainProcessor(String appName, String appKey, String appPath, String sdkPath, String jarsPath) {
        this.currentApp =PaprikaApp.createPaprikaApp(appName,appKey);
        currentClass= null;
        currentMethod = null;
        this.appPath = appPath;
        this.jarsPath = jarsPath;
        this.sdkPath=sdkPath;
    }

    public void process(){


        Launcher launcher=new Launcher();
        launcher.addInputResource(appPath);
        launcher.getEnvironment().setNoClasspath(true);
        File folder =new File(jarsPath);
        try {
            paths =this.listFilesForFolder(folder);
            paths.add(new URL(sdkPath));
            //launcher.getEnvironment().setSourceClasspath(paths.toArray(new String[paths.size()]));
            launcher.buildModel();
            ClassProcessor classProcessor=new ClassProcessor();
            MethodProcessor methodProcessor =new MethodProcessor();
            launcher.addProcessor(classProcessor);
            launcher.addProcessor(methodProcessor);
            launcher.process();
        }catch (IOException ioException){
            ioException.printStackTrace();
        }

    }

    public ArrayList<URL> listFilesForFolder(final File folder) throws IOException {
        ArrayList<URL> jars =new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                jars.add(fileEntry.toURI().toURL());
            }
        }

        return jars;
    }
}
