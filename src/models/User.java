package models;

public class User {
    protected int id;
    protected String username;
    protected String role;

    public User(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public String getRole() {
        return role;
    }
    // You can add more getters/setters here
}