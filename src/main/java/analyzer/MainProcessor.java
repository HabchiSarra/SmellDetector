package analyzer;

import entities.PaprikaApp;
import entities.PaprikaClass;
import entities.PaprikaMethod;
import spoon.Launcher;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtInterface;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by sarra on 21/02/17.
 */
public class MainProcessor {

    static PaprikaApp currentApp;
    static PaprikaClass currentClass;
    static PaprikaMethod currentMethod;
    static ArrayList<URL> paths = new ArrayList<>();
    String appPath;
    private String jarsPath;
    private String sdkPath;

    public MainProcessor(String appName, int appVersion, int commitNumber, String status, String appKey, String appPath, String sdkPath, String jarsPath, int sdkVersion, String module) {
        this.currentApp = PaprikaApp.createPaprikaApp(appName, appVersion, commitNumber, status, appKey, appPath, sdkVersion, module);
        currentClass = null;
        currentMethod = null;
        this.appPath = appPath;
        this.jarsPath = jarsPath;
        this.sdkPath = sdkPath;
    }

    public void process() {
        Launcher launcher = new Launcher();
        launcher.addInputResource(appPath);
        launcher.getEnvironment().setNoClasspath(true);
        launcher.buildModel();
        AbstractProcessor<CtClass> classProcessor = new ClassProcessor();
        AbstractProcessor<CtInterface> interfaceProcessor = new InterfaceProcessor();
        launcher.addProcessor(classProcessor);
        launcher.addProcessor(interfaceProcessor);
        launcher.process();
    }

    private void update_classpath(Launcher launcher) {
        try {
            paths = new ArrayList<>();
            if (this.jarsPath != null) {
                File folder = new File(jarsPath);
                paths = this.listFilesForFolder(folder);
            }
            if (this.sdkPath != null) {
                paths.add(new File(sdkPath).toURI().toURL());
            }
            String[] cl = new String[paths.size()];
            for (int i = 0; i < paths.size(); i++) {
                URL url = paths.get(i);
                cl[i] = url.getPath();
            }
            launcher.getEnvironment().setSourceClasspath(cl);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private ArrayList<URL> listFilesForFolder(final File folder) throws IOException {
        ArrayList<URL> jars = new ArrayList<>();
        File[] files = folder.listFiles();
        if (files == null) {
            return jars;
        }
        for (final File fileEntry : files) {
            jars.add(fileEntry.toURI().toURL());
        }
        return jars;
    }
}
