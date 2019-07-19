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

import fr.inria.sniffer.detector.entities.PaprikaClass;
import fr.inria.sniffer.detector.entities.PaprikaModifiers;
import fr.inria.sniffer.detector.entities.PaprikaVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.declaration.CtInterfaceImpl;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;


/**
 * Created by sarra on 17/02/17.
 */
public class ClassProcessor extends TypeProcessor<CtClass> {
    private static final Logger logger = LoggerFactory.getLogger(ClassProcessor.class.getName());
    private static final URLClassLoader classloader;

    @Override
    public void process(CtClass ctType) {
        super.process(ctType);
    }

    static {
        if (MainProcessor.paths == null) {
            classloader = new URLClassLoader(new URL[0]);
        } else {
            classloader = new URLClassLoader(MainProcessor.paths.toArray(new URL[MainProcessor.paths.size()]));
        }
    }

    @Override
    public boolean isToBeProcessed(CtClass candidate) {
        return super.isToBeProcessed(candidate) && !(candidate instanceof CtInterfaceImpl);
    }

    @Override
    public void processMethods(CtClass ctClass) {
        MethodProcessor methodProcessor = new MethodProcessor();
        ConstructorProcessor constructorProcessor = new ConstructorProcessor();
        for (Object o : ctClass.getMethods()) {
            methodProcessor.process((CtMethod) o);
        }
        CtConstructor ctConstructor;
        for (Object o : ctClass.getConstructors()) {
            ctConstructor = (CtConstructor) o;
            constructorProcessor.process(ctConstructor);
        }

    }

    @Override
    public void handleAttachments(CtClass ctClass, PaprikaClass paprikaClass) {
        if (ctClass.getSuperclass() != null) {
            paprikaClass.setParentName(ctClass.getSuperclass().getQualifiedName());
        }
        for (CtTypeReference<?> ctTypeReference : ctClass.getSuperInterfaces()) {
            paprikaClass.getInterfacesNames().add(ctTypeReference.getQualifiedName());
        }
        String modifierText;
        PaprikaVariable paprikaVariable;
        PaprikaModifiers paprikaModifiers1;
        boolean isStatic;
        for (CtField<?> ctField : (List<CtField>) ctClass.getFields()) {
            modifierText = ctField.getVisibility() == null ? "null" : ctField.getVisibility().toString();
            paprikaModifiers1 = DataConverter.convertTextToModifier(modifierText);
            if (paprikaModifiers1 == null) {
                paprikaModifiers1 = PaprikaModifiers.DEFAULT;
            }
            paprikaVariable = PaprikaVariable.createPaprikaVariable(ctField.getSimpleName(), ctField.getType().getQualifiedName(), paprikaModifiers1, paprikaClass);
            isStatic = false;
            for (ModifierKind modifierKind : ctField.getModifiers()) {
                if (modifierKind.toString().toLowerCase().equals("static")) {
                    isStatic = true;
                    break;
                }
            }
            paprikaVariable.setStatic(isStatic);
        }

    }

    @Override
    public void handleProperties(CtClass ctClass, PaprikaClass paprikaClass) {
        Integer doi = 0;
        boolean isApplication = false;
        boolean isContentProvider = false;
        boolean isAsyncTask = false;
        boolean isService = false;
        boolean isView = false;
        boolean isActivity = false;
        boolean isBroadcastReceiver = false;
        boolean isInterface = ctClass.isInterface();
        boolean isStatic = false;
        for (ModifierKind modifierKind : ctClass.getModifiers()) {
            if (modifierKind.toString().toLowerCase().equals("static")) {
                isStatic = true;
                break;
            }
        }

        CtTypeReference reference = findSuperClass(ctClass, doi);

        if (reference != null) {
            try {
                Class myRealClass = classloader.loadClass(reference.getQualifiedName());
                while (myRealClass.getSuperclass() != null) {
                    doi++;
                    if (myRealClass.getSimpleName().endsWith("Activity")) {
                        isActivity = true;
                        break;
                    } else if (myRealClass.getSimpleName().endsWith("ContentProvider")) {
                        isContentProvider = true;
                        break;
                    } else if (myRealClass.getSimpleName().endsWith("AsyncTask")) {
                        isAsyncTask = true;
                        break;
                    } else if (myRealClass.getSimpleName().endsWith("View")) {
                        isView = true;
                        break;
                    } else if (myRealClass.getSimpleName().endsWith("BroadcastReceiver")) {
                        isBroadcastReceiver = true;
                        break;
                    } else if (myRealClass.getSimpleName().endsWith("Service")) {
                        isService = true;
                        break;
                    } else if (myRealClass.getSimpleName().endsWith("Application")) {
                        isApplication = true;
                        break;
                    }
                    myRealClass = myRealClass.getSuperclass();
                }
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                logger.warn("Class Not Found or Definition Not Found ; message : " + e.getLocalizedMessage());
                logger.debug("switching to heuristic mode");
                if (ctClass.getSimpleName().endsWith("Activity")||ctClass.getSuperclass().getQualifiedName().endsWith("Activity")) {
                    isActivity = true;
                } else if (ctClass.getSimpleName().endsWith("ContentProvider")||ctClass.getSuperclass().getQualifiedName().endsWith("ContentProvider")) {
                    isContentProvider = true;
                } else if (ctClass.getSimpleName().endsWith("AsyncTask")||ctClass.getSuperclass().getQualifiedName().endsWith("AsyncTask")) {
                    isAsyncTask = true;
                } else if (ctClass.getSimpleName().endsWith("View")||ctClass.getSuperclass().getQualifiedName().endsWith("View")) {
                    isView = true;
                } else if (ctClass.getSimpleName().endsWith("BroadcastReceiver")||ctClass.getSuperclass().getQualifiedName().endsWith("BroadcastReceiver")) {
                    isBroadcastReceiver = true;
                } else if (ctClass.getSimpleName().endsWith("Service")||ctClass.getSuperclass().getQualifiedName().endsWith("Service")) {
                    isService = true;
                } else if (ctClass.getSimpleName().endsWith("Application")||ctClass.getSuperclass().getQualifiedName().endsWith("Application")) {
                    isApplication = true;
                }
            }
        }

        paprikaClass.setInterface(isInterface);
        paprikaClass.setActivity(isActivity);
        paprikaClass.setStatic(isStatic);
        paprikaClass.setAsyncTask(isAsyncTask);
        paprikaClass.setContentProvider(isContentProvider);
        paprikaClass.setBroadcastReceiver(isBroadcastReceiver);
        paprikaClass.setService(isService);
        paprikaClass.setView(isView);
        paprikaClass.setApplication(isApplication);
        paprikaClass.setDepthOfInheritance(doi);
    }


}
