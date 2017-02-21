package analyzer;

/**
 * Created by sarra on 21/02/17.
 */
public class InvocationData {
    String target;
    String method;
    //TODO arguments?


    public InvocationData(String target, String method) {
        this.target = target;
        this.method = method;
    }
}
