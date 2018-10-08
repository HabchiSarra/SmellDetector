package fr.inria.sniffer.detector.entities;

/**
 * Created by sarra on 26/04/17.
 */
public class PaprikaLibrary extends Entity {

    PaprikaApp paprikaApp;

    private PaprikaLibrary(String name,  PaprikaApp paprikaApp) {
        this.name=name;
        this.paprikaApp = paprikaApp;
    }

    public static PaprikaLibrary createPaprikaLibrary(String name,  PaprikaApp paprikaApp){
        PaprikaLibrary paprikaLibrary =new PaprikaLibrary(name,paprikaApp);
        paprikaApp.addPaprikaLibrary(paprikaLibrary);
        return paprikaLibrary;
    }


    public PaprikaApp getPaprikaApp() {
        return paprikaApp;
    }

    public void setPaprikaApp(PaprikaApp paprikaApp) {
        this.paprikaApp = paprikaApp;
    }
}
