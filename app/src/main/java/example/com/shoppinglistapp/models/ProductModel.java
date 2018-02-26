package example.com.shoppinglistapp.models;

public class ProductModel {
    private String productId, productName;
    private Boolean izInShoppingList;

    public ProductModel() {}

    public ProductModel(String productId, String productName, Boolean izInShoppingList) {
        this.productId = productId;
        this.productName = productName;
        this.izInShoppingList = izInShoppingList;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Boolean getIzInShoppingList() {
        return izInShoppingList;
    }
}