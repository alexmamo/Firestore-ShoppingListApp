package example.com.shoppinglistapp.models;

public class UserModel {
    private String userEmail, userName, tokenId;

    public UserModel() {}

    public UserModel(String userEmail, String userName, String tokenId) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.tokenId = tokenId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public String getTokenId() {
        return tokenId;
    }
}