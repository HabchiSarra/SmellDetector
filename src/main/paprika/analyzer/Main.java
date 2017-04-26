package analyzer;

import entities.*;
import metrics.MetricsCalculator;
import neo4j.ModelToGraph;

/**
 * Created by sarra on 17/02/17.
 */
public class Main {


    public static void main(String[] args) {
        String path = "/home/sarra/Android-Projects/DuckDuckGo-Recursive/Android/app/src/main/java/com/duckduckgo/mobile/android";
        String name = "DuckDuckGo";
        String key = "DuckDuckGo";
        String sdkPath = "/home/sarra/Android/Sdk/platforms/android-23/android.jar";
        String jarsPath = "/home/sarra/Android-Projects/DuckDuckGo-Recursive/Android/app/runtime";
        MainProcessor mainProcessor = new MainProcessor(name, key, path, sdkPath, jarsPath);
        mainProcessor.process();
        GraphCreator graphCreator = new GraphCreator(MainProcessor.currentApp);
        graphCreator.createClassHierarchy();
        graphCreator.createCallGraph();
        MetricsCalculator.calculateAppMetrics(MainProcessor.currentApp);
        ModelToGraph modelToGraph=new ModelToGraph("/home/sarra/Paprika-BDD/databases/graph.db");
        modelToGraph.insertApp(MainProcessor.currentApp);
    }

    public static void addLibrary(PaprikaApp paprikaApp, String libraryString){
        PaprikaLibrary.createPaprikaLibrary(libraryString,paprikaApp);
    }

    public static void showModel(PaprikaApp app) {
        System.out.println("App: " + app.getName());
        for (PaprikaClass paprikaClass : app.getPaprikaClasses()) {
            System.out.println("---------------------------------------------------------------------------------------------");
            System.out.println(" Class : " + paprikaClass.getName());
            System.out.println(" Visibility : " + paprikaClass.getModifier().name());
            System.out.println(" isActivity : " + paprikaClass.isActivity());
            System.out.println(" isInterface : " + paprikaClass.isInterface());
            System.out.println(" isStatic : " + paprikaClass.isStatic());
            System.out.println(" isApplication : " + paprikaClass.isApplication());
            System.out.println(" isBroadcastReceiver : " + paprikaClass.isBroadcastReceiver());
            System.out.println(" isContentProvider : " + paprikaClass.isContentProvider());
            System.out.println(" isView : " + paprikaClass.isView());
            System.out.println(" isService : " + paprikaClass.isService());
            System.out.println(" isAsyncTask : " + paprikaClass.isAsyncTask());
            System.out.println(" isInnerClass : " + paprikaClass.isInnerClass());
            for (PaprikaMethod paprikaMethod : paprikaClass.getPaprikaMethods()) {
                showMethod(paprikaMethod);
            }
            for (PaprikaVariable paprikaVariable : paprikaClass.getPaprikaVariables()) {
                showVariable(paprikaVariable);
            }
            if (paprikaClass.getParent() != null) {
                System.out.println(" Inherits of internal class: " + paprikaClass.getParent().getName());
            } else if (paprikaClass.getParentName() != null) {
                System.out.println(" Inherits of external class: " + paprikaClass.getParentName());
            }
            System.out.println("---------------------------------------------------------------------------------------------");

        }
    }

    public static void showMethod(PaprikaMethod paprikaMethod) {
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println(" Method : " + paprikaMethod.getName());
        System.out.println(" Return type : " + paprikaMethod.getReturnType());
        System.out.println(" Visbility : " + paprikaMethod.getModifier().name());
        System.out.println(" isGetter : " + paprikaMethod.isGetter() + " isSetter : " + paprikaMethod.isSetter() + " isConstructor : " + paprikaMethod.isConstructor());
        for (PaprikaArgument paprikaArgument : paprikaMethod.getArguments()) {
            showArgument(paprikaArgument);
        }
        System.out.println("Calls +++++ ");
        for (Entity entity : paprikaMethod.getCalledMethods()) {
            if (entity instanceof PaprikaMethod) {
                System.out.println(" Méthode : " + entity.getName() + " Classe "
                        + ((PaprikaMethod) entity).getPaprikaClass().getName());

            } else {
                System.out.println(" Méthode externe: " + entity.getName() + " Classe "
                        + ((PaprikaExternalMethod) entity).getPaprikaExternalClass().getName());

            }

        }

        System.out.println("Uses +++++++");
        for (PaprikaVariable paprikaVariable : paprikaMethod.getUsedVariables()) {
            System.out.println(" Variable : " + paprikaVariable.getName() + " Classe : " + paprikaVariable.getPaprikaClass());
        }
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

    }

    public static void showArgument(PaprikaArgument paprikaArgument) {
        System.out.println(" Argument : " + paprikaArgument.getName());
        System.out.println(" position : " + paprikaArgument.getPosition());
    }

    public static void showVariable(PaprikaVariable paprikaVariable) {
        System.out.println("*********************************************************************************************");
        System.out.println(" Variable : " + paprikaVariable.getName());
        System.out.println(" Type : " + paprikaVariable.getType());
        System.out.println(" Visibility : " + paprikaVariable.getModifier().name());
        System.out.println("*********************************************************************************************");

    }

    //public static void show


}
