package com.example.exoExplorer.dto;

/**
 * Data Transfer Object for user profile information.
 */
public class UserProfileDTO {
    private String email;
    private String firstName;
    private String lastName;
    private boolean darkMode;
    private String language;
    private int favoriteCount;

    /**
     * Default constructor.
     */
    public UserProfileDTO() {}

    /**
     * Constructor with basic user information.
     *
     * @param email User's email
     * @param firstName User's first name
     * @param lastName User's last name
     */
    public UserProfileDTO(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Full constructor with all user information.
     *
     * @param email User's email
     * @param firstName User's first name
     * @param lastName User's last name
     * @param darkMode User's dark mode preference
     * @param language User's language preference
     * @param favoriteCount Count of user's favorite exoplanets
     */
    public UserProfileDTO(String email, String firstName, String lastName, boolean darkMode, String language, int favoriteCount) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.darkMode = darkMode;
        this.language = language;
        this.favoriteCount = favoriteCount;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public boolean isDarkMode() { return darkMode; }
    public void setDarkMode(boolean darkMode) { this.darkMode = darkMode; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public int getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(int favoriteCount) { this.favoriteCount = favoriteCount; }
}