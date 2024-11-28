import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;

public class EmployeeManagementApp {
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/employee";
    static final String DB_USER = "root";
    static final String DB_PASS = "root";

    public static void main(String[] args) {
        new MainFrame();
    }
}

// MainFrame: Initial welcome screen
class MainFrame extends Frame {
    public MainFrame() {
        setTitle("Employee Management System");
        setSize(400, 200);
        setLayout(new FlowLayout());

        Label welcomeLabel = new Label("Welcome to Employee Management System");
        add(welcomeLabel);

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        add(loginButton);
        add(registerButton);

        loginButton.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        registerButton.addActionListener(e -> {
            dispose();
            new RegisterFrame();
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setVisible(true);
    }
}

// RegisterFrame: Registration screen for new users
class RegisterFrame extends Frame {
    TextField nameField, emailField, passwordField, roleField;

    public RegisterFrame() {
        setTitle("Register");
        setSize(300, 300);
        setLayout(new GridLayout(5, 2));

        Label nameLabel = new Label("Name:");
        nameField = new TextField();
        add(nameLabel);
        add(nameField);

        Label emailLabel = new Label("Email:");
        emailField = new TextField();
        add(emailLabel);
        add(emailField);

        Label passwordLabel = new Label("Password:");
        passwordField = new TextField();
        passwordField.setEchoChar('*');
        add(passwordLabel);
        add(passwordField);

        Label roleLabel = new Label("Role (HR/Employee):");
        roleField = new TextField();
        add(roleLabel);
        add(roleField);

        Button registerButton = new Button("Register");
        add(registerButton);

        registerButton.addActionListener(e -> registerUser());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setVisible(true);
    }

    private void registerUser() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String role = roleField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }

        try (Connection conn = DriverManager.getConnection(EmployeeManagementApp.DB_URL, EmployeeManagementApp.DB_USER, EmployeeManagementApp.DB_PASS)) {
            String query = "INSERT INTO ValidationDetails (Name, Email, pass, Role) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, role);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Registration Successful!");
            dispose();
            new LoginFrame();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Registration failed. Try again.");
        }
    }
}

// LoginFrame: Login screen for the user to authenticate
class LoginFrame extends Frame {
    TextField emailField, passwordField;
    Choice roleChoice;

    public LoginFrame() {
        setTitle("Login");
        setSize(300, 200);
        setLayout(new FlowLayout());

        Label roleLabel = new Label("Role:");
        roleChoice = new Choice();
        roleChoice.add("HR");
        roleChoice.add("Employee");

        add(roleLabel);
        add(roleChoice);

        Label emailLabel = new Label("Email:");
        emailField = new TextField(20);
        add(emailLabel);
        add(emailField);

        Label passwordLabel = new Label("Password:");
        passwordField = new TextField(20);
        passwordField.setEchoChar('*');
        add(passwordLabel);
        add(passwordField);

        Button loginButton = new Button("Login");
        add(loginButton);

        loginButton.addActionListener(e -> authenticateUser());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setVisible(true);
    }

    private void authenticateUser() {
        String role = roleChoice.getSelectedItem();
        String email = emailField.getText();
        String password = passwordField.getText();

        try (Connection conn = DriverManager.getConnection(EmployeeManagementApp.DB_URL, EmployeeManagementApp.DB_USER, EmployeeManagementApp.DB_PASS)) {
            String query = "SELECT * FROM ValidationDetails WHERE Email = ? AND pass = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                dispose();
                if ("HR".equals(role)) {
                    new HRFrame(email);
                } else {
                    new EmployeeFrame(email);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

// HRFrame: HR panel after login
class HRFrame extends Frame {
    String email;

    public HRFrame(String email) {
        this.email = email;
        setTitle("HR Panel");
        setSize(400, 300);
        setLayout(new FlowLayout());

        Label welcomeLabel = new Label("Welcome HR: " + email);
        add(welcomeLabel);

        Button viewEmployeesButton = new Button("View Employees");
        add(viewEmployeesButton);

        viewEmployeesButton.addActionListener(e -> new EmployeeListFrame());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setVisible(true);
    }
}

// EmployeeListFrame: Show employee details
class EmployeeListFrame extends Frame {
    public EmployeeListFrame() {
        setTitle("Employee List");
        setSize(600, 400);
        setLayout(new FlowLayout());

        String[] columns = {"Name", "Email", "Position", "Salary", "HRA", "Conveyance", "Medical", "Bonus", "Gross"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        try (Connection conn = DriverManager.getConnection(EmployeeManagementApp.DB_URL, EmployeeManagementApp.DB_USER, EmployeeManagementApp.DB_PASS)) {
            String query = "SELECT EMPName, Email, Position, Salary, HRA, Conveyance, Medical, Bonus, Gross FROM EmployeeDetails";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("EMPName"),
                        rs.getString("Email"),
                        rs.getString("Position"),
                        rs.getDouble("Salary"),
                        rs.getDouble("HRA"),
                        rs.getDouble("Conveyance"),
                        rs.getDouble("Medical"),
                        rs.getDouble("Bonus"),
                        rs.getDouble("Gross")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setVisible(true);
    }
}

// EmployeeFrame: Employee's personal panel
class EmployeeFrame extends Frame {
    String email;

    public EmployeeFrame(String email) {
        this.email = email;

        setTitle("Employee Panel");
        setSize(400, 300);
        setLayout(new FlowLayout());

        Label welcomeLabel = new Label("Welcome Employee: " + email);
        add(welcomeLabel);

        Button viewDetailsButton = new Button("View Details");
        add(viewDetailsButton);

        viewDetailsButton.addActionListener(e -> viewDetails(email));

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setVisible(true);
    }

    private void viewDetails(String email) {
        try (Connection conn = DriverManager.getConnection(EmployeeManagementApp.DB_URL, EmployeeManagementApp.DB_USER, EmployeeManagementApp.DB_PASS)) {
            String query = "SELECT * FROM EmployeeDetails WHERE Email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String details = String.format("Name: %s\nPosition: %s\nSalary: %.2f\nHRA: %.2f\nConveyance: %.2f\nMedical: %.2f\nBonus: %.2f\nGross: %.2f",
                        rs.getString("EMPName"),
                        rs.getString("Position"),
                        rs.getDouble("Salary"),
                        rs.getDouble("HRA"),
                        rs.getDouble("Conveyance"),
                        rs.getDouble("Medical"),
                        rs.getDouble("Bonus"),
                        rs.getDouble("Gross"));

                JOptionPane.showMessageDialog(this, details);
            } else {
                JOptionPane.showMessageDialog(this, "No details found.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
