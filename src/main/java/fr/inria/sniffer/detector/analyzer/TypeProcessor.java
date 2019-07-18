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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

public abstract class TypeProcessor<T extends CtType> extends AbstractProcessor<T> {
    private static final Logger logger = LoggerFactory.getLogger(TypeProcessor.class.getName());

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
//        logger.debug("Type in process: " + ctType.getSimpleName());
//        logger.debug("Processor: " + this.getClass().getSimpleName());
//        logger.debug("Type location: " + ctType.getPosition().toString());
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
