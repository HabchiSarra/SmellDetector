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

import fr.inria.sniffer.detector.entities.PaprikaArgument;
import fr.inria.sniffer.detector.entities.PaprikaMethod;
import fr.inria.sniffer.detector.entities.PaprikaModifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtInvocationImpl;

import java.util.Arrays;
import java.util.List;

public abstract class ExecutableProcessor<T extends CtExecutable> {
    private static final Logger logger = LoggerFactory.getLogger(ExecutableProcessor.class.getName());

    public void process(T ctExecutable) {
        String name = ctExecutable.getSimpleName();
        String returnType = ctExecutable.getType().getQualifiedName();

        String visibility = "null";
        if (ctExecutable instanceof CtModifiable) {
            CtModifiable modifiable = (CtModifiable) ctExecutable;
            visibility = modifiable.getVisibility() == null ? "null" : modifiable.getVisibility().toString();
        }
        PaprikaModifiers paprikaModifiers = DataConverter.convertTextToModifier(visibility);
        if (paprikaModifiers == null) {
            paprikaModifiers = PaprikaModifiers.PROTECTED;
        }
        int position = 0;
        String qualifiedName;
        PaprikaMethod paprikaMethod = PaprikaMethod.createPaprikaMethod(name, paprikaModifiers, returnType,
                MainProcessor.currentClass);
        MainProcessor.currentMethod = paprikaMethod;
        for (CtParameter<?> ctParameter : (List<CtParameter>) ctExecutable.getParameters()) {
            qualifiedName = ctParameter.getType().getQualifiedName();
            PaprikaArgument.createPaprikaArgument(qualifiedName, position, paprikaMethod);
            position++;
        }
        int numberOfDeclaredLocals = ctExecutable.getElements(new TypeFilter<CtLocalVariable>(CtLocalVariable.class)).size();
        paprikaMethod.setNumberOfLines(countEffectiveCodeLines(ctExecutable));
        handleUsedVariables(ctExecutable, paprikaMethod);
        handleInvocations(ctExecutable, paprikaMethod);
        paprikaMethod.setComplexity(getComplexity(ctExecutable));
        paprikaMethod.setNumberOfDeclaredLocals(numberOfDeclaredLocals);

        process(ctExecutable, paprikaMethod);
    }

    /**
     * Define the process behavior specific to the current element.
     *
     * @param ctExecutable  The processed spoon executable.
     * @param paprikaMethod The processed output so far.
     */
    protected abstract void process(T ctExecutable, PaprikaMethod paprikaMethod);

    private int countEffectiveCodeLines(T ctMethod) {
        try {
            return ctMethod.getBody().toString().split("\n").length;
        } catch (NullPointerException npe) {
            return ctMethod.getPosition().getEndLine() - ctMethod.getPosition().getLine();
        }
    }

    private void handleUsedVariables(T ctExecutable, PaprikaMethod paprikaMethod) {
        List<CtFieldAccess> elements = ctExecutable.getElements(new TypeFilter<CtFieldAccess>(CtFieldAccess.class));
        String variableTarget = null;
        String variableName;

        CtTypeMember member = ctExecutable instanceof CtTypeMember ? (CtTypeMember) ctExecutable : null;
        for (CtFieldAccess ctFieldAccess : elements) {
            if (ctFieldAccess.getTarget() != null && ctFieldAccess.getTarget().getType() != null) {
                if (member != null && ctFieldAccess.getTarget().getType().getDeclaration() == member.getDeclaringType()) {
                    variableTarget = ctFieldAccess.getTarget().getType().getQualifiedName();
                    variableName = ctFieldAccess.getVariable().getSimpleName();
                    paprikaMethod.getUsedVariablesData().add(new VariableData(variableTarget, variableName));
                }
            }
        }
    }

    private void handleInvocations(T ctConstructor, PaprikaMethod paprikaMethod) {
        String targetName;
        String executable;
        String type = "Unknown";
        // Thanks to spoon we have to use a CtAbstractInvocation
        List<CtAbstractInvocation> invocations = ctConstructor.getElements(new TypeFilter<>(CtAbstractInvocation.class));
        for (CtAbstractInvocation invocation : invocations) {
            executable = invocation.getExecutable().getSimpleName();
            targetName = getTarget(invocation);
            if (invocation.getExecutable().getType() != null) {
                type = invocation.getExecutable().getType().getQualifiedName();
            }
            if (targetName != null) {
                paprikaMethod.getInvocationData().add(new InvocationData(targetName, executable, type));
            }
        }
    }

    private String getTarget(CtAbstractInvocation ctInvocation) {
        try {
            return ctInvocation.getExecutable().getDeclaringType().getQualifiedName();
        } catch (NullPointerException nullPointerException) {
            logger.warn("Could not find qualified name for method call: " + ctInvocation.toString() + " (" + nullPointerException.getMessage() + ")");
        }
        return null;
    }

    private int getComplexity(T ctConstructor) {
        int numberOfTernaries = ctConstructor.getElements(new TypeFilter<CtConditional>(CtConditional.class)).size();
        int numberOfIfs = ctConstructor.getElements(new TypeFilter<CtIf>(CtIf.class)).size();
        int numberOfCases = ctConstructor.getElements(new TypeFilter<CtCase>(CtCase.class)).size();
        int numberOfReturns = ctConstructor.getElements(new TypeFilter<CtReturn>(CtReturn.class)).size();
        int numberOfLoops = ctConstructor.getElements(new TypeFilter<CtLoop>(CtLoop.class)).size();
        int numberOfBinaryOperators = ctConstructor.getElements(new TypeFilter<CtBinaryOperator>(CtBinaryOperator.class) {
            private final List<BinaryOperatorKind> operators = Arrays.asList(BinaryOperatorKind.AND, BinaryOperatorKind.OR);

            @Override
            public boolean matches(CtBinaryOperator element) {
                return super.matches(element) && operators.contains(element.getKind());
            }
        }).size();
        int numberOfCatches = ctConstructor.getElements(new TypeFilter<CtCatch>(CtCatch.class)).size();
        int numberOfThrows = ctConstructor.getElements(new TypeFilter<CtThrow>(CtThrow.class)).size();
        int numberOfBreaks = ctConstructor.getElements(new TypeFilter<CtBreak>(CtBreak.class)).size();
        int numberOfContinues = ctConstructor.getElements(new TypeFilter<CtContinue>(CtContinue.class)).size();
        return numberOfBreaks + numberOfCases + numberOfCatches + numberOfContinues + numberOfIfs + numberOfLoops +
                numberOfReturns + numberOfTernaries + numberOfThrows + numberOfBinaryOperators + 1;
    }
}
