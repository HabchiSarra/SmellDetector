package analyzer;

/**
 * Created by sarra on 21/02/17.
 */
public class InvocationData {
    private String target;
    private String method;
    private String type;


    public InvocationData(String target, String method, String type) {
        this.target = target;
        this.method = method;
        this.type=type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
