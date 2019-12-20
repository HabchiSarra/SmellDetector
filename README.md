# SmellDetector

SmellDetector is a tool for detecting code smells in Android apps. <br />
It detects the following code smells:

* **Leaking Inner Class (_LIC_)**: in Android, anonymous and non-static inner classes hold a reference of the containing class. This can prevent the garbage collector 
from freeing the memory space of the outer class even when it is not used anymore, thus causing memory leaks.

* **Member Ignoring Method (_MIM_)**: this code smell occurs when a method that is not a constructor and does not access non-static attributes is not static. As the invocation 
of static methods is 15\%--20\% faster than dynamic invocations, the framework recommends making these methods static.

* **No LowMemory Resolver (_NLMR_)**: this code smell occurs when an Activity does not implement the method onLowMemory(). 
This method is called by the operating system when running low on memory in order to free allocated and unused memory spaces. 
If it is not implemented, the operating system may kill the process.

* **Hashmap Usage (_HMU_)**: the usage of HashMap is inadvisable when managing small sets in Android. 
Using Hashmaps entails the auto-boxing process where primitive types are converted into generic objects. 
The issue is that generic objects are much larger than primitive types, 16 and 4 bytes, respectively. 
Therefore, the framework recommends using the SparseArray data structure that is more memory-efficient.

* **UI Overdraw (_UIO_)**: a UI Overdraw is a situation where a pixel of the screen is drawn many times in the same frame. 
This happens when the UI design consists of unneeded overlapping layers, _e.g.,_ hidden backgrounds. 
To avoid such situations, the canvas.quickreject() API should be used to define the view boundaries that are drawable.

* **Unsupported Hardware Acceleration (_UHA_)**: in Android, most of the drawing operations are executed in the GPU. 
Rare drawing operations that are executed in the CPU, _e.g.,_ drawPath() method in android.graphics.Canvas, should be avoided to reduce CPU load.

* **Init OnDraw (_IOD_)**: _a.k.a._ DrawAllocation, this occurs when allocations are made inside onDraw() routines. 
The onDraw() methods are responsible for drawing Views and they are invoked 60 times per second. 
Therefore, allocations should be avoided inside them in order to avoid memory churn.

* **Unsuited Cache Size (_UCS_)**: in Android, a cache can be used to store frequently used objects with the Least Recently Used (LRU) API. 
The code smell occurs when the LRU is initialized without checking the available memory via the method getMemoryClass(). 
The available memory may vary considerably according to the device so it is necessary to adapt the cache size to the available memory.

SmellDetector performs static analysis using [Spoon](https://github.com/INRIA/spoon), a framework for Java-based programs analysis and transformation.
In this analysis, it builds an abstract model of the source code that contains the code entities (_e.g.,_ classes and methods), 
properties (_e.g.,_ names, types), and metrics (_e.g.,_ number of lines, complexity).
This model is stored in a graph database with Neo4J, which can be queried to detect the aforementioned code smells.

This project is part of the [Sniffer Toolkit](https://github.com/HabchiSarra/Sniffer). <br /> 
For more details about the tool and its associated research work, you can refer to the following research papers:

* [The rise of Android code smells: Who is to blame?](https://ieeexplore.ieee.org/document/8816779)
* [On the survival of Android code smells in the wild.](https://ieeexplore.ieee.org/abstract/document/8816910)

## Build

To build the jar, use the command `./gradlew shadowJar `
