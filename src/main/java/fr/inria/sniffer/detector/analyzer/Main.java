/**
 *   Sniffer - Analyze the history of Android code smells at scale.
 *   Copyright (C) 2019 Sarra Habchi
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package fr.inria.sniffer.detector.analyzer;

import fr.inria.sniffer.detector.entities.*;
import fr.inria.sniffer.detector.metrics.MetricsCalculator;

import fr.inria.sniffer.detector.neo4j.*;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sarra on 17/02/17.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class.getName());

    public static void main(String[] args) {
        //testRun();
        ArgumentParser parser = ArgumentParsers.newArgumentParser("paprika");
        Subparsers subparsers = parser.addSubparsers().dest("sub_command");
        Subparser analyseParser = subparsers.addParser("analyse").help("Analyse an app");
        analyseParser.addArgument("folder").help("Path of the code source folder");
        analyseParser.addArgument("-a", "--androidJar").required(false).help("Path to android platform jar");
        analyseParser.addArgument("-db", "--database").required(true).help("Path to neo4J Database folder");
        analyseParser.addArgument("-n", "--name").required(true).help("Name of the application");
        analyseParser.addArgument("-p", "--package").required(false).help("Application main package");
        analyseParser.addArgument("-k", "--key").required(true).help("sha256 of the apk used as identifier");
        analyseParser.addArgument("-d", "--dependencies").required(false).help("Path to dependencies");
        analyseParser.addArgument("-l", "--libs").required(false).help("List of the external libs used by the apps (separated by :)");
        analyseParser.addArgument("-v", "--version").required(false).help("Version of the apps");
        analyseParser.addArgument("-cn", "--commitNumber").required(true).help("Real commit number");
        analyseParser.addArgument("-s", "--status").required(false).help("Commit status");
        analyseParser.addArgument("-m", "--module").required(false).help("analyzed module folder");
        analyseParser.addArgument("-sd", "--sdk").required(false).help("Sdk Version");
        analyseParser.addArgument("-e", "--exclude").nargs("+").required(false).help("Exclude folders using java regex patterns");

        Subparser queryParser = subparsers.addParser("query").help("Query the database");
        queryParser.addArgument("-db", "--database").required(true).help("Path to neo4J Database folder");
        queryParser.addArgument("-r", "--request").help("Request to execute");
        queryParser.addArgument("-c", "--csv").help("path to register csv files").setDefault("");
        queryParser.addArgument("-dk", "--delKey").help("key to delete");
        queryParser.addArgument("-dp", "--delPackage").help("Package of the applications to delete");
        queryParser.addArgument("-d", "--details").type(Boolean.class).setDefault(false).help("Show the concerned entity in the results");

        try {
            Namespace res = parser.parseArgs(args);
            if (res.getString("sub_command").equals("analyse")) {
                runAnalysis(res);
            } else if (res.getString("sub_command").equals("query")) {
                queryMode(res);
            }
        } catch (ArgumentParserException e) {
            analyseParser.handleError(e);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }



    public static void runAnalysis(Namespace arg) throws Exception {
        logger.info("Collecting metrics");
        String path = arg.getString("folder");
        path = new File(path).getAbsolutePath();
        String name = arg.getString("name");
        int version = arg.getString("version") != null ? Integer.valueOf(arg.getString("version")) : -1;
        String key = arg.getString("key");
        String sdkPath = arg.getString("androidJar");
        String jarsPath = arg.getString("dependencies");
        int commitNumber = Integer.valueOf(arg.getString("commitNumber"));
        String status = arg.getString("status") != null ? arg.getString("status") : "NO_STATUS";
        // The input is unfortunately a String
        int sdkVersion = arg.getString("sdk") != null ? Integer.valueOf(arg.getString("sdk")) : -1;
        String module = arg.getString("module") != null ? arg.getString("module") : "NO_MODULE";
        String[] libs = {};
        if (arg.getString("libs") != null) {
            libs = arg.getString("libs").split(":");
        }
        List<String> exclusions = arg.get("exclude");
        MainProcessor mainProcessor = new MainProcessor(name, version, commitNumber, status, key, path, sdkPath, jarsPath, sdkVersion, module, exclusions);
        mainProcessor.process();
        GraphCreator graphCreator = new GraphCreator(MainProcessor.currentApp);
        graphCreator.createClassHierarchy();
        graphCreator.createCallGraph();
        if (libs != null) {
            for (String lib : libs) {
                if (lib != "") {
                    addLibrary(MainProcessor.currentApp, lib);
                }
            }
        }

        MetricsCalculator.calculateAppMetrics(MainProcessor.currentApp);
        ModelToGraph modelToGraph = new ModelToGraph(arg.getString("database"));
        modelToGraph.insertApp(MainProcessor.currentApp);
        logger.info("Saving into database " + arg.getString("database"));
        logger.info("Done");
    }

    public static void queryMode(Namespace arg) throws Exception {
        logger.info("Executing Queries");
        QueryEngine queryEngine = new QueryEngine(arg.getString("database"));
        String request = arg.get("request");
        Boolean details = arg.get("details");
        Calendar cal = new GregorianCalendar();
        String csvDate = String.valueOf(cal.get(Calendar.YEAR)) + "_" + String.valueOf(cal.get(Calendar.MONTH) + 1) + "_" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) + "_" + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + "_" + String.valueOf(cal.get(Calendar.MINUTE));
        String csvPrefix = arg.getString("csv") + csvDate;
        logger.debug("Resulting csv file name will start with prefix " + csvPrefix);
        queryEngine.setCsvPrefix(csvPrefix);
        switch (request) {
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
                if (arg.get("delKey") != null) {
                    queryEngine.deleteEntireApp(arg.getString("delKey"));
                } else {
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
                CommitSizeQuery.createCommitSize(queryEngine).execute(details);
                break;
            case "SIZE":
                CommitSizeQuery.createCommitSize(queryEngine).execute(details);
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
                logger.info("Executing custom request");
                queryEngine.executeRequest(request);
        }
        queryEngine.shutDown();
        logger.info("Done");
    }


    public static void addLibrary(PaprikaApp paprikaApp, String libraryString) {
        PaprikaLibrary.createPaprikaLibrary(libraryString, paprikaApp);
    }


}
