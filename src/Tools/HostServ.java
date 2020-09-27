package Tools;

import javafx.application.HostServices;

/**
 *
 * @author Korisnik
 */
public class HostServ {
    public static HostServices hostServices;

    public HostServ() {
    }

    public static HostServices getHostServices() {
        return hostServices;
    }

    public static void setHostServices(HostServices hostServices) {
        HostServ.hostServices = hostServices;
    }
    

}
