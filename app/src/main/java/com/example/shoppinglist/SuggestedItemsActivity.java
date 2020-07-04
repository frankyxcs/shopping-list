package com.example.shoppinglist;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;

import com.example.shoppinglist.model.MarketItems;
import com.example.shoppinglist.model.ShoppingList;
import com.example.shoppinglist.model.ShoppingListItem;

import java.util.ArrayList;

public class SuggestedItemsActivity extends AppCompatActivity implements ShoppingListViewAdapter.OnItemCheckListener,
        ShoppingListViewAdapter.OnLongClickListener, EditItemFragment.EditItemDialogListener {

    private ShoppingListViewAdapter recommendedListAdapter;
    private String currentList;
    private ShoppingList shoppingList;
    private ShoppingList suggestedItemList;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_items);

        // Retrieve the list
        Bundle bundle = getIntent().getBundleExtra("shoppingList");

        // Retrieve the list name
        currentList = bundle.getString("currentList");

        String filename = currentList + "-listFile";
        FileService fileService = new FileService(this);
        shoppingList = fileService.readShoppingList(filename);

        MarketItems marketItems = fileService.readMarketItems("catalog");
        SuggestedItemsService suggestedItemsService = new SuggestedItemsService(marketItems);

        // Initialize a shopping list with all the recommended items unselected
        suggestedItemList = suggestedItemsService.getSuggestedItems(shoppingList);

        // Set up the suggested list RecyclerView
        RecyclerView recommendedRecyclerView = findViewById(R.id.recommended_list);
        recommendedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recommendedListAdapter = new ShoppingListViewAdapter(this, suggestedItemList);
        recommendedListAdapter.setItemCheckListener(this);
        recommendedListAdapter.setLongClickListener(this);
        recommendedRecyclerView.setAdapter(recommendedListAdapter);

        // Set up the Done button
        Button doneButton = findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                openConfirmActivity();
            }
        });
    }

    @Override
    public void onItemCheck(int position) {
        ShoppingListItem item = recommendedListAdapter.getItem(position);
        suggestedItemList.select(item.getItemName());
    }

    @Override
    public void onItemUncheck(int position) {
        ShoppingListItem item = recommendedListAdapter.getItem(position);
        suggestedItemList.deselect(item.getItemName());
    }

    @Override
    public void onLongClick(ShoppingListItem item) {
        FragmentManager fm = getSupportFragmentManager();

        EditItemFragment alertDialog = EditItemFragment.newInstance(item.getItemName(), item.getQuantity());
        alertDialog.setEditItemDialogListener(this);
        alertDialog.show(fm, "fragment_alert");
    }

    @Override
    public void onItemEdit(String itemName, int quantity) {
        suggestedItemList.update(itemName, quantity);
        recommendedListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemDelete(String itemName) {
        suggestedItemList.remove(itemName);
        recommendedListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 2 && resultCode == 3) {
            // Finish the activity to go back to the main activity
            finish();
        }
    }

    private void openConfirmActivity(){
        // Add all the selected suggested items to the original shopping list
        shoppingList.addAll(suggestedItemList);

        Intent intent = new Intent(this, ConfirmActivity.class);

        Bundle bundle = new Bundle();
        ArrayList<Parcelable> parcelableList = new ArrayList<>();
        parcelableList.addAll(shoppingList.toList(true));
        bundle.putParcelableArrayList("data", parcelableList);
        bundle.putString("currentList", currentList);
        intent.putExtra("shoppingList", bundle);

        startActivityForResult(intent, 2);
    }
}
