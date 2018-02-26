package example.com.shoppinglistapp.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class ShoppingListModel implements Serializable {
    private String shoppingListId, shoppingListName, createdBy;
    @ServerTimestamp
    private Date date;

    public ShoppingListModel() {}

    public ShoppingListModel(String shoppingListId, String shoppingListName, String createdBy) {
        this.shoppingListId = shoppingListId;
        this.shoppingListName = shoppingListName;
        this.createdBy = createdBy;
    }

    public String getShoppingListId() {
        return shoppingListId;
    }

    public String getShoppingListName() {
        return shoppingListName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Date getDate() {
        return date;
    }
}