package analyzer;

import entities.*;
import metrics.MetricsCalculator;

import neo4j.*;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;
import scala.collection.mutable.ArrayLike$class;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by sarra on 17/02/17.
 */
public class Main {


    public static void main(String[] args) {
        //testRun();
        ArgumentParser parser = ArgumentParsers.newArgumentParser("paprika");
        Subparsers subparsers = parser.addSubparsers().dest("sub_command");
        Subparser analyseParser = subparsers.addParser("analyse").help("Analyse an app");
        analyseParser.addArgument("folder").help("Path of the code source folder");
        analyseParser.addArgument("-a", "--androidJar").required(true).help("Path to android platform jar");
        analyseParser.addArgument("-db", "--database").required(true).help("Path to neo4J Database folder");
        analyseParser.addArgument("-n", "--name").required(true).help("Name of the application");
        analyseParser.addArgument("-p", "--package").required(false).help("Application main package");
        analyseParser.addArgument("-k", "--key").required(true).help("sha256 of the apk used as identifier");
        analyseParser.addArgument("-d", "--dependencies").required(true).help("Path to dependencies");
        analyseParser.addArgument("-l", "--libs").help("List of the external libs used by the apps (separated by :)");
        analyseParser.addArgument("-v", "--version").required(true).help("Version of the apps");
        analyseParser.addArgument("-cn", "--commitNumber").required(true).help("Real commit number");
        analyseParser.addArgument("-s", "--status").required(true).help("Commit status");

        Subparser queryParser = subparsers.addParser("query").help("Query the database");
        queryParser.addArgument("-db", "--database").required(true).help("Path to neo4J Database folder");
        queryParser.addArgument("-r", "--request").help("Request to execute");
        queryParser.addArgument("-c", "--csv").help("path to register csv files").setDefault("");
        queryParser.addArgument("-dk", "--delKey").help("key to delete");
        queryParser.addArgument("-dp", "--delPackage").help("Package of the applications to delete");
        queryParser.addArgument("-d", "--details").type(Boolean.class).setDefault(false).help("Show the concerned entity in the results");

        try {
            Namespace res = parser.parseArgs(args);
            if(res.getString("sub_command").equals("analyse")){
                runAnalysis(res);
            }
            else if(res.getString("sub_command").equals("query")){
                queryMode(res);
            }
        } catch (ArgumentParserException e) {
            analyseParser.handleError(e);
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void testRun() {
        String path = "/home/sarra/Desktop/ASE-Downloads/OLDBASE/2017-07-08/seadroid/app";
        String name = "Seadroid";
        String key = "droid-droid";
        int version =1;
        int commitNumber =1;
        String status="Built";
        String sdkPath = "/home/sarra/Android/Sdk/platforms/android-19/android.jar";
        String jarsPath =  "/home/sarra/Desktop/ASE-Downloads/OLDBASE/2017-07-08/seadroid/app";
        MainProcessor mainProcessor = new MainProcessor(name, version,commitNumber, status, key, path, sdkPath, jarsPath);
        mainProcessor.process();
        GraphCreator graphCreator = new GraphCreator(MainProcessor.currentApp);
        graphCreator.createClassHierarchy();
        graphCreator.createCallGraph();
        MetricsCalculator.calculateAppMetrics(MainProcessor.currentApp);
       ModelToGraph modelToGraph=new ModelToGraph("/home/sarra/Desktop/ASE-Downloads/OLDBASE/17-07-2017/databases/graph.db");
        modelToGraph.insertApp(MainProcessor.currentApp);

    }

    public static void runAnalysis(Namespace arg) throws Exception {
        System.out.println("Collecting metrics");
        String path = arg.getString("folder");
        String name = arg.getString("name");
        int version = Integer.valueOf(arg.getString("version"));
        String key = arg.getString("key");
        String sdkPath = arg.getString("androidJar");
        String jarsPath =arg.getString("dependencies");
        int commitNumber = Integer.valueOf(arg.getString("commitNumber"));
        String status =arg.getString("status");
        String[] libs = {};
        if(arg.getString("libs") != null)
        {
            libs = arg.getString("libs").split(":");
        }
        MainProcessor mainProcessor = new MainProcessor(name, version,commitNumber, status, key, path, sdkPath, jarsPath);
        mainProcessor.process();
        GraphCreator graphCreator = new GraphCreator(MainProcessor.currentApp);
        graphCreator.createClassHierarchy();
        graphCreator.createCallGraph();
        if(libs !=null)
        {
            for(String lib : libs){
                if(lib != "")
                {
                    addLibrary(MainProcessor.currentApp,lib);
                }
            }
        }
        MetricsCalculator.calculateAppMetrics(MainProcessor.currentApp);
        ModelToGraph modelToGraph=new ModelToGraph(arg.getString("database"));
        modelToGraph.insertApp(MainProcessor.currentApp);
        System.out.println("Saving into database "+arg.getString("database"));
        System.out.println("Done");
    }

    public static void queryMode(Namespace arg) throws Exception {
        System.out.println("Executing Queries");
        QueryEngine queryEngine = new QueryEngine(arg.getString("database"));
        String request = arg.get("request");
        Boolean details = arg.get("details");
        Calendar cal = new GregorianCalendar();
        String csvDate = String.valueOf(cal.get(Calendar.YEAR))+"_"+String.valueOf(cal.get(Calendar.MONTH)+1)+"_"+String.valueOf(cal.get(Calendar.DAY_OF_MONTH))+"_"+String.valueOf(cal.get(Calendar.HOUR_OF_DAY))+"_"+String.valueOf(cal.get(Calendar.MINUTE));
        String csvPrefix = arg.getString("csv")+csvDate;
        System.out.println("Resulting csv file name will start with prefix "+csvPrefix);
        queryEngine.setCsvPrefix(csvPrefix);
        switch(request){
            case "ARGB8888":
                ARGB8888Query.createARGB8888Query(queryEngine).execute(details);
            case "MIM":
                MIMQuery.createMIMQuery(queryEngine).execute(details);
                break;
            case "IGS":
                IGSQuery.createIGSQuery(queryEngine).execute(details);
                break;
            case "LIC":
                LICQuery.createLICQuery(queryEngine).execute(details);
                break;
            case "NLMR":
                NLMRQuery.createNLMRQuery(queryEngine).execute(details);
                break;
            case "CC":
                CCQuery.createCCQuery(queryEngine).executeFuzzy(details);
                break;
            case "LM":
                LMQuery.createLMQuery(queryEngine).executeFuzzy(details);
                break;
            case "SAK":
                SAKQuery.createSAKQuery(queryEngine).executeFuzzy(details);
                break;
            case "BLOB":
                BLOBQuery.createBLOBQuery(queryEngine).executeFuzzy(details);
                break;
            case "OVERDRAW":
                OverdrawQuery.createOverdrawQuery(queryEngine).execute(details);
                break;
            case "HSS":
                HeavyServiceStartQuery.createHeavyServiceStartQuery(queryEngine).executeFuzzy(details);
                break;
            case "HBR":
                HeavyBroadcastReceiverQuery.createHeavyBroadcastReceiverQuery(queryEngine).executeFuzzy(details);
                break;
            case "HAS":
                HeavyAsyncTaskStepsQuery.createHeavyAsyncTaskStepsQuery(queryEngine).executeFuzzy(details);
                break;
            case "THI":
                TrackingHardwareIdQuery.createTrackingHardwareIdQuery(queryEngine).execute(details);
                break;
            case "ALLHEAVY":
                HeavyServiceStartQuery.createHeavyServiceStartQuery(queryEngine).executeFuzzy(details);
                HeavyBroadcastReceiverQuery.createHeavyBroadcastReceiverQuery(queryEngine).executeFuzzy(details);
                HeavyAsyncTaskStepsQuery.createHeavyAsyncTaskStepsQuery(queryEngine).executeFuzzy(details);
                break;
            case "ANALYZED":
                queryEngine.AnalyzedAppQuery();
                break;
            case "DELETE":
                queryEngine.deleteQuery(arg.getString("delKey"));
                break;
            case "DELETEAPP":
                if(arg.get("delKey") != null) { queryEngine.deleteEntireApp(arg.getString("delKey")); }
                else {
                    queryEngine.deleteEntireAppFromPackage(arg.getString("delPackage"));
                }
                break;
            case "STATS":
                QuartileCalculator quartileCalculator = new QuartileCalculator(queryEngine);
                quartileCalculator.calculateClassComplexityQuartile();
                quartileCalculator.calculateLackofCohesionInMethodsQuartile();
                quartileCalculator.calculateNumberOfAttributesQuartile();
                quartileCalculator.calculateNumberOfImplementedInterfacesQuartile();
                quartileCalculator.calculateNumberOfMethodsQuartile();
                quartileCalculator.calculateNumberofInstructionsQuartile();
                quartileCalculator.calculateCyclomaticComplexityQuartile();
                quartileCalculator.calculateNumberOfMethodsForInterfacesQuartile();
                break;
            case "ALLLCOM":
                queryEngine.getAllLCOM();
                break;
            case "ALLCYCLO":
                queryEngine.getAllCyclomaticComplexity();
                break;
            case "ALLCC":
                queryEngine.getAllClassComplexity();
                break;
            case "ALLNUMMETHODS":
                queryEngine.getAllNumberOfMethods();
                break;
            case "COUNTVAR":
                queryEngine.countVariables();
                break;
            case "COUNTINNER":
                queryEngine.countInnerClasses();
                break;
            case "COUNTASYNC":
                queryEngine.countAsyncClasses();
                break;
            case "COUNTVIEWS":
                queryEngine.countViews();
                break;
            case "NONFUZZY":
                //ARGB8888Query.createARGB8888Query(queryEngine).execute(details);
                IGSQuery.createIGSQuery(queryEngine).execute(details);
                MIMQuery.createMIMQuery(queryEngine).execute(details);
                LICQuery.createLICQuery(queryEngine).execute(details);
                NLMRQuery.createNLMRQuery(queryEngine).execute(details);
                OverdrawQuery.createOverdrawQuery(queryEngine).execute(details);
                UnsuitedLRUCacheSizeQuery.createUnsuitedLRUCacheSizeQuery(queryEngine).execute(details);
                InitOnDrawQuery.createInitOnDrawQuery(queryEngine).execute(details);
                UnsupportedHardwareAccelerationQuery.createUnsupportedHardwareAccelerationQuery(queryEngine).execute(details);
                HashMapUsageQuery.createHashMapUsageQuery(queryEngine).execute(details);
                InvalidateWithoutRectQuery.createInvalidateWithoutRectQuery(queryEngine).execute(details);
                break;
            case "FUZZY":
                CCQuery.createCCQuery(queryEngine).executeFuzzy(details);
                LMQuery.createLMQuery(queryEngine).executeFuzzy(details);
                SAKQuery.createSAKQuery(queryEngine).executeFuzzy(details);
                BLOBQuery.createBLOBQuery(queryEngine).executeFuzzy(details);
                HeavyServiceStartQuery.createHeavyServiceStartQuery(queryEngine).executeFuzzy(details);
                HeavyBroadcastReceiverQuery.createHeavyBroadcastReceiverQuery(queryEngine).executeFuzzy(details);
                HeavyAsyncTaskStepsQuery.createHeavyAsyncTaskStepsQuery(queryEngine).executeFuzzy(details);
                break;
            case "ALLAP":
                ARGB8888Query.createARGB8888Query(queryEngine).execute(details);
                CCQuery.createCCQuery(queryEngine).executeFuzzy(details);
                LMQuery.createLMQuery(queryEngine).executeFuzzy(details);
                SAKQuery.createSAKQuery(queryEngine).executeFuzzy(details);
                BLOBQuery.createBLOBQuery(queryEngine).executeFuzzy(details);
                MIMQuery.createMIMQuery(queryEngine).execute(details);
                IGSQuery.createIGSQuery(queryEngine).execute(details);
                LICQuery.createLICQuery(queryEngine).execute(details);
                NLMRQuery.createNLMRQuery(queryEngine).execute(details);
                OverdrawQuery.createOverdrawQuery(queryEngine).execute(details);
                HeavyServiceStartQuery.createHeavyServiceStartQuery(queryEngine).executeFuzzy(details);
                HeavyBroadcastReceiverQuery.createHeavyBroadcastReceiverQuery(queryEngine).executeFuzzy(details);
                HeavyAsyncTaskStepsQuery.createHeavyAsyncTaskStepsQuery(queryEngine).executeFuzzy(details);
                UnsuitedLRUCacheSizeQuery.createUnsuitedLRUCacheSizeQuery(queryEngine).execute(details);
                InitOnDrawQuery.createInitOnDrawQuery(queryEngine).execute(details);
                UnsupportedHardwareAccelerationQuery.createUnsupportedHardwareAccelerationQuery(queryEngine).execute(details);
                HashMapUsageQuery.createHashMapUsageQuery(queryEngine).execute(details);
                InvalidateWithoutRectQuery.createInvalidateWithoutRectQuery(queryEngine).execute(details);
                TrackingHardwareIdQuery.createTrackingHardwareIdQuery(queryEngine).execute(details);
                break;
            case "FORCENOFUZZY":
                CCQuery.createCCQuery(queryEngine).execute(details);
                LMQuery.createLMQuery(queryEngine).execute(details);
                SAKQuery.createSAKQuery(queryEngine).execute(details);
                BLOBQuery.createBLOBQuery(queryEngine).execute(details);
                HeavyServiceStartQuery.createHeavyServiceStartQuery(queryEngine).execute(details);
                HeavyBroadcastReceiverQuery.createHeavyBroadcastReceiverQuery(queryEngine).execute(details);
                HeavyAsyncTaskStepsQuery.createHeavyAsyncTaskStepsQuery(queryEngine).execute(details);
                break;
            default:
                System.out.println("Executing custom request");
                queryEngine.executeRequest(request);
        }
        queryEngine.shutDown();
        System.out.println("Done");
    }



    public static void addLibrary(PaprikaApp paprikaApp, String libraryString){
        PaprikaLibrary.createPaprikaLibrary(libraryString,paprikaApp);
    }






}
