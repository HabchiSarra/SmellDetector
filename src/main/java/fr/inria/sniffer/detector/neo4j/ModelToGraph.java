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
package fr.inria.sniffer.detector.neo4j;

import fr.inria.sniffer.detector.entities.Entity;
import fr.inria.sniffer.detector.entities.PaprikaApp;
import fr.inria.sniffer.detector.entities.PaprikaArgument;
import fr.inria.sniffer.detector.entities.PaprikaClass;
import fr.inria.sniffer.detector.entities.PaprikaExternalArgument;
import fr.inria.sniffer.detector.entities.PaprikaExternalClass;
import fr.inria.sniffer.detector.entities.PaprikaExternalMethod;
import fr.inria.sniffer.detector.entities.PaprikaLibrary;
import fr.inria.sniffer.detector.entities.PaprikaMethod;
import fr.inria.sniffer.detector.entities.PaprikaVariable;
import fr.inria.sniffer.detector.metrics.Metric;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Geoffrey Hecht on 05/06/14.
 */
public class ModelToGraph {
    private static final Logger logger = LoggerFactory.getLogger(ModelToGraph.class.getName());

    private GraphDatabaseService graphDatabaseService;
    private DatabaseManager databaseManager;
    private static final Label appLabel = DynamicLabel.label("App");
    private static final Label classLabel = DynamicLabel.label("Class");
    private static final Label externalClassLabel = DynamicLabel.label("ExternalClass");
    private static final Label methodLabel = DynamicLabel.label("Method");
    private static final Label externalMethodLabel = DynamicLabel.label("ExternalMethod");
    private static final Label variableLabel = DynamicLabel.label("Variable");
    private static final Label argumentLabel = DynamicLabel.label("Argument");
    private static final Label externalArgumentLabel = DynamicLabel.label("ExternalArgument");
    private static final Label libraryLabel = DynamicLabel.label("Library");

    private Map<Entity, Node> methodNodeMap;
    private Map<PaprikaClass, Node> classNodeMap;
    private Map<PaprikaVariable, Node> variableNodeMap;

    private String key;
    private String appName;

    public ModelToGraph(String DatabasePath) {
        this.databaseManager = new DatabaseManager(DatabasePath);
        databaseManager.start();
        this.graphDatabaseService = databaseManager.getGraphDatabaseService();
        methodNodeMap = new HashMap<>();
        classNodeMap = new HashMap<>();
        variableNodeMap = new HashMap<>();
        IndexManager indexManager = new IndexManager(graphDatabaseService);
        indexManager.createIndex();
    }

    public Node insertApp(PaprikaApp paprikaApp) {
        this.key = paprikaApp.getKey();
        this.appName = paprikaApp.getName();
        Node appNode;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            appNode = graphDatabaseService.createNode(appLabel);
            appNode.setProperty("app_key", key);
            appNode.setProperty("name", appName);
            //appNode.setProperty("version", paprikaApp.getVersion());
            appNode.setProperty("commit_number", paprikaApp.getCommitNumber());
            //appNode.setProperty("commit_status", paprikaApp.getStatus());
            //appNode.setProperty("sdk_version", paprikaApp.getSdkVersion());
            //appNode.setProperty("analyzed_module", paprikaApp.getModule());
            Date date = new Date();
            SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
            appNode.setProperty("date_analysis", simpleFormat.format(date));
            //appNode.setProperty("path", paprikaApp.getPath());

            Node classNode;
            for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
                classNode = insertClass(paprikaClass);
                appNode.createRelationshipTo(classNode, RelationTypes.APP_OWNS_CLASS);
            }
            for (PaprikaExternalClass paprikaExternalClass : paprikaApp.getPaprikaExternalClasses()) {
                insertExternalClass(paprikaExternalClass);
            }
            for (Metric metric : paprikaApp.getMetrics()) {
                insertMetric(metric, appNode);
            }

            for (PaprikaLibrary paprikaLibrary : paprikaApp.getPaprikaLibraries()) {
                appNode.createRelationshipTo(insertLibrary(paprikaLibrary), RelationTypes.APP_USES_LIBRARY);
            }
            tx.success();
        }
        try (Transaction tx = graphDatabaseService.beginTx()) {
            createHierarchy(paprikaApp);
            createCallGraph(paprikaApp);
            tx.success();
        }
        return appNode;
    }

    private void insertMetric(Metric metric, Node node) {
        node.setProperty(metric.getName(), metric.getValue());
    }


    public Node insertClass(PaprikaClass paprikaClass) {
        Node classNode = graphDatabaseService.createNode(classLabel);
        classNodeMap.put(paprikaClass, classNode);
        classNode.setProperty("app_key", key);
        classNode.setProperty("name", paprikaClass.getName());
        classNode.setProperty("modifier", paprikaClass.getModifier().toString().toLowerCase());
        classNode.setProperty("file_path", paprikaClass.getPath());
        classNode.setProperty("app_name", appName);
        if (paprikaClass.getParentName() != null) {
            classNode.setProperty("parent_name", paprikaClass.getParentName());
        }
        for (PaprikaVariable paprikaVariable : paprikaClass.getPaprikaVariables()) {
            classNode.createRelationshipTo(insertVariable(paprikaVariable), RelationTypes.CLASS_OWNS_VARIABLE);

        }
        for (PaprikaMethod paprikaMethod : paprikaClass.getPaprikaMethods()) {
            classNode.createRelationshipTo(insertMethod(paprikaMethod), RelationTypes.CLASS_OWNS_METHOD);
        }
        for (Metric metric : paprikaClass.getMetrics()) {
            insertMetric(metric, classNode);
        }
        return classNode;
    }

    public Node insertLibrary(PaprikaLibrary paprikaLibrary) {
        Node libraryNode = graphDatabaseService.createNode(libraryLabel);
        libraryNode.setProperty("app_key", key);
        libraryNode.setProperty("name", paprikaLibrary.getName());
        libraryNode.setProperty("app_name", appName);
        return libraryNode;
    }

    public Node insertExternalClass(PaprikaExternalClass paprikaClass) {
        Node classNode = graphDatabaseService.createNode(externalClassLabel);
        classNode.setProperty("app_key", key);
        classNode.setProperty("name", paprikaClass.getName());
        classNode.setProperty("app_name", appName);
        if (paprikaClass.getParentName() != null) {
            classNode.setProperty("parent_name", paprikaClass.getParentName());
        }
        for (PaprikaExternalMethod paprikaExternalMethod : paprikaClass.getPaprikaExternalMethods()) {
            classNode.createRelationshipTo(insertExternalMethod(paprikaExternalMethod), RelationTypes.CLASS_OWNS_METHOD);
        }
        for (Metric metric : paprikaClass.getMetrics()) {
            insertMetric(metric, classNode);
        }
        return classNode;
    }

    public Node insertVariable(PaprikaVariable paprikaVariable) {
        Node variableNode = graphDatabaseService.createNode(variableLabel);
        variableNodeMap.put(paprikaVariable, variableNode);
        variableNode.setProperty("app_key", key);
        variableNode.setProperty("name", paprikaVariable.getName());
        variableNode.setProperty("modifier", paprikaVariable.getModifier().toString().toLowerCase());
        variableNode.setProperty("type", paprikaVariable.getType());
        variableNode.setProperty("app_name", appName);
        for (Metric metric : paprikaVariable.getMetrics()) {
            insertMetric(metric, variableNode);
        }
        return variableNode;
    }

    public Node insertMethod(PaprikaMethod paprikaMethod) {
        Node methodNode = graphDatabaseService.createNode(methodLabel);
        methodNodeMap.put(paprikaMethod, methodNode);
        methodNode.setProperty("app_key", key);
        methodNode.setProperty("name", paprikaMethod.getName());
        methodNode.setProperty("modifier", paprikaMethod.getModifier().toString().toLowerCase());
        methodNode.setProperty("full_name", paprikaMethod.toString());
        methodNode.setProperty("app_name", appName);
        methodNode.setProperty("return_type", paprikaMethod.getReturnType());

        for (Metric metric : paprikaMethod.getMetrics()) {
            insertMetric(metric, methodNode);
        }
        Node variableNode;
        for (PaprikaVariable paprikaVariable : paprikaMethod.getUsedVariables()) {
            variableNode = variableNodeMap.get(paprikaVariable);
            if (variableNode != null) {
                methodNode.createRelationshipTo(variableNode, RelationTypes.USES);
            } else {
                logger.warn("problem");
            }

        }
        for (PaprikaArgument arg : paprikaMethod.getArguments()) {
            methodNode.createRelationshipTo(insertArgument(arg), RelationTypes.METHOD_OWNS_ARGUMENT);
        }
        return methodNode;
    }

    public Node insertExternalMethod(PaprikaExternalMethod paprikaMethod) {
        Node methodNode = graphDatabaseService.createNode(externalMethodLabel);
        methodNodeMap.put(paprikaMethod, methodNode);
        methodNode.setProperty("app_key", key);
        methodNode.setProperty("name", paprikaMethod.getName());
        methodNode.setProperty("full_name", paprikaMethod.toString());
        methodNode.setProperty("return_type", paprikaMethod.getReturnType());
        methodNode.setProperty("app_name", appName);
        for (Metric metric : paprikaMethod.getMetrics()) {
            insertMetric(metric, methodNode);
        }
        for (PaprikaExternalArgument arg : paprikaMethod.getPaprikaExternalArguments()) {
            methodNode.createRelationshipTo(insertExternalArgument(arg), RelationTypes.METHOD_OWNS_ARGUMENT);
        }
        return methodNode;
    }

    public Node insertArgument(PaprikaArgument paprikaArgument) {
        Node argNode = graphDatabaseService.createNode(argumentLabel);
        argNode.setProperty("app_key", key);
        argNode.setProperty("name", paprikaArgument.getName());
        argNode.setProperty("position", paprikaArgument.getPosition());
        argNode.setProperty("app_name", appName);
        return argNode;
    }

    public Node insertExternalArgument(PaprikaExternalArgument paprikaExternalArgument) {
        Node argNode = graphDatabaseService.createNode(externalArgumentLabel);
        argNode.setProperty("app_key", key);
        argNode.setProperty("name", paprikaExternalArgument.getName());
        argNode.setProperty("position", paprikaExternalArgument.getPosition());
        argNode.setProperty("app_name", appName);
        for (Metric metric : paprikaExternalArgument.getMetrics()) {
            insertMetric(metric, argNode);
        }
        return argNode;
    }

    public void createHierarchy(PaprikaApp paprikaApp) {
        for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
            PaprikaClass parent = paprikaClass.getParent();
            if (parent != null) {
                classNodeMap.get(paprikaClass).createRelationshipTo(classNodeMap.get(parent), RelationTypes.EXTENDS);
            }
            for (PaprikaClass pInterface : paprikaClass.getInterfaces()) {
                classNodeMap.get(paprikaClass).createRelationshipTo(classNodeMap.get(pInterface), RelationTypes.IMPLEMENTS);
            }
        }
    }

    public void createCallGraph(PaprikaApp paprikaApp) {
        for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
            for (PaprikaMethod paprikaMethod : paprikaClass.getPaprikaMethods()) {
                for (Entity calledMethod : paprikaMethod.getCalledMethods()) {
                    methodNodeMap.get(paprikaMethod).createRelationshipTo(methodNodeMap.get(calledMethod), RelationTypes.CALLS);
                }
            }
        }
    }
}
