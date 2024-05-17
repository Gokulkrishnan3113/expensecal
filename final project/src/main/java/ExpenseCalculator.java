import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class ExpenseCalculator extends JFrame {
    protected static final String JDBC_URL = "jdbc:mysql://localhost:3306/expensecalculator";
    protected static final String USERNAME = "root";
    protected static final String PASSWORD = "rootpassword";

    private DefaultTableModel tableModel;
    private JLabel welcomeLabel;
    private JLabel totalSpendingLabel;

    public ExpenseCalculator(String username) {
        setTitle("Expense Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create table model with columns: Amount, Date, Category
        tableModel = new DefaultTableModel(new Object[]{"Amount", "Date", "Category"}, 0);
        JTable expenseTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(expenseTable);
        double a = 0;
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/expensecalculator", "root", "rootpassword");
            String res = "SELECT * from expenses where user_name='"+username+"'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(res);
            while(rs.next())
            {
                tableModel.addRow(new Object[]{rs.getDouble("amount"),rs.getTimestamp("money_date"),rs.getString("category")});
                a = a+rs.getDouble("amount");
            }

        }catch (SQLException ex) {
            ex.printStackTrace();
        }
        // Create buttons
        JButton addButton = new JButton("Add New Expense");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openAddExpenseDialog(username);
            }
        });

        JButton insightsButton = new JButton("Insights");
        insightsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openInsightsPage(username);
            }
        });

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Close all open frames
                Window[] windows = getWindows();
                for (Window window : windows) {
                    if (window instanceof JFrame) {
                        window.dispose();
                    }
                }

                // Open the login page
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }
        });

        // Create welcome panel
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomeLabel = new JLabel("Welcome, " + username + "!");
        welcomePanel.add(welcomeLabel);

        // Create total spending panel
        JPanel totalSpendingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalSpendingLabel = new JLabel("Total Spending: ₹"+a);
        totalSpendingPanel.add(totalSpendingLabel);

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(welcomePanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(insightsButton);
        buttonPanel.add(logoutButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        add(totalSpendingPanel, BorderLayout.SOUTH);
    }

    private void openAddExpenseDialog(String username) {
        // Create add expense dialog
        JDialog addExpenseDialog = new JDialog(this, "Add New Expense", true);
        addExpenseDialog.setSize(400, 200);
        addExpenseDialog.setLocationRelativeTo(this);

        // Create labels and text fields
        JLabel amountLabel = new JLabel("Amount:");
        JTextField amountField = new JTextField(10);

        JLabel categoryLabel = new JLabel("Category:");
        String[] categories = {"Food", "Transport", "Grocery","Medical" ,"Education","Entertainment","Bills","Rent","Sports","Bank","Other"};
        JComboBox<String> categoryComboBox = new JComboBox<>(categories);

        // Create add button
        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addExpense(amountField.getText(), (String) categoryComboBox.getSelectedItem(),username);
                addExpenseDialog.dispose();
            }
        });

        // Create dialog layout
        JPanel dialogPanel = new JPanel(new GridLayout(3, 2));
        dialogPanel.add(amountLabel);
        dialogPanel.add(amountField);
        dialogPanel.add(categoryLabel);
        dialogPanel.add(categoryComboBox);
        dialogPanel.add(new JLabel());
        dialogPanel.add(addButton);

        addExpenseDialog.add(dialogPanel);
        addExpenseDialog.setVisible(true);
    }

    private void addExpense(String amountText, String category,String username) {
        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount format.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            String query = "INSERT INTO expenses (user_name,amount, category) VALUES (?,?,?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setDouble(2, amount);
            statement.setString(3, category);
            statement.executeUpdate();
            statement.close();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Add the expense to the table
        tableModel.addRow(new Object[]{amount, new java.util.Date(), category});

        // Update total spending label
        double totalSpending = calculateTotalSpending();
        totalSpendingLabel.setText("Total Spending: ₹" + String.format("%.2f", totalSpending));
    }

    private double calculateTotalSpending() {
        double totalSpending = 0.0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            double amount = (double) tableModel.getValueAt(i, 0);
            totalSpending += amount;
        }
        return totalSpending;
    }

    private void openInsightsPage(String u_name) {
        String url = "jdbc:mysql://localhost:3306/expensecalculator"; // Replace with your Oracle database URL
        String username = "root"; // Replace with your Oracle database username
        String password = "rootpassword"; // Replace with your Oracle database password

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            // 1. Load the Oracle JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2. Create a connection to the Oracle database
            connection = DriverManager.getConnection(url, username, password);

            // 3. Create a statement object to execute SQL queries
            statement = connection.createStatement();

            // 4. Execute the SQL query to fetch data from the expenses table
            String sqlQuery = "SELECT category, SUM(amount) AS total_amount FROM expenses where user_name='"+u_name+"'GROUP BY category";
            resultSet = statement.executeQuery(sqlQuery);

            // 5. Create a dataset to hold the category and total amount data
            DefaultPieDataset dataset = new DefaultPieDataset();
            while (resultSet.next()) {
                String category = resultSet.getString("category");
                double totalAmount = resultSet.getDouble("total_amount");
                dataset.setValue(category, totalAmount);
            }

            // 6. Create the pie chart using the dataset
            JFreeChart chart = ChartFactory.createPieChart("Expense Breakdown", dataset, true, true, false);

            // 7. Customize the appearance of the chart
            chart.getTitle().setPaint(Color.BLACK);
            chart.getTitle().setFont(new Font("Arial", Font.BOLD, 18));
            //chart.getLegend().setItemFont(new Font("Arial", Font.PLAIN, 12));

            // 8. Create a chart frame and display the chart
            ChartFrame frame = new ChartFrame("Expense Pie Chart", chart);
            frame.pack();
            frame.setVisible(true);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 9. Close the resources
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }
        });
    }
}

class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Expense Calculator - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();
        JLabel signUpLabel = new JLabel("Don't have an account?");

        inputPanel.add(usernameLabel);
        inputPanel.add(usernameField);
        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);
        inputPanel.add(new JLabel());
        inputPanel.add(signUpLabel);

        JPanel buttonPanel = new JPanel();
        JButton loginButton = new JButton("Login");
        JButton signUpButton = new JButton("Signup");
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Close all open frames
                Window[] windows = getWindows();
                for (Window window : windows) {
                    if (window instanceof JFrame) {
                        window.dispose();
                    }
                }


            }
        });

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                if (login(username, password)) {
                    openExpenseCalculator(username);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        signUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openSignupPage();
                dispose();
            }
        });

        buttonPanel.add(loginButton);
        buttonPanel.add(signUpButton);
        buttonPanel.add(closeButton);

        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private boolean login(String username, String password) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/expensecalculator", "root", "rootpassword");
            String res = "SELECT * from signup where user_name='"+username+"' and password='"+password+"'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(res);
            if (rs.next())
            {
                return true;
            }
            else {
                return false;
            }
        }catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void openExpenseCalculator(String username) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ExpenseCalculator expenseCalculator = new ExpenseCalculator(username);
                expenseCalculator.setVisible(true);
            }
        });
    }

    private void openSignupPage() {
        SignupFrame signupFrame = new SignupFrame();
        signupFrame.setVisible(true);
    }
}

class SignupFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JTextField ageField;
    private JTextField genderField;

    public SignupFrame() {
        setTitle("Expense Calculator - Signup");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(6, 2));
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();
        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField();
        JLabel ageLabel = new JLabel("Age:");
        ageField = new JTextField();
        JLabel genderLabel = new JLabel("Gender:");
        genderField = new JTextField();

        inputPanel.add(usernameLabel);
        inputPanel.add(usernameField);
        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);
        inputPanel.add(emailLabel);
        inputPanel.add(emailField);
        inputPanel.add(ageLabel);
        inputPanel.add(ageField);
        inputPanel.add(genderLabel);
        inputPanel.add(genderField);
        inputPanel.add(new JLabel());
        inputPanel.add(new JLabel());


        JPanel buttonPanel = new JPanel();
        JButton signupButton = new JButton("Signup");
        JButton backButton = new JButton("Back");

        signupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String email = emailField.getText();
                String age = ageField.getText();
                String gender = genderField.getText();

                if (signup(username, password, email, age, gender)) {
                    openExpenseCalculator(username);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(SignupFrame.this, "Failed to signup.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openLoginPage();
                dispose();
            }
        });

        buttonPanel.add(signupButton);
        buttonPanel.add(backButton);

        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private boolean signup(String username, String password, String email, String age, String gender) {
        // Perform your signup logic here
        // You can store the user details in a database or file
        // Implement your desired validation and data storage mechanism

        // Example code for storing user details in the database
        try {
            Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/expensecalculator","root","rootpassword");
            String query = "INSERT INTO signup (user_name, password, mail, age, gender) VALUES ('"+username+"','"+password+"','"+email+"','"+age+"','"+gender+"')";
            PreparedStatement statement = con.prepareStatement(query);
            statement.executeUpdate();
            statement.close();
            con.close();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void openExpenseCalculator(String username) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ExpenseCalculator expenseCalculator = new ExpenseCalculator(username);
                expenseCalculator.setVisible(true);
            }
        });
    }

    private void openLoginPage() {
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
    }
}