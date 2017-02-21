package analyzer;

import entities.PaprikaClass;
import entities.PaprikaModifiers;
import entities.PaprikaVariable;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;


/**
 * Created by sarra on 17/02/17.
 */
public class ClassProcessor extends AbstractProcessor<CtClass> {

    public void process(CtClass ctClass) {
        String qualifiedName = ctClass.getQualifiedName();
        if (ctClass.isAnonymous()) {
            String[] splitName = qualifiedName.split("\\$");
            qualifiedName = splitName[0] + "$" +
                    ((CtNewClass)ctClass.getParent()).getType().getQualifiedName() + splitName[1] ;
        }

        boolean isInterface = ctClass.isInterface();
        boolean isStatic = false;
        String visibility = ctClass.getVisibility() == null ? "null"  : ctClass.getVisibility().toString();
        PaprikaModifiers paprikaModifiers=DataConverter.convertTextToModifier(visibility);
        if(paprikaModifiers==null){
            paprikaModifiers=PaprikaModifiers.PROTECTED;
        }
        for(ModifierKind modifierKind:ctClass.getModifiers()){
            if(modifierKind.toString().toLowerCase().equals("static")){
                isStatic =true;
                break;
            }
            //System.out.println(" modifier "+modifierKind);
        }

        CtType myClass;
        boolean found=false;
        if(ctClass.getSuperclass()!=null){
            myClass=ctClass.getSuperclass().getDeclaration();
            while(myClass!=null){
                if(myClass.getSimpleName().contains("Activity"))
                {
                    found=true;
                    break;
                }
                System.out.println("My class ::   "+myClass.getSimpleName());
                if(myClass.getSuperclass()!=null) {
                    myClass = myClass.getSuperclass().getDeclaration();
                }else{
                    myClass=null;
                }

            }

        }

        PaprikaClass paprikaClass =PaprikaClass.createPaprikaClass(qualifiedName,MainProcessor.currentApp,paprikaModifiers);
        paprikaClass.setInterface(isInterface);
        paprikaClass.setActivity(found);
        paprikaClass.setStatic(isStatic);
        MainProcessor.currentClass=paprikaClass;
        for (CtTypeReference<?> ctTypeReference : ctClass.getSuperInterfaces()) {
            paprikaClass.getInterfacesNames().add(ctTypeReference.getQualifiedName());
        }
        if(ctClass.getSuperclass()!=null)
        {
            paprikaClass.setParentName(ctClass.getSuperclass().getQualifiedName());
        }
        String modifierText;
        PaprikaModifiers paprikaModifiers1;
        for (CtField<?> ctField : (List<CtField>)ctClass.getFields()) {
            modifierText = ctField.getVisibility() == null ? "null"  : ctField.getVisibility().toString();
            paprikaModifiers1=DataConverter.convertTextToModifier(modifierText);
            if(paprikaModifiers1 == null){
                paprikaModifiers1=PaprikaModifiers.PROTECTED;
            }
            PaprikaVariable.createPaprikaVariable(ctField.getSimpleName(),ctField.getType().getQualifiedName(), paprikaModifiers1, paprikaClass);
        }


    }


}
