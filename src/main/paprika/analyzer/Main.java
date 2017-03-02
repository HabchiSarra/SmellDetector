package analyzer;
import entities.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by sarra on 17/02/17.
 */
public class Main {


    public static void main(String[] args) {
        String path ="../android/src/com/owncloud/android";
        String name = "owncloud";
        String key ="owncloud";
        String sdkPath ="/home/sarra/IdeaProjects/Android-jars/android25.jar";
        String jarsPath ="";
        MainProcessor mainProcessor=new MainProcessor(name,key,path, sdkPath, jarsPath);
        mainProcessor.process();
        GraphCreator graphCreator = new GraphCreator(MainProcessor.currentApp);
        graphCreator.createClassHierarchy();
        graphCreator.createCallGraph();
        //showModel(MainProcessor.currentApp);


    }

    public static void showModel(PaprikaApp app){
        System.out.println("App: "+ app.getName());
        for(PaprikaClass paprikaClass: app.getPaprikaClasses()) {
            System.out.println(" Class : "+ paprikaClass.getName());
            System.out.println(" Visibility : "+ paprikaClass.getModifier().name());
            System.out.println(" isActivity : "+paprikaClass.isActivity());
            System.out.println(" isInterface : "+paprikaClass.isInterface());
            System.out.println(" isStatic : "+paprikaClass.isStatic());
            for (PaprikaMethod paprikaMethod: paprikaClass.getPaprikaMethods()) {
                showMethod(paprikaMethod);
            }
            for (PaprikaVariable paprikaVariable: paprikaClass.getPaprikaVariables()){
                showVariable(paprikaVariable);
            }
        }
    }

    public static void showMethod(PaprikaMethod paprikaMethod){
        System.out.println(" Method : "+paprikaMethod.getName());
        System.out.println(" Return type : "+ paprikaMethod.getReturnType());
        System.out.println(" modifier : "+ paprikaMethod.getModifier().name());
        for (PaprikaArgument paprikaArgument: paprikaMethod.getArguments()){
            showArgument(paprikaArgument);
        }
    }

    public  static  void showArgument(PaprikaArgument paprikaArgument){
        System.out.println(" Argument : "+ paprikaArgument.getName());
        System.out.println(" position : "+ paprikaArgument.getPosition());
    }

    public static void showVariable(PaprikaVariable paprikaVariable){
        System.out.println(" Variable : "+paprikaVariable.getName());
        System.out.println(" Type : "+paprikaVariable.getType());
        System.out.println(" Visbility : "+paprikaVariable.getModifier().name());
    }




}
