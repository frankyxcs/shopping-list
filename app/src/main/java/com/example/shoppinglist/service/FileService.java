package com.example.shoppinglist.service;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.shoppinglist.ListConstants;
import com.example.shoppinglist.model.Item;
import com.example.shoppinglist.model.MarketItem;
import com.example.shoppinglist.model.MarketItemComparator;
import com.example.shoppinglist.model.MarketItems;
import com.example.shoppinglist.model.PreviouslyBoughtItem;
import com.example.shoppinglist.model.PreviouslyBoughtItems;
import com.example.shoppinglist.model.ShoppingList;
import com.example.shoppinglist.model.ShoppingListItem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringTokenizer;

public class FileService {
    private static final String LIST_FILE = "-listFile";
    private static final String BOUGHT_LIST_FILE = "-boughtListFile";
    private Context mContext;

    public FileService(Context context) {
        mContext = context;
    }

    public String getShoppingListFilename(String currentList) {
        return currentList + LIST_FILE;
    }

    public String getBoughtListFilename(String currentList) {
        return currentList + BOUGHT_LIST_FILE;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String getCurrentList(String filename) {
        if (!createFileIfNotExists(filename)) {
            return "";
        }

        String currentList = "";
        InputStreamReader inputStreamReader = openFile(filename);
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            currentList = null == line ? "" : line;
        } catch (IOException e) {
            Toast.makeText(mContext, "Failed to read from file", Toast.LENGTH_SHORT);
        }

        closeFile(inputStreamReader);

        return currentList;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void saveCurrentList(String filename, String currentList) {
        if (!createFileIfNotExists(filename) || currentList.isEmpty()) {
            return;
        }

        try (FileOutputStream fos = mContext.openFileOutput(filename, Context.MODE_PRIVATE)) {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(currentList);
            bw.newLine();
            bw.flush();
        }
        catch (IOException e) {
            Toast.makeText(mContext, "Failed to write to file", Toast.LENGTH_SHORT);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public ShoppingList readShoppingList(String filename) {

        MarketItems marketItems = this.readMarketItems(ListConstants.CATALOG);
        MarketItemComparator itemComparator = new MarketItemComparator(marketItems);

        if (!createFileIfNotExists(filename)) {
            return new ShoppingList(itemComparator);
        }

        InputStreamReader inputStreamReader = openFile(filename);
        if (inputStreamReader == null) {
            return new ShoppingList(itemComparator);
        }

        ShoppingList shoppingList = new ShoppingList(itemComparator);
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, ":");
                String itemName = tokenizer.nextToken();
                int isSelected = tokenizer.hasMoreTokens()
                        ? Integer.parseInt(tokenizer.nextToken()) : 0;
                int quantity = tokenizer.hasMoreTokens()
                        ? Integer.parseInt(tokenizer.nextToken()) : -1;
                Item item = new Item(itemName);
                shoppingList.add(item, quantity, isSelected > 0);
                line = reader.readLine();
            }
        } catch (IOException e) {
            Toast.makeText(mContext, "Failed to read from file", Toast.LENGTH_SHORT);
        }

        closeFile(inputStreamReader);

        return shoppingList;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public MarketItems readMarketItems(String filename) {
        if (!createFileIfNotExists(filename)) {
            return new MarketItems();
        }

        InputStreamReader inputStreamReader = openFile(filename);

        MarketItems marketItems = new MarketItems();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, ":");
                String itemName = tokenizer.nextToken();

                Item item = new Item(itemName);
                marketItems.add(item);

                line = reader.readLine();
            }
        } catch (IOException e) {
            Toast.makeText(mContext, "Failed to read from file", Toast.LENGTH_SHORT);
        }

        closeFile(inputStreamReader);

        return marketItems;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public PreviouslyBoughtItems readPreviouslyBoughtItems(String filename) {
        if (!createFileIfNotExists(filename)) {
            return new PreviouslyBoughtItems();
        }

        InputStreamReader inputStreamReader = openFile(filename);

        PreviouslyBoughtItems previouslyBoughtItems = new PreviouslyBoughtItems();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, ":");
                String itemName = tokenizer.nextToken();
                int lastBought = tokenizer.hasMoreTokens()
                        ? Integer.parseInt(tokenizer.nextToken()) : -1;

                Item item = new Item(itemName);
                PreviouslyBoughtItem previouslyBoughtItem = new PreviouslyBoughtItem(item, lastBought);
                previouslyBoughtItems.add(previouslyBoughtItem);

                line = reader.readLine();
            }
        } catch (IOException e) {
            Toast.makeText(mContext, "Failed to read from file", Toast.LENGTH_SHORT);
        }

        closeFile(inputStreamReader);

        return previouslyBoughtItems;
    }

    /**
     * Writes the specified items to the file.
     * This rewrites the file instead of appending, so only the selected items will be saved.
     * @param filename
     * @param items
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void saveShoppingList(String filename, List<ShoppingListItem> items) {
        if (!createFileIfNotExists(filename)) {
            return;
        }

        try (FileOutputStream fos = mContext.openFileOutput(filename, Context.MODE_PRIVATE)) {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            for (ShoppingListItem item: items) {
                bw.write(item.getItemName());
                bw.write(':');
                bw.write('0');
                bw.write(':');
                bw.write(Integer.toString(item.getQuantity()));
                bw.newLine();
            }

            bw.flush();
        }
        catch (IOException e) {
            Toast.makeText(mContext, "Failed to write to file", Toast.LENGTH_SHORT);
        }
    }

    /**
     * Writes the specified items to the file.
     * This rewrites the file instead of appending, so only the selected items will be saved.
     * @param filename
     * @param shoppingList
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void saveShoppingList(String filename, ShoppingList shoppingList) {
        if (!createFileIfNotExists(filename)) {
            return;
        }

        try (FileOutputStream fos = mContext.openFileOutput(filename, Context.MODE_PRIVATE)) {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            for (ShoppingListItem item: shoppingList.toList(false)) {
                bw.write(item.getItemName());
                bw.write(':');
                bw.write(item.isSelected() ? '1':'0');
                bw.write(':');
                bw.write(Integer.toString(item.getQuantity()));
                bw.newLine();
            }

            bw.flush();
        }
        catch (IOException e) {
            Toast.makeText(mContext, "Failed to write to file", Toast.LENGTH_SHORT);
        }
    }

    /**
     * Writes the specified items to the file.
     * @param filename
     * @param marketItems
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void saveMarketItems(String filename, MarketItems marketItems) {
        if (!createFileIfNotExists(filename)) {
            return;
        }

        try (FileOutputStream fos = mContext.openFileOutput(filename, Context.MODE_PRIVATE)) {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            for (MarketItem item: marketItems.toList()) {
                bw.write(item.getItemName());
                bw.newLine();
            }

            bw.flush();
        }
        catch (IOException e) {
            Toast.makeText(mContext, "Failed to write to file", Toast.LENGTH_SHORT);
        }
    }

    /**
     * Writes the specified items to the file.
     * @param filename
     * @param previouslyBoughtItems
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void savePreviouslyBoughtItems(String filename, PreviouslyBoughtItems previouslyBoughtItems) {
        if (!createFileIfNotExists(filename)) {
            return;
        }

        try (FileOutputStream fos = mContext.openFileOutput(filename, Context.MODE_PRIVATE)) {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            for (PreviouslyBoughtItem item: previouslyBoughtItems.toList()) {
                bw.write(item.getItemName());
                bw.write(':');
                bw.write(Integer.toString(item.getLastBought()));
                bw.newLine();
            }

            bw.flush();
        }
        catch (IOException e) {
            Toast.makeText(mContext, "Failed to write to file", Toast.LENGTH_SHORT);
        }
    }
    private boolean createFileIfNotExists(String filename) {
        File directory = mContext.getFilesDir();
        File file = new File(directory, filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            }
            catch (IOException e) {
                Toast.makeText(mContext, "Failed to create file", Toast.LENGTH_SHORT);
                return false;
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private InputStreamReader openFile(String filename) {
        FileInputStream fis;
        try {
            fis = mContext.openFileInput(filename);
        } catch (FileNotFoundException e) {
            Toast.makeText(mContext, "Could not find file", Toast.LENGTH_SHORT);
            return null;
        }
        InputStreamReader inputStreamReader =
                new InputStreamReader(fis, StandardCharsets.UTF_8);
        return inputStreamReader;
    }

    private void closeFile(InputStreamReader inputStreamReader) {
        if (inputStreamReader == null) {
            Toast.makeText(mContext, "Input stream is null", Toast.LENGTH_SHORT);
        }

        try {
            inputStreamReader.close();
        } catch (IOException e) {
            Toast.makeText(mContext, "Failed to close file", Toast.LENGTH_SHORT);
        }
    }
}
