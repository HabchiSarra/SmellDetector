package analyzer;

/**
 * Created by sarra on 21/02/17.
 */
public class InvocationData {
    private String target;
    private String method;


    public InvocationData(String target, String method) {
        this.target = target;
        this.method = method;
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
}
