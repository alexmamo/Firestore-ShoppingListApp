package example.com.shoppinglistapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import example.com.shoppinglistapp.fragments.HistoryFragment;
import example.com.shoppinglistapp.fragments.ShoppingListFragment;
import example.com.shoppinglistapp.models.ShoppingListModel;

public class ShoppingListActivity extends AppCompatActivity {
    private String userEmail;
    private GoogleApiClient googleApiClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore rootRef;
    private FirebaseAuth.AuthStateListener authStateListener;
    private ShoppingListModel shoppingListModel;
    private String shoppingListId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (googleSignInAccount != null) {
            userEmail = googleSignInAccount.getEmail();
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseFirestore.getInstance();

        authStateListener = firebaseAuth -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                Intent intent = new Intent(ShoppingListActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        };

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        shoppingListModel = (ShoppingListModel) getIntent().getSerializableExtra("shoppingListModel");
        String shoppingListName = shoppingListModel.getShoppingListName();
        setTitle(shoppingListName);
        shoppingListId = shoppingListModel.getShoppingListId();

        ViewPager viewPager = findViewById(R.id.view_pager);
        setupViewPager(viewPager);
        viewPager.setOffscreenPageLimit(2);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void signOut() {
        Map<String, Object> map = new HashMap<>();
        map.put("tokenId", FieldValue.delete());

        rootRef.collection("users").document(userEmail).update(map).addOnSuccessListener(aVoid -> {
            firebaseAuth.signOut();

            if (googleApiClient.isConnected()) {
                Auth.GoogleSignInApi.signOut(googleApiClient);
            }
        });
    }

    private void shareShoppingList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingListActivity.this);
        builder.setTitle("Share Shopping List");
        builder.setMessage("Please insert your friend's email");

        EditText editText = new EditText(ShoppingListActivity.this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editText.setHint("Type an email address");
        editText.setHintTextColor(Color.GRAY);
        builder.setView(editText);

        builder.setPositiveButton("Add", (dialogInterface, i) -> {
            String friendEmail = editText.getText().toString().trim();
            rootRef.collection("shoppingLists").document(friendEmail)
                    .collection("userShoppingLists").document(shoppingListId)
                    .set(shoppingListModel).addOnSuccessListener(aVoid -> {
                        Map<String, Object> users = new HashMap<>();
                        Map<String, Object> map = new HashMap<>();
                        map.put(userEmail, true);
                        map.put(friendEmail, true);
                        users.put("users", map);
                        rootRef.collection("shoppingLists").document(userEmail)
                                .collection("userShoppingLists").document(shoppingListId)
                                .update(users);
                        rootRef.collection("shoppingLists").document(friendEmail)
                                .collection("userShoppingLists").document(shoppingListId)
                                .update(users);
                    });
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shopping_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;

            case R.id.add_friend_button:
                shareShoppingList();
                return true;

            case R.id.sign_out_button:
                signOut();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> titleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titleList.get(position);
        }

        void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            titleList.add(title);
        }
    }

    public ShoppingListModel getShoppingListModel() {return shoppingListModel;}

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        ShoppingListFragment trueFragment = new ShoppingListFragment();
        Bundle trueBundle = new Bundle();
        trueBundle.putBoolean("izInShoppingList", true);
        trueFragment.setArguments(trueBundle);
        viewPagerAdapter.addFragment(trueFragment, "SHOPPING LIST");

        ShoppingListFragment falseFragment = new ShoppingListFragment();
        Bundle falseBundle = new Bundle();
        falseBundle.putBoolean("izInShoppingList", false);
        falseFragment.setArguments(falseBundle);
        viewPagerAdapter.addFragment(falseFragment, "PRODUCT LIST");

        viewPagerAdapter.addFragment(new HistoryFragment(), "HISTORY");

        viewPager.setAdapter(viewPagerAdapter);
    }
}