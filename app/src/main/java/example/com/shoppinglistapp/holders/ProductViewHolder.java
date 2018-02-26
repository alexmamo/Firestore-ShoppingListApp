package example.com.shoppinglistapp.holders;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import example.com.shoppinglistapp.R;
import example.com.shoppinglistapp.models.NotificationModel;
import example.com.shoppinglistapp.models.ProductModel;
import example.com.shoppinglistapp.models.ShoppingListModel;

public class ProductViewHolder extends RecyclerView.ViewHolder {
    private TextView productNameTextView;

    public ProductViewHolder(View itemView) {
        super(itemView);
        productNameTextView = itemView.findViewById(R.id.product_name_text_view);
    }

    public void setProduct(Context context, View shoppingListViewFragment, String userEmail, String userName, ShoppingListModel shoppingListModel, ProductModel productModel) {
        String shoppingListId = shoppingListModel.getShoppingListId();
        String shoppingListName = shoppingListModel.getShoppingListName();
        String productId = productModel.getProductId();
        String productName = productModel.getProductName();
        Boolean izInShoppingList = productModel.getIzInShoppingList();
        productNameTextView.setText(productName);

        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        DocumentReference productIdRef = rootRef.collection("products").document(shoppingListId)
                .collection("shoppingListProducts").document(productId);

        itemView.setOnClickListener(view -> {
            Map<String, Object> map = new HashMap<>();
            if (izInShoppingList) {
                map.put("izInShoppingList", false);
            } else {
                map.put("izInShoppingList", true);
            }
            productIdRef.update(map).addOnSuccessListener(aVoid -> {
                if (izInShoppingList) {
                    rootRef.collection("shoppingLists").document(userEmail)
                            .collection("userShoppingLists").document(shoppingListId).get().addOnCompleteListener(task -> {
                                Map<String, Object> map1 = (Map<String, Object>) task.getResult().get("users");
                                String notificationMessage = userName + " just bought " + productName + " from " + shoppingListName + "'s list!";
                                NotificationModel notificationModel = new NotificationModel(notificationMessage, userEmail);

                                for (Map.Entry<String, Object> entry : map1.entrySet()) {
                                    String sharedUserEmail = entry.getKey();

                                    if (!sharedUserEmail.equals(userEmail)) {
                                        rootRef.collection("notifications").document(sharedUserEmail)
                                                .collection("userNotifications").document()
                                                .set(notificationModel);
                                    }
                                }
                            });
                }
            });
        });

        itemView.setOnLongClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Edit / Delete Product");

            EditText editText = new EditText(context);
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            editText.setText(productName);
            editText.setSelection(editText.getText().length());
            editText.setHint("Type a name");
            editText.setHintTextColor(Color.GRAY);
            builder.setView(editText);

            builder.setPositiveButton("Update", (dialogInterface, i) -> {
                String newProductName = editText.getText().toString().trim();
                Map<String, Object> map = new HashMap<>();
                map.put("productName", newProductName);
                productIdRef.update(map);
            });

            builder.setNegativeButton("Delete", (dialogInterface, i) -> {
                productIdRef.delete().addOnSuccessListener(aVoid -> Snackbar.make(shoppingListViewFragment, "Product deleted!", Snackbar.LENGTH_LONG).show());
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            return true;
        });
    }
}