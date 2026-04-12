import org.h2.tools.Server;

public class StartH2Console {
    public static void main(String[] args) throws Exception {
        // Start web server on port 8082
        Server server = Server.createWebServer("-webPort", "8082");
        server.start();
        
        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║     H2 Web Console Started Successfully!            ║");
        System.out.println("╠════════════════════════════════════════════════════╣");
        System.out.println("║ URL: http://localhost:8082                         ║");
        System.out.println("║                                                    ║");
        System.out.println("║ Connection Settings:                               ║");
        System.out.println("║ - JDBC URL: jdbc:h2:~/bdms_v2                      ║");
        System.out.println("║ - Username: sa                                     ║");
        System.out.println("║ - Password: (leave blank)                          ║");
        System.out.println("║                                                    ║");
        System.out.println("║ Press Ctrl+C to stop                               ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
    }
}
