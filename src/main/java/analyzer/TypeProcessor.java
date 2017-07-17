package analyzer;

import entities.PaprikaClass;
import entities.PaprikaModifiers;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

public abstract class TypeProcessor<T extends CtType> extends AbstractProcessor<T> {
    // TODO: Holding the method in the superclass send every CtType in each class extending TypeProcessor!
//    @Override
//    public void process(T ctType) {
//        String qualifiedName = ctType.getQualifiedName();
//        if (ctType.isAnonymous()) {
//            String[] splitName = qualifiedName.split("\\$");
//            qualifiedName = splitName[0] + "$" +
//                    ((CtNewClass) ctType.getParent()).getType().getQualifiedName() + splitName[1];
//        }
//        String visibility = ctType.getVisibility() == null ? "null" : ctType.getVisibility().toString();
//        PaprikaModifiers paprikaModifiers = DataConverter.convertTextToModifier(visibility);
//        if (paprikaModifiers == null) {
//            paprikaModifiers = PaprikaModifiers.DEFAULT;
//        }
//        PaprikaClass paprikaClass = PaprikaClass.createPaprikaClass(qualifiedName, MainProcessor.currentApp, paprikaModifiers);
//        MainProcessor.currentClass = paprikaClass;
//        System.out.println("Type in process: " + ctType.getSimpleName());
//        System.out.println("Processor: " + this.getClass().getSimpleName());
//        System.out.println("Type location: " + ctType.getPosition().toString());
//        handleProperties(ctType, paprikaClass);
//        handleAttachments(ctType, paprikaClass);
//        if (ctType.getQualifiedName().contains("$")) {
//            paprikaClass.setInnerClass(true);
//        }
//        processMethods(ctType);
//    }

    protected abstract void processMethods(T ctType);

    protected abstract void handleAttachments(T ctType, PaprikaClass paprikaClass);

    protected abstract void handleProperties(T ctType, PaprikaClass paprikaClass);

    protected CtTypeReference findSuperClass(T ctType, Integer depthOfInheritance) {
        CtType myClass = ctType;
        CtTypeReference reference = null;
        while (myClass != null) {
            depthOfInheritance++;
            if (myClass.getSuperclass() != null) {
                reference = myClass.getSuperclass();
                myClass = myClass.getSuperclass().getDeclaration();
                // See https://github.com/INRIA/spoon/issues/1475
                if (reference.equals(reference.getSuperclass())) {
                    break;
                }
            } else {
                myClass = null;
            }
        }
        return reference;
    }
}
