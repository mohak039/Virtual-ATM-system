import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountService {
    private static boolean isInitialized = false;

    public static void initializeDatabase() {
        if (!isInitialized) {
            DatabaseConnection.initializeDatabase();
            isInitialized = true;
        }
    }

    public Account getAccountByPin(String pin) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM accounts WHERE pin = ?")) {
            stmt.setString(1, pin);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Account(
                    rs.getString("account_number"),
                    rs.getString("pin"),
                    rs.getString("owner_name"),
                    rs.getDouble("balance")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Account getAccountByNumber(String accountNumber) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM accounts WHERE account_number = ?")) {
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Account(
                    rs.getString("account_number"),
                    rs.getString("pin"),
                    rs.getString("owner_name"),
                    rs.getDouble("balance")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateBalance(String accountNumber, double newBalance) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Get current balance
                PreparedStatement checkStmt = conn.prepareStatement("SELECT balance FROM accounts WHERE account_number = ?");
                checkStmt.setString(1, accountNumber);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }
                double currentBalance = rs.getDouble("balance");
                double amount = newBalance - currentBalance;

                // Update balance
                PreparedStatement updateStmt = conn.prepareStatement("UPDATE accounts SET balance = ? WHERE account_number = ?");
                updateStmt.setDouble(1, newBalance);
                updateStmt.setString(2, accountNumber);
                updateStmt.executeUpdate();

                // Record transaction
                PreparedStatement transStmt = conn.prepareStatement(
                    "INSERT INTO transactions (account_number, transaction_type, amount, balance, description) " +
                    "VALUES (?, ?, ?, ?, ?)"
                );
                transStmt.setString(1, accountNumber);
                transStmt.setString(2, amount > 0 ? "DEPOSIT" : "WITHDRAW");
                transStmt.setDouble(3, amount);
                transStmt.setDouble(4, newBalance);
                transStmt.setString(5, amount > 0 ? "Deposit" : "Withdrawal");
                transStmt.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePin(String accountNumber, String newPin) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE accounts SET pin = ? WHERE account_number = ?")) {
            stmt.setString(1, newPin);
            stmt.setString(2, accountNumber);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean transfer(String fromAccount, String toAccount, double amount) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Check if source account has sufficient funds
                PreparedStatement checkStmt = conn.prepareStatement("SELECT balance FROM accounts WHERE account_number = ? FOR UPDATE");
                checkStmt.setString(1, fromAccount);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next() || rs.getDouble("balance") < amount) {
                    conn.rollback();
                    return false;
                }

                // Update source account
                PreparedStatement updateFromStmt = conn.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE account_number = ?");
                updateFromStmt.setDouble(1, amount);
                updateFromStmt.setString(2, fromAccount);
                updateFromStmt.executeUpdate();

                // Update destination account
                PreparedStatement updateToStmt = conn.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE account_number = ?");
                updateToStmt.setDouble(1, amount);
                updateToStmt.setString(2, toAccount);
                updateToStmt.executeUpdate();

                // Record transaction for source account
                PreparedStatement transStmt = conn.prepareStatement(
                    "INSERT INTO transactions (account_number, transaction_type, amount, balance, description) " +
                    "VALUES (?, 'TRANSFER', ?, (SELECT balance FROM accounts WHERE account_number = ?), ?)"
                );
                transStmt.setString(1, fromAccount);
                transStmt.setDouble(2, -amount);
                transStmt.setString(3, fromAccount);
                transStmt.setString(4, "Transfer to " + toAccount);
                transStmt.executeUpdate();

                // Record transaction for destination account
                transStmt.setString(1, toAccount);
                transStmt.setDouble(2, amount);
                transStmt.setString(3, toAccount);
                transStmt.setString(4, "Transfer from " + fromAccount);
                transStmt.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Object[][] getTransactionHistory(String accountNumber) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT transaction_date, transaction_type, amount, balance, description " +
                 "FROM transactions WHERE account_number = ? ORDER BY transaction_date DESC"
             )) {
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            List<Object[]> transactions = new ArrayList<>();
            while (rs.next()) {
                transactions.add(new Object[]{
                    rs.getTimestamp("transaction_date"),
                    rs.getString("transaction_type"),
                    rs.getDouble("amount"),
                    rs.getDouble("balance"),
                    rs.getString("description")
                });
            }
            return transactions.toArray(new Object[0][]);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Object[0][];
        }
    }
} 