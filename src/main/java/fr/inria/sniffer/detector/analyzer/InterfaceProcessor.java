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
import spoon.reflect.code.CtNewClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterfaceProcessor extends TypeProcessor<CtInterface> {
    private static final Logger logger = LoggerFactory.getLogger(InterfaceProcessor.class.getName());

    private static final URLClassLoader classloader;

    static {
        classloader = new URLClassLoader(MainProcessor.paths.toArray(new URL[MainProcessor.paths.size()]));
    }

    @Override
    public void process(CtInterface ctType) {
        String qualifiedName = ctType.getQualifiedName();
        String absolutePath= ctType.getPosition().getFile().getAbsolutePath();
        String relativePath = absolutePath.replaceFirst(MainProcessor.currentApp.getPath(),"");
        if (ctType.isAnonymous()) {
            String[] splitName = qualifiedName.split("\\$");
            qualifiedName = splitName[0] + "$" +
                    ((CtNewClass) ctType.getParent()).getType().getQualifiedName() + splitName[1];
        }
        String visibility = ctType.getVisibility() == null ? "null" : ctType.getVisibility().toString();
        PaprikaModifiers paprikaModifiers = DataConverter.convertTextToModifier(visibility);
        if (paprikaModifiers == null) {
            paprikaModifiers = PaprikaModifiers.DEFAULT;
        }
        PaprikaClass paprikaClass = PaprikaClass.createPaprikaClass(qualifiedName, MainProcessor.currentApp, paprikaModifiers,relativePath);
        MainProcessor.currentClass = paprikaClass;
        handleProperties(ctType, paprikaClass);
        handleAttachments(ctType, paprikaClass);
        if (ctType.getQualifiedName().contains("$")) {
            paprikaClass.setInnerClass(true);
        }
        processMethods(ctType);    }

    @Override
    public void processMethods(CtInterface ctInterface) {
        MethodProcessor methodProcessor = new MethodProcessor();
        for (Object o : ctInterface.getMethods()) {
            methodProcessor.process((CtMethod) o);
        }
    }

    @Override
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

    @Override
    public void handleProperties(CtInterface ctInterface, PaprikaClass paprikaClass) {
        Integer doi = 0;
        boolean isStatic = false;
        for (ModifierKind modifierKind : ctInterface.getModifiers()) {
            if (modifierKind.toString().toLowerCase().equals("static")) {
                isStatic = true;
                break;
            }
        }

        CtTypeReference reference = findSuperClass(ctInterface, doi);
        if (reference != null) {
            try {
                Class myRealClass;
                myRealClass = classloader.loadClass(reference.getQualifiedName());
                while (myRealClass.getSuperclass() != null) {
                    doi++;
                    myRealClass = myRealClass.getSuperclass();
                }
            } catch (ClassNotFoundException e) {
                logger.warn("Class Not Found; message : " + e.getLocalizedMessage());
            } catch (NoClassDefFoundError e) {
                logger.warn("No Class Def Found : " + e.getLocalizedMessage());
            }
        }

        paprikaClass.setInterface(true);
        paprikaClass.setDepthOfInheritance(doi);
        paprikaClass.setStatic(isStatic);

    }
}
