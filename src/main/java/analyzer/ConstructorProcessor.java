package analyzer;

import entities.PaprikaMethod;
import spoon.reflect.declaration.CtConstructor;

/**
 * Created by sarra on 08/03/17.
 */
public class ConstructorProcessor extends ExecutableProcessor<CtConstructor> {
    @Override
    protected void process(CtConstructor ctExecutable, PaprikaMethod paprikaMethod) {
        paprikaMethod.setConstructor(true);
    }
}
