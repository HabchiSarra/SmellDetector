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
package fr.inria.sniffer.detector.metrics;
import fr.inria.sniffer.detector.entities.*;

import java.util.HashMap;
/**
 * Created by sarra on 08/03/17.
 */

public class MetricsCalculator {



        public static void calculateAppMetrics(PaprikaApp app)
        {
            NumberOfClasses.createNumberOfClasses(app, app.getPaprikaClasses().size());
            int numberOfInterfaces=0;
            int numberOfContentProviders =0 ;
            int numberOfAsyncTasks =0;
            int numberOfInnerClasses =0;
            int numberOfBroadcastReceivers =0;
            int numberOfMethods =0 ;
            int numberOfServices =0;
            int numberOfViews =0;
            int numberOfActivities =0;
            int numberOfVariables = 0;


            for(PaprikaClass c: app.getPaprikaClasses()){
                if(c.isInterface()){
                    numberOfInterfaces++;
                }
                if(c.isInnerClass()){
                    numberOfInnerClasses++;
                }
                if (c.isActivity()){
                    numberOfActivities++;
                }else if (c.isBroadcastReceiver()){
                    numberOfBroadcastReceivers++;
                }else if (c.isContentProvider()){
                    numberOfContentProviders++;
                }else if (c.isService()){
                    numberOfServices++;
                }else if (c.isView()){
                    numberOfViews++;
                }else if (c.isAsyncTask()){
                    numberOfAsyncTasks++;
                }
                numberOfVariables += c.getPaprikaVariables().size();
                numberOfMethods += c.getPaprikaMethods().size();
            }
            NumberOfInterfaces.createNumberOfInterfaces(app,numberOfInterfaces);
            NumberOfActivities.createNumberOfActivities(app,numberOfActivities);
            NumberOfMethods.createNumberOfMethods(app,numberOfMethods);
            NumberOfViews.createNumberOfViews(app,numberOfViews);
            NumberOfServices.createNumberOfServices(app,numberOfServices);
            NumberOfBroadcastReceivers.createNumberOfBroadcastReceivers(app,numberOfBroadcastReceivers);
            NumberOfInnerClasses.createNumberOfInnerClasses(app,numberOfInnerClasses);
            NumberOfAsyncTasks.createNumberOfAsyncTasks(app,numberOfAsyncTasks);
            NumberOfContentProviders.createNumberOfContentProviders(app,numberOfContentProviders);
            NumberOfVariables.createNumberOfVariables(app, numberOfVariables);
            for(PaprikaClass paprikaClass: app.getPaprikaClasses()){
                calculateClassMetrics(paprikaClass);
            }
            calculateGraphMetrics(app);
        }


        public static void calculateClassMetrics(PaprikaClass paprikaClass){
            if(paprikaClass.isInterface()){
                IsInterface.createIsInterface(paprikaClass,true);
            }
            if(paprikaClass.isInnerClass()){
                IsInnerClass.createIsInnerClass(paprikaClass,true);
            }
            if (paprikaClass.isActivity()){
                IsActivity.createIsActivity(paprikaClass,true);
            }else if (paprikaClass.isBroadcastReceiver()){
                IsBroadcastReceiver.createIsBroadcastReceiver(paprikaClass,true);
            }else if (paprikaClass.isContentProvider()){
                IsContentProvider.createIsContentProvider(paprikaClass,true);
            }else if (paprikaClass.isService()){
                IsService.createIsService(paprikaClass,true);
            }else if (paprikaClass.isView()){
                IsView.createIsView(paprikaClass,true);
            }else if (paprikaClass.isAsyncTask()){
                IsAsyncTask.createIsAsyncTask(paprikaClass,true);
            }else if (paprikaClass.isApplication()){
                IsApplication.createIsApplication(paprikaClass,true);
            }
            NumberOfAttributes.createNumberOfAttributes(paprikaClass,paprikaClass.getPaprikaVariables().size());
            NumberOfMethods.createNumberOfMethods(paprikaClass, paprikaClass.getPaprikaMethods().size());
            NumberOfImplementedInterfaces.createNumberOfImplementedInterfaces(paprikaClass,
                    paprikaClass.getInterfacesNames().size());
            CouplingBetweenObjects.createCouplingBetweenObjects(paprikaClass);
            DepthOfInheritance.createDepthOfInheritance(paprikaClass,paprikaClass.getDepthOfInheritance());
            LackofCohesionInMethods.createLackofCohesionInMethods(paprikaClass);
            ClassComplexity.createClassComplexity(paprikaClass);
            NPathComplexity.createNPathComplexity(paprikaClass);
            if(paprikaClass.isStatic())
            {
                IsStatic.createIsStatic(paprikaClass,true);
            }
            NumberOfChildren.createNumberOfChildren(paprikaClass);
            for(PaprikaMethod paprikaMethod: paprikaClass.getPaprikaMethods()){
                calculateMethodMetrics(paprikaMethod);
            }

            for (PaprikaVariable paprikaVariable: paprikaClass.getPaprikaVariables()){
                if(paprikaVariable.isStatic()){
                    IsStatic.createIsStatic(paprikaVariable,true);
                }
            }

        }

        public static void calculateMethodMetrics(PaprikaMethod paprikaMethod){
            NumberOfParameters.createNumberOfParameters(paprikaMethod, paprikaMethod.getArguments().size());
            NumberOfDirectCalls.createNumberOfDirectCalls(paprikaMethod, paprikaMethod.getCalledMethods().size());
            if(paprikaMethod.isConstructor()){
                IsInit.createIsInit(paprikaMethod,true);
            }else if(paprikaMethod.isGetter()){
                IsGetter.createIsGetter(paprikaMethod,true);
            }else if(paprikaMethod.isSetter()){
                IsSetter.createIsSetter(paprikaMethod,true);
            }
            if(paprikaMethod.isStatic()){
                IsStatic.createIsStatic(paprikaMethod,true);
            }
            if(paprikaMethod.isOverride()){
                IsOverride.createIsOverride(paprikaMethod, true);
            }
            NumberOfLines.createNumberOfLines(paprikaMethod, paprikaMethod.getNumberOfLines());
            CyclomaticComplexity.createCyclomaticComplexity(paprikaMethod, paprikaMethod.getComplexity());
        }

        private static void calculateGraphMetrics(PaprikaApp app){
            HashMap<PaprikaMethod, Integer> numberOfCallers = new HashMap<>();
            Integer nb;
            for(PaprikaClass paprikaClass: app.getPaprikaClasses()){
                for (PaprikaMethod paprikaMethod: paprikaClass.getPaprikaMethods()){
                    if(!numberOfCallers.containsKey(paprikaMethod)){
                        numberOfCallers.put(paprikaMethod,0);
                    }
                    for(Entity entity: paprikaMethod.getCalledMethods()){
                        if(entity instanceof PaprikaMethod){
                            nb=numberOfCallers.get((PaprikaMethod)entity);
                            if(nb==null){
                                numberOfCallers.put((PaprikaMethod)entity,1);
                            }else{
                                numberOfCallers.put((PaprikaMethod)entity,nb+1);
                            }
                        }
                    }
                }
            }
            for(PaprikaClass paprikaClass:app.getPaprikaClasses()){
                //compute the number of callers
                for(PaprikaMethod paprikaMethod: paprikaClass.getPaprikaMethods()){
                    NumberOfCallers.createNumberOfCallers(paprikaMethod,numberOfCallers.get(paprikaMethod));
                }
            }
        }

}
