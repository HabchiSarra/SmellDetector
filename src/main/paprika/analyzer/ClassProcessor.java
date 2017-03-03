package analyzer;

import entities.PaprikaClass;
import entities.PaprikaModifiers;
import entities.PaprikaVariable;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;


/**
 * Created by sarra on 17/02/17.
 */
public class ClassProcessor extends AbstractProcessor<CtClass> {


    private static final URLClassLoader classloader; static {
        classloader = new URLClassLoader( MainProcessor.paths.toArray(new URL[MainProcessor.paths.size()]));
    }


    public void process(CtClass ctClass) {
        String qualifiedName = ctClass.getQualifiedName();
        if (ctClass.isAnonymous()) {
            String[] splitName = qualifiedName.split("\\$");
            qualifiedName = splitName[0] + "$" +
                    ((CtNewClass)ctClass.getParent()).getType().getQualifiedName() + splitName[1] ;
        }
        String visibility = ctClass.getVisibility() == null ? "null"  : ctClass.getVisibility().toString();
        PaprikaModifiers paprikaModifiers=DataConverter.convertTextToModifier(visibility);
        if(paprikaModifiers==null){
            paprikaModifiers=PaprikaModifiers.PROTECTED;
        }
        PaprikaClass paprikaClass =PaprikaClass.createPaprikaClass(qualifiedName,MainProcessor.currentApp,paprikaModifiers);
        MainProcessor.currentClass=paprikaClass;
        handleProperties(ctClass,paprikaClass);
        handleAttachments(ctClass,paprikaClass);



    }

    public void handleAttachments(CtClass ctClass, PaprikaClass paprikaClass){
        if(ctClass.getSuperclass()!=null)
        {
            paprikaClass.setParentName(ctClass.getSuperclass().getQualifiedName());
        }
        for (CtTypeReference<?> ctTypeReference : ctClass.getSuperInterfaces()) {
            paprikaClass.getInterfacesNames().add(ctTypeReference.getQualifiedName());
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

    public void handleProperties(CtClass ctClass, PaprikaClass paprikaClass){
        boolean isContentProvider =false;
        boolean isAsyncTask=false;
        boolean isService=false;
        boolean isView=false;
        boolean isActivity=false;
        boolean isBroadcastReceiver=false;
        boolean isInterface = ctClass.isInterface();
        boolean isStatic = false;
        for(ModifierKind modifierKind:ctClass.getModifiers()){
            if(modifierKind.toString().toLowerCase().equals("static")){
                isStatic =true;
                break;
            }
        }

        CtType myClass = ctClass;
        boolean noSuperClass=false;
        boolean found=false;
        if(ctClass.getSuperclass()!=null){
            Class myRealClass;
            CtTypeReference reference=null;
            while(myClass != null){
                    if (myClass.getSuperclass() != null) {
                        reference= myClass.getSuperclass();
                        myClass = myClass.getSuperclass().getDeclaration();
                    } else {
                        noSuperClass=true;
                        myClass = null;
                    }
            }

            myRealClass = null;
            if(!noSuperClass) {
                try {
                    myRealClass = classloader.loadClass(reference.getQualifiedName());
                    while (myRealClass.getSuperclass() != null) {
                       if(myRealClass.getSimpleName().equals("Activity")){
                            isActivity=true;
                            break;
                        }else if(myRealClass.getSimpleName().equals("ContentProvider")){
                            isContentProvider=true;
                            break;
                        }else if(myRealClass.getSimpleName().equals("AsyncTask")){
                            isAsyncTask =true;
                            break;
                        }else if(myRealClass.getSimpleName().equals("View")){
                            isView=true;
                            break;
                        }else if(myRealClass.getSimpleName().equals("BroadcastReceiver")){
                            isBroadcastReceiver=true;
                            break;
                        }else if(myRealClass.getSimpleName().equals("Service")){
                            isService=true;
                            break;
                        }
                        myRealClass = myRealClass.getSuperclass();
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoClassDefFoundError e) {
                    e.printStackTrace();
                }
            }
            System.out.println("\n\n ------------------------------------------------- \n\n");
        }

        paprikaClass.setInterface(isInterface);
        paprikaClass.setActivity(found);
        paprikaClass.setStatic(isStatic);

    }


}
