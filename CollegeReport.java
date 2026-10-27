import java.sql.*;
import java.util.Scanner;

public class CollegeReport {

    static Connection con;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/college";
        String user = "root";
        String password = "your_password";

        try {
            con = DriverManager.getConnection(url, user, password);

            int choice;
            do {
                System.out.println("\n--- College Report Generator ---");
                System.out.println("1. Add department");
                System.out.println("2. Add student");
                System.out.println("3. Count students per department");
                System.out.println("4. Department with the most students");
                System.out.println("5. Students department-wise (JOIN)");
                System.out.println("6. Exit");
                System.out.print("Enter choice: ");

                try {
                    choice = Integer.parseInt(sc.nextLine());
                } catch (NumberFormatException e) {
                    choice = 0;
                }

                try {
                    switch (choice) {
                        case 1: addDepartment(); break;
                        case 2: addStudent(); break;
                        case 3: countPerDepartment(); break;
                        case 4: topDepartment(); break;
                        case 5: studentsDepartmentWise(); break;
                        case 6: System.out.println("Goodbye."); break;
                        default: System.out.println("Invalid choice.");
                    }
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Not saved: duplicate ID/name, or the department "
                        + "ID does not exist.");
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number.");
                } catch (SQLException e) {
                    System.out.println("Database error: " + e.getMessage());
                }
            } while (choice != 6);

            con.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void addDepartment() throws SQLException {
        System.out.print("Department ID: ");
        int id = Integer.parseInt(sc.nextLine());
        System.out.print("Department name: ");
        String name = sc.nextLine().trim();
        System.out.print("HOD name: ");
        String hod = sc.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Department name cannot be empty.");
            return;
        }

        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO departments VALUES (?, ?, ?)");
        ps.setInt(1, id);
        ps.setString(2, name);
        ps.setString(3, hod);
        ps.executeUpdate();
        System.out.println("Department added.");
    }

    static void addStudent() throws SQLException {
        System.out.print("Student ID: ");
        int id = Integer.parseInt(sc.nextLine());
        System.out.print("Student name: ");
        String name = sc.nextLine().trim();
        showDepartments();
        System.out.print("Department ID: ");
        int deptId = Integer.parseInt(sc.nextLine());

        if (name.isEmpty()) {
            System.out.println("Student name cannot be empty.");
            return;
        }

        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO students VALUES (?, ?, ?)");
        ps.setInt(1, id);
        ps.setString(2, name);
        ps.setInt(3, deptId);
        ps.executeUpdate();
        System.out.println("Student added.");
    }

    static void countPerDepartment() throws SQLException {
        ResultSet rs = con.createStatement().executeQuery(
            "SELECT d.dept_name, COUNT(s.student_id) AS total " +
            "FROM departments d LEFT JOIN students s ON d.dept_id = s.dept_id " +
            "GROUP BY d.dept_id, d.dept_name ORDER BY total DESC, d.dept_name");

        System.out.printf("%n%-15s %s%n", "Department", "Students");
        while (rs.next()) {
            System.out.printf("%-15s %d%n",
                rs.getString("dept_name"), rs.getInt("total"));
        }
    }

    static void topDepartment() throws SQLException {
        ResultSet rs = con.createStatement().executeQuery(
            "SELECT d.dept_name, COUNT(s.student_id) AS total " +
            "FROM departments d LEFT JOIN students s ON d.dept_id = s.dept_id " +
            "GROUP BY d.dept_id, d.dept_name " +
            "HAVING COUNT(s.student_id) = (" +
            "  SELECT MAX(cnt) FROM (" +
            "    SELECT COUNT(*) AS cnt FROM students GROUP BY dept_id) AS counts)");

        int count = 0;
        while (rs.next()) {
            if (count == 0) {
                System.out.println("\nDepartment(s) with the most students ("
                    + rs.getInt("total") + "):");
            }
            System.out.println("  " + rs.getString("dept_name"));
            count++;
        }
        if (count == 0) System.out.println("No students in the database yet.");
    }

    static void studentsDepartmentWise() throws SQLException {
        ResultSet rs = con.createStatement().executeQuery(
            "SELECT d.dept_name, d.hod_name, s.student_id, s.student_name " +
            "FROM departments d LEFT JOIN students s ON d.dept_id = s.dept_id " +
            "ORDER BY d.dept_name, s.student_name");

        String current = "";
        while (rs.next()) {
            String dept = rs.getString("dept_name");
            if (!dept.equals(current)) {
                System.out.println("\n" + dept + " (HOD: " + rs.getString("hod_name") + ")");
                current = dept;
            }
            int sid = rs.getInt("student_id");
            if (rs.wasNull()) {
                System.out.println("   (no students)");
            } else {
                System.out.println("   " + sid + "  " + rs.getString("student_name"));
            }
        }
    }

    static void showDepartments() throws SQLException {
        ResultSet rs = con.createStatement().executeQuery(
            "SELECT * FROM departments ORDER BY dept_id");
        System.out.println("Departments:");
        while (rs.next()) {
            System.out.println("  " + rs.getInt("dept_id") + " - " + rs.getString("dept_name"));
        }
    }
}