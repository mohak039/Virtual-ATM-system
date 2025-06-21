import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.swing.table.DefaultTableModel;
import com.lowagie.text.Font;

public class ATMGUI extends JFrame {
    private AccountService accountService;
    private Account currentAccount;
    private JLabel welcomeLabel = new JLabel();
    private JLabel balanceLabel;
    private Image backgroundImage;
    private boolean isDarkMode = false;

    private JPanel mainPanel;
    private JPasswordField pinField;
    private JTextField accountNumberField;

    public ATMGUI() {
        // Set FlatLaf Look and Feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            FlatLaf.updateUI();
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf");
        }

        // Initialize database and service
        AccountService.initializeDatabase();
        accountService = new AccountService();
        setupFrame();
        loadImages();
        showLoginPanel();
    }

    private void loadImages() {
        try {
            backgroundImage = ImageIO.read(new File("src/images/atm_background.jpg"));
            System.out.println("Images loaded successfully.");
        } catch (IOException e) {
            System.out.println("Error loading images: " + e.getMessage());
        }
    }

    private void setupFrame() {
        setTitle("Virtual ATM System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.getWidth() * 0.8);  // 80% of screen width
        int height = (int) (screenSize.getHeight() * 0.8); // 80% of screen height
        
        // Set initial size
        setSize(width, height);
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Add fullscreen toggle with F11 key
        KeyStroke f11KeyStroke = KeyStroke.getKeyStroke("F11");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f11KeyStroke, "F11");
        getRootPane().getActionMap().put("F11", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                    setExtendedState(JFrame.NORMAL);
                } else {
                    setExtendedState(JFrame.MAXIMIZED_BOTH);
                }
            }
        });
    }

    private void showLoginPanel() {
        JPanel loginPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Create a semi-transparent panel for the login form
        JPanel formPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 230));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        formPanel.setLayout(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        // Title
        JLabel titleLabel = new JLabel("Welcome to Virtual ATM");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 28));
        titleLabel.setForeground(new Color(51, 98, 140));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 30, 0);
        formPanel.add(titleLabel, gbc);

        // Account Number Label and Field
        JLabel accountNumberLabel = new JLabel("Account Number:");
        accountNumberLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(accountNumberLabel, gbc);

        accountNumberField = new JTextField(15);
        accountNumberField.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        accountNumberField.setPreferredSize(new Dimension(250, 35));
        accountNumberField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(accountNumberField, gbc);

        // PIN Label
        JLabel pinLabel = new JLabel("Enter PIN:");
        pinLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(pinLabel, gbc);

        // PIN Field
        pinField = new JPasswordField(15);
        pinField.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        pinField.setPreferredSize(new Dimension(250, 35));
        pinField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(pinField, gbc);

        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(200, 40));
        loginButton.setBackground(new Color(51, 98, 140));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        loginButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(41, 78, 120));
            }
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(new Color(51, 98, 140));
            }
        });
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(30, 0, 5, 0);
        formPanel.add(loginButton, gbc);

        // Message Label
        JLabel loginMsg = new JLabel("");
        loginMsg.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        loginMsg.setForeground(new Color(200, 0, 0));
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 0, 0, 0);
        formPanel.add(loginMsg, gbc);

        // Add login action
        loginButton.addActionListener(e -> {
            String accountNumber = accountNumberField.getText();
            String pin = new String(pinField.getPassword());
            Account acc = accountService.getAccountByNumber(accountNumber);
            if (acc != null && acc.getPin().equals(pin)) {
                currentAccount = acc;
                showMenuPanel();
                welcomeLabel.setText(String.format("Welcome, %s!", acc.getOwner()));
                pinField.setText("");
                accountNumberField.setText("");
                loginMsg.setText("");
            } else {
                loginMsg.setText("Invalid account number or PIN. Please try again.");
            }
        });

        // Add the form panel to the main login panel
        loginPanel.add(formPanel);
        setContentPane(loginPanel);
        revalidate();
        repaint();
    }

    private void showMenuPanel() {
        JPanel menuPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        menuPanel.setLayout(new BorderLayout());
        
        // Create a semi-transparent panel for the menu content
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 150));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(60, 120, 60, 120));

        // Welcome and Balance Section
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        welcomeLabel.setText(String.format("Welcome, %s!", currentAccount.getOwner()));
        welcomeLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(51, 98, 140));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(welcomeLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        contentPanel.add(headerPanel);

        // Create styled buttons
        JButton balanceBtn = createModernButton("Check Balance", "");
        JButton depositBtn = createModernButton("Deposit", "");
        JButton withdrawBtn = createModernButton("Withdraw", "");
        JButton transferBtn = createModernButton("Transfer", "");
        JButton historyBtn = createModernButton("Transaction History", "");
        JButton changePinBtn = createModernButton("Change PIN", "");
        JButton logoutBtn = createModernButton("Logout", "");
        JButton exitBtn = createModernButton("Exit", "");
        JButton locationBtn = createModernButton("Find ATM Location", "");

        // Arrange buttons in a 2-column grid
        JPanel buttonGrid = new JPanel(new GridLayout(5, 2, 20, 20));
        buttonGrid.setOpaque(false);
        buttonGrid.add(balanceBtn);
        buttonGrid.add(depositBtn);
        buttonGrid.add(withdrawBtn);
        buttonGrid.add(transferBtn);
        buttonGrid.add(historyBtn);
        buttonGrid.add(changePinBtn);
        buttonGrid.add(locationBtn);
        buttonGrid.add(logoutBtn);
        buttonGrid.add(exitBtn);

        contentPanel.add(buttonGrid);

        // Add button actions
        balanceBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                String.format("Account: %s\nCurrent Balance: $%.2f", currentAccount.getAccountNumber(), currentAccount.getBalance()),
                "Balance",
                JOptionPane.INFORMATION_MESSAGE);
        });

        depositBtn.addActionListener(e -> {
            String amount = JOptionPane.showInputDialog(this, "Enter amount to deposit:");
            if (amount != null && !amount.isEmpty()) {
                try {
                    double depositAmount = Double.parseDouble(amount);
                    if (depositAmount > 0) {
                        double newBalance = currentAccount.getBalance() + depositAmount;
                        if (accountService.updateBalance(currentAccount.getAccountNumber(), newBalance)) {
                            currentAccount.deposit(depositAmount);
                            JOptionPane.showMessageDialog(this,
                                String.format("Deposit successful!\nNew Balance: $%.2f", currentAccount.getBalance()),
                                "Deposit",
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, "Deposit failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Please enter a valid amount greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        withdrawBtn.addActionListener(e -> {
            String amount = JOptionPane.showInputDialog(this, "Enter amount to withdraw:");
            if (amount != null && !amount.isEmpty()) {
                try {
                    double withdrawAmount = Double.parseDouble(amount);
                    if (withdrawAmount > 0) {
                        if (currentAccount.getBalance() >= withdrawAmount) {
                            double newBalance = currentAccount.getBalance() - withdrawAmount;
                            if (accountService.updateBalance(currentAccount.getAccountNumber(), newBalance)) {
                                currentAccount.withdraw(withdrawAmount);
                                JOptionPane.showMessageDialog(this,
                                    String.format("Withdrawal successful!\nNew Balance: $%.2f", currentAccount.getBalance()),
                                    "Withdraw",
                                    JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(this, "Withdrawal failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "Insufficient funds.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Please enter a valid amount greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Transfer button action
        transferBtn.addActionListener(e -> {
            String accountNumber = JOptionPane.showInputDialog(this, "Enter recipient account number:");
            if (accountNumber != null && !accountNumber.isEmpty()) {
                String amount = JOptionPane.showInputDialog(this, "Enter amount to transfer:");
                if (amount != null && !amount.isEmpty()) {
                    try {
                        double transferAmount = Double.parseDouble(amount);
                        if (transferAmount > 0) {
                            if (currentAccount.getBalance() < transferAmount) {
                                JOptionPane.showMessageDialog(this, "Insufficient funds.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            Account destAcc = accountService.getAccountByNumber(accountNumber);
                            if (destAcc == null) {
                                JOptionPane.showMessageDialog(this, "Destination account not found.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            if (accountService.transfer(currentAccount.getAccountNumber(), accountNumber, transferAmount)) {
                                currentAccount.withdraw(transferAmount);
                                destAcc.deposit(transferAmount);
                                JOptionPane.showMessageDialog(this, 
                                    String.format("Successfully transferred $%.2f to account %s\nNew Balance: $%.2f", transferAmount, accountNumber, currentAccount.getBalance()),
                                    "Transfer Successful", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(this, "Transfer failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "Please enter a valid amount greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // Transaction History button action
        historyBtn.addActionListener(e -> {
            JDialog historyDialog = new JDialog(this, "Transaction History", true);
            historyDialog.setSize(800, 500);
            
            JPanel historyPanel = new JPanel(new BorderLayout(10, 10));
            historyPanel.setBackground(new Color(255, 255, 255, 200));
            historyPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Add title label
            JLabel titleLabel = new JLabel("Transaction History", SwingConstants.CENTER);
            titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
            titleLabel.setForeground(new Color(51, 98, 140));
            historyPanel.add(titleLabel, BorderLayout.NORTH);

            String[] columnNames = {"Date", "Type", "Amount", "Balance", "Description"};
            Object[][] allTransactions = accountService.getTransactionHistory(currentAccount.getAccountNumber());
            
            // Create table model
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable historyTable = new JTable(tableModel);
            historyTable.setFillsViewportHeight(true);
            historyTable.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
            
            // Set column widths
            historyTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Date
            historyTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Type
            historyTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Amount
            historyTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Balance
            historyTable.getColumnModel().getColumn(4).setPreferredWidth(300); // Description
            
            // Create scroll pane for the table
            JScrollPane scrollPane = new JScrollPane(historyTable);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            historyPanel.add(scrollPane, BorderLayout.CENTER);

            // Navigation panel
            JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            navPanel.setBackground(new Color(255, 255, 255, 200));
            
            JButton prevButton = new JButton("Previous");
            JButton nextButton = new JButton("Next");
            JLabel pageLabel = new JLabel("Page 1");
            
            styleButton(prevButton);
            styleButton(nextButton);
            
            navPanel.add(prevButton);
            navPanel.add(pageLabel);
            navPanel.add(nextButton);
            
            // Button panel at the bottom
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            buttonPanel.setBackground(new Color(255, 255, 255, 200));

            JButton closeButton = new JButton("Close");
            styleButton(closeButton);
            closeButton.addActionListener(ev -> historyDialog.dispose());

            JButton pdfButton = new JButton("Download as PDF");
            styleButton(pdfButton);
            
            // Add PDF download functionality
            pdfButton.addActionListener(ev -> {
                try {
                    Document document = new Document(PageSize.A4.rotate());
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Save Transaction History PDF");
                    fileChooser.setSelectedFile(new File("Transaction_History_" + currentAccount.getAccountNumber() + ".pdf"));
                    int userSelection = fileChooser.showSaveDialog(historyDialog);
                    if (userSelection != JFileChooser.APPROVE_OPTION) {
                        document.close();
                        return;
                    }
                    File fileToSave = fileChooser.getSelectedFile();
                    PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
                    document.open();

                    // Add title
                    com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
                    Paragraph title = new Paragraph("Transaction History", titleFont);
                    title.setAlignment(Element.ALIGN_CENTER);
                    title.setSpacingAfter(20);
                    document.add(title);

                    // Add account info
                    com.lowagie.text.Font infoFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.NORMAL);
                    Paragraph accountInfo = new Paragraph(
                        String.format("Account Number: %s\nAccount Holder: %s\n", 
                            currentAccount.getAccountNumber(), 
                            currentAccount.getOwner()),
                        infoFont
                    );
                    accountInfo.setSpacingAfter(20);
                    document.add(accountInfo);

                    // Create table
                    PdfPTable pdfTable = new PdfPTable(5);
                    pdfTable.setWidthPercentage(100);
                    
                    // Add headers
                    String[] headers = {"Date", "Type", "Amount", "Balance", "Description"};
                    for (String header : headers) {
                        PdfPCell cell = new PdfPCell(new Phrase(header));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setBackgroundColor(new java.awt.Color(51, 98, 140));
                        cell.setPadding(5);
                        pdfTable.addCell(cell);
                    }

                    // Add data
                    for (Object[] transaction : allTransactions) {
                        for (Object value : transaction) {
                            PdfPCell cell = new PdfPCell(new Phrase(value.toString()));
                            cell.setPadding(5);
                            pdfTable.addCell(cell);
                        }
                    }

                    document.add(pdfTable);
                    document.close();

                    JOptionPane.showMessageDialog(historyDialog,
                        "PDF has been generated successfully: " + fileToSave.getAbsolutePath(),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(historyDialog,
                        "Error generating PDF: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            });
            
            buttonPanel.add(pdfButton);
            buttonPanel.add(closeButton);
            
            // Combine navigation and button panels
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setBackground(new Color(255, 255, 255, 200));
            bottomPanel.add(navPanel, BorderLayout.NORTH);
            bottomPanel.add(buttonPanel, BorderLayout.CENTER);
            
            historyPanel.add(bottomPanel, BorderLayout.SOUTH);

            // Pagination logic
            final int[] currentPage = {0};
            final int transactionsPerPage = 10;
            final int totalPages = (int) Math.ceil((double) allTransactions.length / transactionsPerPage);
            
            // Function to update table with current page data
            Runnable updateTable = () -> {
                tableModel.setRowCount(0); // Clear table
                int start = currentPage[0] * transactionsPerPage;
                int end = Math.min(start + transactionsPerPage, allTransactions.length);
                
                for (int i = start; i < end; i++) {
                    Object[] row = allTransactions[i];
                    tableModel.addRow(row);
                }
                
                pageLabel.setText(String.format("Page %d of %d", currentPage[0] + 1, totalPages));
                prevButton.setEnabled(currentPage[0] > 0);
                nextButton.setEnabled(currentPage[0] < totalPages - 1);
            };
            
            // Add navigation button listeners
            prevButton.addActionListener(ev -> {
                if (currentPage[0] > 0) {
                    currentPage[0]--;
                    updateTable.run();
                }
            });
            
            nextButton.addActionListener(ev -> {
                if (currentPage[0] < totalPages - 1) {
                    currentPage[0]++;
                    updateTable.run();
                }
            });
            
            // Initial table update
            updateTable.run();

            historyDialog.setContentPane(historyPanel);
            historyDialog.setLocationRelativeTo(this);
            historyDialog.setVisible(true);
        });

        // Change PIN button action
        changePinBtn.addActionListener(e -> {
            JDialog pinDialog = new JDialog(this, "Change PIN", true);
            JPanel pinPanel = new JPanel(new GridBagLayout());
            pinPanel.setBackground(new Color(255, 255, 255, 200));
            pinPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JPasswordField currentPinField = new JPasswordField(20);
            JPasswordField newPinField = new JPasswordField(20);
            JPasswordField confirmPinField = new JPasswordField(20);
            JLabel messageLabel = new JLabel("");
            messageLabel.setForeground(new Color(200, 0, 0));

            gbc.gridx = 0; gbc.gridy = 0;
            pinPanel.add(new JLabel("Current PIN:"), gbc);
            gbc.gridx = 1;
            pinPanel.add(currentPinField, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            pinPanel.add(new JLabel("New PIN:"), gbc);
            gbc.gridx = 1;
            pinPanel.add(newPinField, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            pinPanel.add(new JLabel("Confirm New PIN:"), gbc);
            gbc.gridx = 1;
            pinPanel.add(confirmPinField, gbc);

            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
            pinPanel.add(messageLabel, gbc);

            JButton changeButton = new JButton("Change PIN");
            styleButton(changeButton);
            gbc.gridy = 4;
            pinPanel.add(changeButton, gbc);

            changeButton.addActionListener(ev -> {
                String currentPin = new String(currentPinField.getPassword());
                String newPin = new String(newPinField.getPassword());
                String confirmPin = new String(confirmPinField.getPassword());

                if (!currentPin.equals(currentAccount.getPin())) {
                    messageLabel.setText("Current PIN is incorrect.");
                    return;
                }

                if (!newPin.equals(confirmPin)) {
                    messageLabel.setText("New PINs do not match.");
                    return;
                }

                if (newPin.length() != 4 || !newPin.matches("\\d+")) {
                    messageLabel.setText("PIN must be 4 digits.");
                    return;
                }

                if (accountService.updatePin(currentAccount.getAccountNumber(), newPin)) {
                    currentAccount.setPin(newPin);
                    pinDialog.dispose();
                    JOptionPane.showMessageDialog(this, 
                        "PIN successfully changed.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    messageLabel.setText("Failed to update PIN. Please try again.");
                }
            });

            pinDialog.setContentPane(pinPanel);
            pinDialog.pack();
            pinDialog.setLocationRelativeTo(this);
            pinDialog.setVisible(true);
        });

        // Logout button action
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                currentAccount = null;
                showLoginPanel();
            }
        });

        // Exit button action
        exitBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
            }
        });

        // Location button action
        locationBtn.addActionListener(e -> {
            String location = JOptionPane.showInputDialog(this, "Enter your city or location:");
            if (location != null && !location.trim().isEmpty()) {
                findNearbyATMs(location);
            }
        });

        menuPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(menuPanel);
        revalidate();
        repaint();
    }

    private JButton createModernButton(String text, String icon) {
        JButton button = new JButton(text);
        button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        button.setPreferredSize(new Dimension(200, 45));
        button.setBackground(new Color(51, 98, 140));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(41, 78, 120));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(51, 98, 140));
            }
        });
        
        return button;
    }

    private void styleButton(JButton button) {
        button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        button.setBackground(new Color(51, 98, 140));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 50));
        button.setPreferredSize(new Dimension(180, 50));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(41, 78, 120));
            }
            
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(51, 98, 140));
            }
        });
    }

    private void findNearbyATMs(String location) {
        try {
            OkHttpClient client = new OkHttpClient();
            String url = "https://nominatim.openstreetmap.org/search?format=json&q=atm+near+" + location + "&limit=10";
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "ATM-Finder/1.0")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                Gson gson = new Gson();
                JsonArray results = gson.fromJson(responseBody, JsonArray.class);

                if (results.size() == 0) {
                    JOptionPane.showMessageDialog(this, "No ATMs found for location: " + location);
                    return;
                }

                // Create a custom dialog
                JDialog mapDialog = new JDialog(this, "Nearby ATMs", true);
                mapDialog.setLayout(new BorderLayout());
                
                // Create a panel for the list
                JPanel listPanel = new JPanel();
                listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
                listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                
                // Add each ATM to the list with a "View on Map" button
                for (int i = 0; i < results.size(); i++) {
                    JsonObject atm = results.get(i).getAsJsonObject();
                    String name = atm.has("display_name") ? atm.get("display_name").getAsString() : "Unknown";
                    double lat = atm.get("lat").getAsDouble();
                    double lon = atm.get("lon").getAsDouble();
                    
                    JPanel atmPanel = new JPanel(new BorderLayout());
                    JLabel atmLabel = new JLabel((i + 1) + ". " + name);
                    JButton mapButton = new JButton("View on Map");
                    
                    // Style the button
                    mapButton.setBackground(new Color(51, 98, 140));
                    mapButton.setForeground(Color.WHITE);
                    mapButton.setFocusPainted(false);
                    mapButton.setBorderPainted(false);
                    
                    // Add hover effect
                    mapButton.addMouseListener(new MouseAdapter() {
                        public void mouseEntered(MouseEvent e) {
                            mapButton.setBackground(new Color(41, 78, 120));
                        }
                        public void mouseExited(MouseEvent e) {
                            mapButton.setBackground(new Color(51, 98, 140));
                        }
                    });
                    
                    // Add action to open Google Maps
                    final double finalLat = lat;
                    final double finalLon = lon;
                    mapButton.addActionListener(e -> {
                        try {
                            String mapsUrl = String.format("https://www.google.com/maps?q=%f,%f", finalLat, finalLon);
                            Desktop.getDesktop().browse(java.net.URI.create(mapsUrl));
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(mapDialog, 
                                "Error opening maps: " + ex.getMessage(), 
                                "Error", 
                                JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    
                    atmPanel.add(atmLabel, BorderLayout.CENTER);
                    atmPanel.add(mapButton, BorderLayout.EAST);
                    listPanel.add(atmPanel);
                    listPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                }
                
                // Add a scroll pane
                JScrollPane scrollPane = new JScrollPane(listPanel);
                scrollPane.setPreferredSize(new Dimension(500, 300));
                mapDialog.add(scrollPane, BorderLayout.CENTER);
                
                // Add a close button
                JButton closeButton = new JButton("Close");
                closeButton.addActionListener(e -> mapDialog.dispose());
                JPanel buttonPanel = new JPanel();
                buttonPanel.add(closeButton);
                mapDialog.add(buttonPanel, BorderLayout.SOUTH);
                
                // Show the dialog
                mapDialog.pack();
                mapDialog.setLocationRelativeTo(this);
                mapDialog.setVisible(true);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error finding ATMs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ATMGUI().setVisible(true);
        });
    }
} 