import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.io.File;

public class ExportDatabase {
    public static void main(String[] args) throws Exception {
        String dbUrl = "jdbc:h2:" + System.getProperty("user.home") + "/bdms_v2";
        String outputFile = "bdms_dump.sql";
        
        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection(dbUrl, "sa", "");
        
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("SCRIPT TO '" + outputFile + "'");
            System.out.println("✓ Database dump created successfully: " + outputFile);
            
            File f = new File(outputFile);
            System.out.println("✓ File size: " + f.length() + " bytes");
        } finally {
            conn.close();
        }
    }
}
