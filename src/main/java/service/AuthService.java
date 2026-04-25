package service;

import dao.UserDao;

public class AuthService {

    private final UserDao userDao = new UserDao();

    public boolean signUp(String username, String password) {

        if (username == null || username.isEmpty()) return false;
        if (password == null || password.length() < 4) return false;

        return userDao.registerUser(username, password);
    }

    public boolean login(String username, String password) {

        if (username == null || password == null) return false;

        return userDao.validateUser(username, password);
    }
}