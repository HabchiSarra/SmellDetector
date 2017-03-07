package analyzer;
import entities.PaprikaArgument;
import entities.PaprikaMethod;
import entities.PaprikaModifiers;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Created by sarra on 20/02/17.
 */
public class MethodProcessor  {
    public void process(CtMethod ctMethod) {

        String name =ctMethod.getSimpleName();
        String returnType= ctMethod.getType().getQualifiedName();

        String visibility = ctMethod.getVisibility() == null ? "null"  : ctMethod.getVisibility().toString();
        PaprikaModifiers paprikaModifiers=DataConverter.convertTextToModifier(visibility);
        if(paprikaModifiers==null){
            paprikaModifiers=PaprikaModifiers.PROTECTED;
        }
        int position = 0;
        String qualifiedName;
        PaprikaMethod paprikaMethod = PaprikaMethod.createPaprikaMethod(name, paprikaModifiers, returnType, MainProcessor.currentClass);
        MainProcessor.currentMethod=paprikaMethod;
        for (CtParameter<?> ctParameter : (List<CtParameter>)ctMethod.getParameters()) {
             qualifiedName = ctParameter.getType().getQualifiedName();
            PaprikaArgument.createPaprikaArgument(qualifiedName,position,paprikaMethod);
             position++;
        }
        int numberOfDeclaredLocals = ctMethod.getElements(new TypeFilter<CtLocalVariable>(CtLocalVariable.class)).size();
        paprikaMethod.setNumberOfLines(ctMethod.getPosition().getSourceEnd()-ctMethod.getPosition().getSourceStart());

        handleUsedVariables(ctMethod,paprikaMethod);
        handleInvocations(ctMethod,paprikaMethod);
        paprikaMethod.setComplexity(getComplexity(ctMethod));
        paprikaMethod.setNumberOfDeclaredLocals(numberOfDeclaredLocals);

    }

    private void handleUsedVariables(CtMethod ctMethod, PaprikaMethod paprikaMethod){
        List<CtFieldAccess> elements = ctMethod.getElements(new TypeFilter<CtFieldAccess>(CtFieldAccess.class));
        String variableTarget =null;
        String variableName;
        for (CtFieldAccess ctFieldAccess : elements) {
            if(ctFieldAccess.getTarget()!= null && ctFieldAccess.getTarget().getType()!=null)
            {
                variableTarget=ctFieldAccess.getTarget().getType().getQualifiedName();
            }
            variableName=ctFieldAccess.getVariable().getSimpleName();
            paprikaMethod.getUsedVariablesData().add(new VariableData(variableTarget, variableName));
        }

    }

    private void handleInvocations(CtMethod ctMethod, PaprikaMethod paprikaMethod){
        String targetName;
        String executable;
        List<CtInvocation> invocations = ctMethod.getElements(new TypeFilter<CtInvocation>(CtInvocation.class));
        for (CtInvocation invocation : invocations) {
            targetName=getTarget(invocation);
            executable = invocation.getExecutable().getSimpleName();
            if(targetName!=null)
            {
                paprikaMethod.getInvocationData().add(new InvocationData(targetName,executable));
            }
        }
    }

    private String getTarget(CtInvocation ctInvocation){
        if(ctInvocation.getTarget() instanceof CtInvocation){
            return getTarget((CtInvocation) ctInvocation.getTarget());
        }else{
            try{
                return ctInvocation.getExecutable().getDeclaringType().getQualifiedName();
            }catch (NullPointerException nullPointerException){
                nullPointerException.printStackTrace();
            }
        }
        return null;
    }

    private int getComplexity(CtMethod<?> ctMethod){
        int numberOfTernaries = ctMethod.getElements(new TypeFilter<CtConditional>(CtConditional.class)).size();
        int numberOfIfs =ctMethod.getElements(new TypeFilter<CtIf>(CtIf.class)).size();
        int numberOfCases = ctMethod.getElements(new TypeFilter<CtCase>(CtCase.class)).size();
        int numberOfReturns = ctMethod.getElements(new TypeFilter<CtReturn>(CtReturn.class)).size();
        int numberOfLoops = ctMethod.getElements(new TypeFilter<CtLoop>(CtLoop.class)).size();
        int numberOfBinaryOperators = ctMethod.getElements(new TypeFilter<CtBinaryOperator>(CtBinaryOperator.class){
            private final List<BinaryOperatorKind> operators = Arrays.asList(BinaryOperatorKind.AND, BinaryOperatorKind.OR);
            @Override
            public boolean matches(CtBinaryOperator element) {
                return super.matches(element)&& operators.contains(element.getKind());
            }
        }).size();
        int numberOfCatches = ctMethod.getElements(new TypeFilter<CtCatch>(CtCatch.class)).size();
        int numberOfThrows = ctMethod.getElements(new TypeFilter<CtThrow>(CtThrow.class)).size();
        int numberOfBreaks = ctMethod.getElements(new TypeFilter<CtBreak>(CtBreak.class)).size();
        int numberOfContinues = ctMethod.getElements(new TypeFilter<CtContinue>(CtContinue.class)).size();
        return numberOfBreaks+numberOfCases+numberOfCatches+numberOfContinues+numberOfIfs+numberOfLoops+
                numberOfReturns+numberOfTernaries+numberOfThrows+numberOfBinaryOperators+1;
    }
}
