public class Account {
    private String accountNumber;
    private String pin;
    private String owner;
    private double balance;

    public Account(String accountNumber, String pin, String owner, double balance) {
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.owner = owner;
        this.balance = balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getOwner() {
        return owner;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        this.balance += amount;
    }

    public void withdraw(double amount) {
        this.balance -= amount;
    }
} 