/*
 * Paprika - Detection of code smells in Android application
 *     Copyright (C)  2016  Geoffrey Hecht - INRIA - UQAM - University of Lille
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package entities;

import analyzer.InvocationData;
import analyzer.VariableData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class PaprikaMethod extends Entity{
    private PaprikaClass paprikaClass;
    private String returnType;
    private Set<PaprikaVariable> usedVariables;
    private Set<Entity> calledMethods;
    private PaprikaModifiers modifier;
    private  int numberOfLines;
    private ArrayList<VariableData> usedVariablesData;
    private ArrayList<InvocationData> invocationData;
    private int complexity;
    private int numberOfDeclaredLocals;
    private List<PaprikaArgument> arguments;
    private boolean isConstructor;
    private boolean isGetter;
    private boolean isSetter;
    private boolean isStatic;
    private boolean isOverride;

    public boolean isOverride() {
        return isOverride;
    }

    public void setOverride(boolean override) {
        isOverride = override;
    }

    public String getReturnType() {
        return returnType;
    }

    public PaprikaModifiers getModifier() {
        return modifier;
    }

    private PaprikaMethod(String name, PaprikaModifiers modifier, String returnType, PaprikaClass paprikaClass) {
        this.setName(name);
        this.paprikaClass = paprikaClass;
        this.usedVariables = new HashSet<>();
        this.calledMethods = new HashSet<>();
        this.arguments = new ArrayList<>();
        this.modifier = modifier;
        this.returnType = returnType;
        this.numberOfLines=0;
        this.usedVariablesData = new ArrayList<>();
        this.invocationData =new ArrayList<>();
        this.complexity =0 ;
        this.numberOfDeclaredLocals=0;
        this.isConstructor=false;
        this.isSetter=false;
        this.isGetter=false;
        this.isStatic=false;
        this.isOverride=false;
    }

    public static PaprikaMethod createPaprikaMethod(String name, PaprikaModifiers modifier, String returnType,  PaprikaClass paprikaClass) {
        PaprikaMethod paprikaMethod = new PaprikaMethod(name, modifier, returnType, paprikaClass);
        paprikaClass.addPaprikaMethod(paprikaMethod);
        return  paprikaMethod;
    }

    public PaprikaClass getPaprikaClass() {
        return paprikaClass;
    }

    public void setPaprikaClass(PaprikaClass paprikaClass) {
        this.paprikaClass = paprikaClass;
    }

    @Override
    public String toString() {
        return this.getName() + "#" + paprikaClass;
    }

    public void useVariable(PaprikaVariable paprikaVariable) {
        usedVariables.add(paprikaVariable);
    }

    public Set<PaprikaVariable> getUsedVariables(){
        return this.usedVariables;
    }

    public void callMethod(Entity paprikaMethod) { calledMethods.add(paprikaMethod);}

    public Set<Entity> getCalledMethods() { return this.calledMethods; }

    public boolean haveCommonFields(PaprikaMethod paprikaMethod){
        Set<PaprikaVariable> otherVariables = paprikaMethod.getUsedVariables();
        for(PaprikaVariable paprikaVariable : usedVariables){
            if(otherVariables.contains(paprikaVariable)) return true;
        }
        return false;
    }

    public void addArgument(PaprikaArgument paprikaArgument){
        this.arguments.add(paprikaArgument);
    }

    public List<PaprikaArgument> getArguments(){
        return arguments;
    }

    public int getNumberOfLines() {
        return numberOfLines;
    }

    public void setNumberOfLines(int numberOfLines) {
        this.numberOfLines = numberOfLines;
    }

    public void setUsedVariables(Set<PaprikaVariable> usedVariables) {
        this.usedVariables = usedVariables;
    }

    public ArrayList<InvocationData> getInvocationData() {
        return invocationData;
    }

    public void setInvocationData(ArrayList<InvocationData> invocationData) {
        this.invocationData = invocationData;
    }

    public ArrayList<VariableData> getUsedVariablesData() {
        return usedVariablesData;
    }

    public void setUsedVariablesData(ArrayList<VariableData> usedVariablesData) {
        this.usedVariablesData = usedVariablesData;
    }

    public int getComplexity() {
        return complexity;
    }

    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }

    public int getNumberOfDeclaredLocals() {
        return numberOfDeclaredLocals;
    }

    public void setNumberOfDeclaredLocals(int numberOfDeclaredLocals) {
        this.numberOfDeclaredLocals = numberOfDeclaredLocals;
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public void setConstructor(boolean constructor) {
        isConstructor = constructor;
    }

    public boolean isGetter() {
        return isGetter;
    }

    public void setGetter(boolean getter) {
        isGetter = getter;
    }

    public boolean isSetter() {
        return isSetter;
    }

    public void setSetter(boolean setter) {
        isSetter = setter;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }
}
