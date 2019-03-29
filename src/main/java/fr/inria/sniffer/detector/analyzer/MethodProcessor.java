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

import fr.inria.sniffer.detector.entities.PaprikaMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;

/**
 * Created by sarra on 20/02/17.
 */
public class MethodProcessor extends ExecutableProcessor<CtMethod> {
    private static final Logger logger = LoggerFactory.getLogger(MethodProcessor.class.getName());

    @Override
    protected void process(CtMethod ctMethod, PaprikaMethod paprikaMethod) {
        paprikaMethod.setSetter(checkSetter(ctMethod));
        paprikaMethod.setGetter(checkGetter(ctMethod));
        for (ModifierKind modifierKind : ctMethod.getModifiers()) {
            if (modifierKind.toString().toLowerCase().equals("static")) {
                paprikaMethod.setStatic(true);
                break;
            }
        }
    }

    private boolean checkGetter(CtMethod element) {
        if (element.getBody() == null) {
            return false;
        }
        if (element.getBody().getStatements().size() != 1) {
            return false;
        }
        CtStatement statement = element.getBody().getStatement(0);
        if (!(statement instanceof CtReturn)) {
            return false;
        }

        CtReturn retur = (CtReturn) statement;
        if (!(retur.getReturnedExpression() instanceof CtFieldRead)) {
            return false;
        }
        CtFieldRead returnedExpression = (CtFieldRead) retur.getReturnedExpression();

        CtType parent = element.getParent(CtType.class);
        if (parent == null) {
            return false;
        }
        try {
            if (parent.equals(returnedExpression.getVariable().getDeclaration().getDeclaringType())) {
                return true;
            }
        } catch (NullPointerException npe) {
            logger.warn("Could not find declaring type for getter: " + returnedExpression.getVariable().toString() + " (" + npe.getMessage() + ")");
        }
        return false;

    }

    private boolean checkSetter(CtMethod element) {
        if (element.getBody() == null) {
            return false;
        }
        if (element.getBody().getStatements().size() != 1) {
            return false;
        }
        if (element.getParameters().size() != 1) {
            return false;
        }
        CtStatement statement = element.getBody().getStatement(0);
        // the last statement is an assignment
        if (!(statement instanceof CtAssignment)) {
            return false;
        }

        CtAssignment ctAssignment = (CtAssignment) statement;
        if (!(ctAssignment.getAssigned() instanceof CtFieldWrite)) {
            return false;
        }
        if (!(ctAssignment.getAssignment() instanceof CtVariableRead)) {
            return false;
        }
        CtVariableRead ctVariableRead = (CtVariableRead) ctAssignment.getAssignment();
        if (element.getParameters().size() != 1) {
            return false;
        }
        try {
            if ((ctVariableRead.getVariable().getDeclaration()!=null) && !(ctVariableRead.getVariable().getDeclaration().equals(element.getParameters().get(0)))) {
                return false;
            }
        }catch (NullPointerException npe){
            System.out.println(npe.getCause());
        }
        CtFieldWrite returnedExpression = (CtFieldWrite) ((CtAssignment) statement).getAssigned();
        if (returnedExpression.getTarget() instanceof CtThisAccess) {
            return true;
        }

        return false;
    }
}
