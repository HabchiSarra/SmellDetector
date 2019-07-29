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
                    targetMethod = ((PaprikaClass) targetClass).getCalledPaprikaMethod(invocationData.getMethod());
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
