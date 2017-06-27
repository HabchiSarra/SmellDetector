package analyzer;

import entities.PaprikaClass;
import entities.PaprikaModifiers;
import entities.PaprikaVariable;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * Created by sarra on 13/03/17.
 */
public class InterfaceProcessor extends AbstractProcessor<CtInterface>{

    private static final URLClassLoader classloader;

    static {
        classloader = new URLClassLoader(MainProcessor.paths.toArray(new URL[MainProcessor.paths.size()]));
    }

    @Override
    public void process(CtInterface ctInterface) {
        String qualifiedName = ctInterface.getQualifiedName();
        if (ctInterface.isAnonymous()) {
            String[] splitName = qualifiedName.split("\\$");
            qualifiedName = splitName[0] + "$" +
                    ((CtNewClass) ctInterface.getParent()).getType().getQualifiedName() + splitName[1];
        }
        String visibility = ctInterface.getVisibility() == null ? "null" : ctInterface.getVisibility().toString();
        PaprikaModifiers paprikaModifiers = DataConverter.convertTextToModifier(visibility);
        if (paprikaModifiers == null) {
            paprikaModifiers = PaprikaModifiers.DEFAULT;
        }
        PaprikaClass paprikaClass = PaprikaClass.createPaprikaClass(qualifiedName, MainProcessor.currentApp, paprikaModifiers);
        MainProcessor.currentClass = paprikaClass;
        handleProperties(ctInterface, paprikaClass);
        handleAttachments(ctInterface, paprikaClass);
        if (ctInterface.getQualifiedName().contains("$")) {
            paprikaClass.setInnerClass(true);
        }
        processMethods(ctInterface);
    }

    public void processMethods(CtInterface ctInterface) {
        MethodProcessor methodProcessor = new MethodProcessor();
        for (Object o : ctInterface.getMethods()) {
            methodProcessor.process((CtMethod) o);
        }


    }

    public void handleAttachments(CtInterface ctInterface, PaprikaClass paprikaClass) {
        if (ctInterface.getSuperclass() != null) {
            paprikaClass.setParentName(ctInterface.getSuperclass().getQualifiedName());
        }
        for (CtTypeReference<?> ctTypeReference : ctInterface.getSuperInterfaces()) {
            paprikaClass.getInterfacesNames().add(ctTypeReference.getQualifiedName());
        }
        String modifierText;
        PaprikaModifiers paprikaModifiers1;
        for (CtField<?> ctField : (List<CtField>) ctInterface.getFields()) {
            modifierText = ctField.getVisibility() == null ? "null" : ctField.getVisibility().toString();
            paprikaModifiers1 = DataConverter.convertTextToModifier(modifierText);
            if (paprikaModifiers1 == null) {
                paprikaModifiers1 = PaprikaModifiers.PROTECTED;
            }
            PaprikaVariable.createPaprikaVariable(ctField.getSimpleName(), ctField.getType().getQualifiedName(), paprikaModifiers1, paprikaClass);
        }

    }

    public void handleProperties(CtInterface ctInterface, PaprikaClass paprikaClass) {
        int doi = 0;
        boolean isStatic=false;
        for (ModifierKind modifierKind : ctInterface.getModifiers()) {
            if (modifierKind.toString().toLowerCase().equals("static")) {
                isStatic = true;
                break;
            }
        }
        CtType myClass = ctInterface;
        boolean noSuperClass = false;
        if (ctInterface.getSuperclass() != null) {
            Class myRealClass;
            CtTypeReference reference = null;
            while (myClass != null) {
                doi++;
                if (myClass.getSuperclass() != null) {
                    reference = myClass.getSuperclass();
                    myClass = myClass.getSuperclass().getDeclaration();
                } else {
                    noSuperClass = true;
                    myClass = null;
                }
            }

            if (!noSuperClass) {
                try {

                    myRealClass = classloader.loadClass(reference.getQualifiedName());
                    while (myRealClass.getSuperclass() != null) {
                        doi++;
                        myRealClass = myRealClass.getSuperclass();
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Class Not Found; message : "+ e.getLocalizedMessage());
                } catch (NoClassDefFoundError e) {
                    System.err.println("No Class Def Found : "+ e.getLocalizedMessage());
                }
            }
        }

        paprikaClass.setInterface(true);
        paprikaClass.setDepthOfInheritance(doi);
        paprikaClass.setStatic(isStatic);

    }
}
