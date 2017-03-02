package analyzer;

/**
 * Created by sarra on 21/02/17.
 */
public class VariableData {
    private String className;
    private String variableName;

    public VariableData(String className, String variableName) {
        this.className = className;
        this.variableName = variableName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }
}
