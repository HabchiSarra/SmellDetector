package analyzer;

import entities.PaprikaApp;
import entities.PaprikaClass;
import entities.PaprikaMethod;
import spoon.Launcher;

/**
 * Created by sarra on 21/02/17.
 */
public class MainProcessor {

    static PaprikaApp currentApp ;
    static PaprikaClass currentClass;
    static PaprikaMethod currentMethod ;
    String path;
    public MainProcessor(String appName, String appKey, String path) {
        this.currentApp =PaprikaApp.createPaprikaApp(appName,appKey);
        currentClass= null;
        currentMethod = null;
        this.path=path;
    }

    public void process(){


        Launcher launcher=new Launcher();
        launcher.addInputResource(path);
        launcher.getEnvironment().setNoClasspath(true);
        launcher.buildModel();
        ClassProcessor classProcessor=new ClassProcessor();
        MethodProcessor methodProcessor =new MethodProcessor();
        launcher.addProcessor(classProcessor);
        launcher.addProcessor(methodProcessor);
        launcher.process();
    }
}
