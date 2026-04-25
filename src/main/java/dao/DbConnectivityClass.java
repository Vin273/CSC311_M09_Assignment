package dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Major;
import model.Person;
import service.MyLogger;

import java.sql.*;

public class DbConnectivityClass {
    final static String DB_NAME = "CSC311_DB;create=true";
    MyLogger lg = new MyLogger();
    //    final static String SQL_SERVER_URL = "jdbc:mysql://server.mariadb.database.azure.com";//update this server name
    final static String DB_URL = "jdbc:derby:" + DB_NAME;//update this database name
//    final static String USERNAME = "csc311admin@server";// update this username
//    final static String PASSWORD = "FARM";// update this password


    private final ObservableList<Person> data = FXCollections.observableArrayList();

    // Method to retrieve all data from the database and store it into an observable list to use in the GUI tableview.


    public ObservableList<Person> getData() {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            String sql = "SELECT * FROM users ";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.isBeforeFirst()) {
                lg.makeLog("No data");
            }
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String department = resultSet.getString("department");
                Major major = Major.valueOf(resultSet.getString("major"));
                String email = resultSet.getString("email");
                String imageURL = resultSet.getString("imageURL");
                data.add(new Person(id, first_name, last_name, department, major, email, imageURL));
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }


    public void connectToDatabase() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

            Connection conn = DriverManager.getConnection(DB_URL);

            Statement stmt = conn.createStatement();

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                            "first_name VARCHAR(200) NOT NULL, " +
                            "last_name VARCHAR(200) NOT NULL, " +
                            "department VARCHAR(200), " +
                            "major VARCHAR(200), " +
                            "email VARCHAR(200) UNIQUE, " +
                            "imageURL VARCHAR(200))"
            );

            stmt.close();
            conn.close();

        } catch (SQLException e) {
            // Table already exists → ignore
            if (!e.getSQLState().equals("X0Y32")) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void queryUserByLastName(String name) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            String sql = "SELECT * FROM users WHERE last_name = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, name);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String major = resultSet.getString("major");
                String department = resultSet.getString("department");

                lg.makeLog("ID: " + id + ", Name: " + first_name + " " + last_name + " "
                        + ", Major: " + major + ", Department: " + department);
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void listAllUsers() {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            String sql = "SELECT * FROM users ";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String department = resultSet.getString("department");
                String major = resultSet.getString("major");
                String email = resultSet.getString("email");

                lg.makeLog("ID: " + id + ", Name: " + first_name + " " + last_name + " "
                        + ", Department: " + department + ", Major: " + major + ", Email: " + email);
            }

            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertUser(Person person) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            String sql = "INSERT INTO users (first_name, last_name, department, major, email, imageURL) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, person.getFirstName());
            preparedStatement.setString(2, person.getLastName());
            preparedStatement.setString(3, person.getDepartment());
            preparedStatement.setString(4, String.valueOf(person.getMajor()));
            preparedStatement.setString(5, person.getEmail());
            preparedStatement.setString(6, person.getImageURL());
            int row = preparedStatement.executeUpdate();
            if (row > 0) {
                lg.makeLog("A new user was inserted successfully.");
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void editUser(int id, Person p) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            String sql = "UPDATE users SET first_name=?, last_name=?, department=?, major=?, email=?, imageURL=? WHERE id=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, p.getFirstName());
            preparedStatement.setString(2, p.getLastName());
            preparedStatement.setString(3, p.getDepartment());
            preparedStatement.setString(4, String.valueOf(p.getMajor()));
            preparedStatement.setString(5, p.getEmail());
            preparedStatement.setString(6, p.getImageURL());
            preparedStatement.setInt(7, id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteRecord(Person person) {
        int id = person.getId();
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            String sql = "DELETE FROM users WHERE id=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Method to retrieve id from database where it is auto-incremented.
    public int retrieveId(Person p) {
        connectToDatabase();
        int id;
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            String sql = "SELECT id FROM users WHERE email=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, p.getEmail());

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            id = resultSet.getInt("id");
            preparedStatement.close();
            conn.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        lg.makeLog(String.valueOf(id));
        return id;
    }
}