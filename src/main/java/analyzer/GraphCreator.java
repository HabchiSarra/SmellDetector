package analyzer;

import entities.*;

import java.util.ArrayList;

/**
 * Created by sarra on 27/02/17.
 */
public class GraphCreator {

    PaprikaApp paprikaApp;

    public GraphCreator(PaprikaApp paprikaApp) {
        this.paprikaApp = paprikaApp;
    }

    public void createCallGraph() {
        Entity targetClass;
        Entity targetMethod;
        PaprikaClass paprikaClass;
        PaprikaVariable paprikaVariable;
        ArrayList<PaprikaMethod> paprikaMethods = paprikaApp.getMethods();
        for (PaprikaMethod paprikaMethod : paprikaMethods) {
            for (InvocationData invocationData : paprikaMethod.getInvocationData()) {
                targetClass = paprikaApp.getPaprikaClass(invocationData.getTarget());
                if (targetClass instanceof PaprikaClass) {
                    targetMethod = ((PaprikaClass) targetClass).getPaprikaMethod(invocationData.getMethod());
                    paprikaMethod.getPaprikaClass().coupledTo((PaprikaClass) targetClass);
                } else {

                    targetMethod = PaprikaExternalMethod.createPaprikaExternalMethod(invocationData.getMethod(), invocationData.getType(),
                            (PaprikaExternalClass) targetClass);
                }
                paprikaMethod.callMethod(targetMethod);

            }

            for (VariableData variableData : paprikaMethod.getUsedVariablesData()) {
                paprikaClass = paprikaApp.getPaprikaInternalClass(variableData.getClassName());
                if (paprikaClass != null) {
                    paprikaVariable = paprikaClass.findVariable(variableData.getVariableName());
                    if (paprikaVariable != null) {
                        paprikaMethod.useVariable(paprikaVariable);
                    }
                }
            }
        }

    }

    public void createClassHierarchy() {
        for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
            String parentName = paprikaClass.getParentName();
            PaprikaClass implementedInterface;
            if (parentName != null) {
                PaprikaClass parentClass = paprikaClass.getPaprikaApp().getPaprikaInternalClass(parentName);
                paprikaClass.setParent(parentClass);
                if (parentClass != null) {
                    parentClass.addChild();
                    paprikaClass.setParentName(null);
                }
            }
            for (String interfaceName : paprikaClass.getInterfacesNames()) {
                implementedInterface = paprikaClass.getPaprikaApp().getPaprikaInternalClass(interfaceName);
                if (implementedInterface != null) {
                    paprikaClass.implement(implementedInterface);
                }
            }

        }
    }


}
