import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/postgres"; // Connect to default postgres database first
    private static final String USER = "postgres";
    private static final String PASSWORD = "harkr005";
    
    private static Connection connection = null;
    private static boolean isInitialized = false;
    
    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("org.postgresql.Driver");
                
                // First connect to postgres database to check/create atm_db
                Connection tempConn = DriverManager.getConnection(URL, USER, PASSWORD);
                try (Statement stmt = tempConn.createStatement()) {
                    stmt.execute("SELECT 1 FROM pg_database WHERE datname = 'atm_db'");
                    if (!stmt.getResultSet().next()) {
                        stmt.execute("CREATE DATABASE atm_db");
                        System.out.println("Database 'atm_db' created successfully");
                    }
                }
                tempConn.close();
                
                // Now connect to atm_db
                connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/atm_db",
                    USER,
                    PASSWORD
                );
                System.out.println("Connected to atm_db database");
                
                // Set some connection properties for better stability
                connection.setAutoCommit(true);
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            }
            return connection;
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver not found");
            e.printStackTrace();
            throw new SQLException("PostgreSQL JDBC Driver not found", e);
        } catch (SQLException e) {
            System.out.println("Connection failed");
            e.printStackTrace();
            throw e;
        }
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void initializeDatabase() {
        if (isInitialized) {
            return;
        }

        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            
            // Check if accounts table exists
            ResultSet rs = stmt.executeQuery("SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'accounts')");
            boolean accountsTableExists = rs.next() && rs.getBoolean(1);
            
            // Create accounts table if it doesn't exist
            if (!accountsTableExists) {
                String createAccountsTable = 
                    "CREATE TABLE accounts (" +
                    "    account_number VARCHAR(20) PRIMARY KEY," +
                    "    owner_name VARCHAR(100) NOT NULL," +
                    "    pin VARCHAR(4) NOT NULL," +
                    "    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00" +
                    ")";
                stmt.execute(createAccountsTable);
                System.out.println("Created accounts table");

                // Insert sample accounts only if table was just created
                String[] sampleAccounts = {
                    "INSERT INTO accounts (account_number, owner_name, pin, balance) VALUES ('ACC001', 'John Doe', '1234', 1000.00)",
                    "INSERT INTO accounts (account_number, owner_name, pin, balance) VALUES ('ACC002', 'Jane Smith', '5678', 2500.00)",
                    "INSERT INTO accounts (account_number, owner_name, pin, balance) VALUES ('ACC003', 'Bob Johnson', '9012', 500.00)",
                    "INSERT INTO accounts (account_number, owner_name, pin, balance) VALUES ('ACC004', 'Alice Brown', '3456', 3000.00)",
                    "INSERT INTO accounts (account_number, owner_name, pin, balance) VALUES ('ACC005', 'Charlie Wilson', '7890', 1500.00)"
                };
                
                for (String insertQuery : sampleAccounts) {
                    stmt.execute(insertQuery);
                }
                System.out.println("Inserted sample accounts");
            }

            // Check if transactions table exists
            rs = stmt.executeQuery("SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'transactions')");
            boolean transactionsTableExists = rs.next() && rs.getBoolean(1);
            
            // Create transactions table if it doesn't exist
            if (!transactionsTableExists) {
                String createTransactionsTable = 
                    "CREATE TABLE transactions (" +
                    "    id SERIAL PRIMARY KEY," +
                    "    account_number VARCHAR(20) NOT NULL," +
                    "    transaction_type VARCHAR(20) NOT NULL," +
                    "    amount DECIMAL(10,2) NOT NULL," +
                    "    balance DECIMAL(10,2) NOT NULL," +
                    "    description TEXT," +
                    "    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "    FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE CASCADE" +
                    ")";
                stmt.execute(createTransactionsTable);
                System.out.println("Created transactions table");
            }

            // Print current table statistics
            rs = stmt.executeQuery("SELECT COUNT(*) FROM accounts");
            rs.next();
            System.out.println("Number of accounts: " + rs.getInt(1));
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM transactions");
            rs.next();
            System.out.println("Number of transactions: " + rs.getInt(1));

            isInitialized = true;
        } catch (SQLException e) {
            System.out.println("Error initializing database:");
            e.printStackTrace();
        }
    }
} 