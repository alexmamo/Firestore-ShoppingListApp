package example.com.shoppinglistapp.holders;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import example.com.shoppinglistapp.MainActivity;
import example.com.shoppinglistapp.R;
import example.com.shoppinglistapp.ShoppingListActivity;
import example.com.shoppinglistapp.models.ShoppingListModel;

public class ShoppingListViewHolder extends RecyclerView.ViewHolder {
    private TextView shoppingListNameTextView, createdByTextView, dateTextView;

    public ShoppingListViewHolder(View itemView) {
        super(itemView);
        shoppingListNameTextView = itemView.findViewById(R.id.shopping_list_name_text_view);
        createdByTextView = itemView.findViewById(R.id.created_by_text_view);
        dateTextView = itemView.findViewById(R.id.date_text_view);
    }

    public void setShoppingList(Context context, String userEmail, ShoppingListModel shoppingListModel) {
        String shoppingListId = shoppingListModel.getShoppingListId();

        String shoppingListName = shoppingListModel.getShoppingListName();
        shoppingListNameTextView.setText(shoppingListName);

        String createdBy = "Created by: " + shoppingListModel.getCreatedBy();
        createdByTextView.setText(createdBy);

        Date date = shoppingListModel.getDate();
        if (date != null) {
            DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US);
            String shoppingListCreationDate = dateFormat.format(date);
            dateTextView.setText(shoppingListCreationDate);
        }

        itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), ShoppingListActivity.class);
            intent.putExtra("shoppingListModel", shoppingListModel);
            view.getContext().startActivity(intent);
        });

        itemView.setOnLongClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Edit Shopping List Name");

            EditText editText = new EditText(context);
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            editText.setText(shoppingListName);
            editText.setSelection(editText.getText().length());
            editText.setHint("Type a name");
            editText.setHintTextColor(Color.GRAY);
            builder.setView(editText);

            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            Map<String, Object> map = new HashMap<>();

            builder.setPositiveButton("Update", (dialogInterface, i) -> {
                String newShoppingListName = editText.getText().toString().trim();
                map.put("shoppingListName", newShoppingListName);
                rootRef.collection("shoppingLists").document(userEmail).collection("userShoppingLists").document(shoppingListId).update(map);
            });

            builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            return true;
        });
    }
}