package analyzer;

import entities.PaprikaArgument;
import entities.PaprikaMethod;
import entities.PaprikaModifiers;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Arrays;
import java.util.List;

public abstract class ExecutableProcessor<T extends CtExecutable> {
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
        if (paprikaMethod.getName().equals("getExtraHeaders")) {
            System.out.println("BLOP!");
        }
        List<CtInvocation> invocations = ctConstructor.getElements(new TypeFilter<CtInvocation>(CtInvocation.class));
        for (CtInvocation invocation : invocations) {
            targetName = getTarget(invocation);
            executable = invocation.getExecutable().getSimpleName();
            if (invocation.getExecutable().getType() != null) {
                type = invocation.getExecutable().getType().getQualifiedName();
            }
            if (targetName != null) {
                paprikaMethod.getInvocationData().add(new InvocationData(targetName, executable, type));
            }
        }
    }

    private String getTarget(CtInvocation ctInvocation) {
        if (ctInvocation.getTarget() instanceof CtInvocation) {
            return getTarget((CtInvocation) ctInvocation.getTarget());
        } else {
            try {
                return ctInvocation.getExecutable().getDeclaringType().getQualifiedName();
            } catch (NullPointerException nullPointerException) {
                System.err.println("Could not find qualified name for method: " + ctInvocation.toString() + " (" + nullPointerException.getMessage() + ")");
            }
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
