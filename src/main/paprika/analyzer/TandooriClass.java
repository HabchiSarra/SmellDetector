package analyzer;

import java.util.Set;

public class TandooriClass  {

    private String name;
    private TandooriClass parent;
    //parent name to cover library case
    private String parentName;
    private int complexity;
    private int children;
    private Set<TandooriClass> coupled;
//    private Set<> paprikaMethods;
//    private Set<> paprikaVariables;
    private Set<TandooriClass> interfaces;
    private boolean isInterface;
    private  boolean isActivity;
    private int numberOfLines;

    public TandooriClass(String name, String parentName, boolean isInterface, boolean isActivity) {
        this.name = name;
        this.parentName = parentName;
        this.isInterface = isInterface;
        this.isActivity = isActivity;
        numberOfLines=0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TandooriClass getParent() {
        return parent;
    }

    public void setParent(TandooriClass parent) {
        this.parent = parent;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public int getComplexity() {
        return complexity;
    }

    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }

    public int getChildren() {
        return children;
    }

    public void setChildren(int children) {
        this.children = children;
    }

    public Set<TandooriClass> getCoupled() {
        return coupled;
    }

    public void setCoupled(Set<TandooriClass> coupled) {
        this.coupled = coupled;
    }



    public Set<TandooriClass> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(Set<TandooriClass> interfaces) {
        this.interfaces = interfaces;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public void setInterface(boolean anInterface) {
        isInterface = anInterface;
    }

    public boolean isActivity() {
        return isActivity;
    }

    public void setActivity(boolean activity) {
        isActivity = activity;
    }

    public int getNumberOfLines() {
        return numberOfLines;
    }

    public void setNumberOfLines(int numberOfLines) {
        this.numberOfLines = numberOfLines;
    }
}
