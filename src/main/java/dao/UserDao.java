package dao;

import java.sql.*;

public class UserDao {

    private final String DB_URL = "jdbc:derby:CSC311_DB;create=true";

    public boolean registerUser(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            String sql = "INSERT INTO users_auth (username, password) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            return false; // username already exists
        }
    }

    public boolean validateUser(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            String sql = "SELECT * FROM users_auth WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            return rs.next(); // true if match found

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}