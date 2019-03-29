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

import fr.inria.sniffer.detector.entities.PaprikaApp;
import fr.inria.sniffer.detector.entities.PaprikaClass;
import fr.inria.sniffer.detector.entities.PaprikaMethod;
import spoon.Launcher;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtInterface;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by sarra on 21/02/17.
 */
public class MainProcessor {

    static PaprikaApp currentApp;
    static PaprikaClass currentClass;
    static PaprikaMethod currentMethod;
    static ArrayList<URL> paths = new ArrayList<>();
    String appPath;
    private String jarsPath;
    private String sdkPath;

    public MainProcessor(String appName, int appVersion, int commitNumber, String status, String appKey, String appPath, String sdkPath, String jarsPath, int sdkVersion, String module) {
        this.currentApp = PaprikaApp.createPaprikaApp(appName, appVersion, commitNumber, status, appKey, appPath, sdkVersion, module);
        currentClass = null;
        currentMethod = null;
        this.appPath = appPath;
        this.jarsPath = jarsPath;
        this.sdkPath = sdkPath;
    }

    public void process() {
        Launcher launcher = new Launcher();
        launcher.addInputResource(appPath);
        launcher.getEnvironment().setNoClasspath(true);
        launcher.buildModel();
        AbstractProcessor<CtClass> classProcessor = new ClassProcessor();
        AbstractProcessor<CtInterface> interfaceProcessor = new InterfaceProcessor();
        launcher.addProcessor(classProcessor);
        launcher.addProcessor(interfaceProcessor);
        launcher.process();
    }

    private void update_classpath(Launcher launcher) {
        try {
            paths = new ArrayList<>();
            if (this.jarsPath != null) {
                File folder = new File(jarsPath);
                paths = this.listFilesForFolder(folder);
            }
            if (this.sdkPath != null) {
                paths.add(new File(sdkPath).toURI().toURL());
            }
            String[] cl = new String[paths.size()];
            for (int i = 0; i < paths.size(); i++) {
                URL url = paths.get(i);
                cl[i] = url.getPath();
            }
            launcher.getEnvironment().setSourceClasspath(cl);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private ArrayList<URL> listFilesForFolder(final File folder) throws IOException {
        ArrayList<URL> jars = new ArrayList<>();
        File[] files = folder.listFiles();
        if (files == null) {
            return jars;
        }
        for (final File fileEntry : files) {
            jars.add(fileEntry.toURI().toURL());
        }
        return jars;
    }
}
