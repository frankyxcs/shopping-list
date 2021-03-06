package com.example.shoppinglist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.NumberPicker;
import android.widget.SearchView;

import com.example.shoppinglist.model.MarketItems;
import com.example.shoppinglist.service.FileService;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddFragment extends DialogFragment implements SearchView.OnQueryTextListener {

    public static final String TAG = "Add";

    private AddItemDialogListener mAddItemDialogListener;

    public AddFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AddFragment.
     */
    public static AddFragment newInstance() {
        AddFragment fragment = new AddFragment();
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.add_item);

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.fragment_add, (ViewGroup) getView(), false);
        alertDialogBuilder.setView(viewInflated);

        // Locate the EditText in listview_main.xml
        final AutoCompleteTextView autoTextView = viewInflated.findViewById(R.id.new_item);

        // Setup the market items in the autocomplete view
        FileService fileService = new FileService(getContext());
        MarketItems marketItems = fileService.readMarketItems(ListConstants.CATALOG);
        String[] itemNames = new String[marketItems.size()];
        for (int i=0; i<itemNames.length; i++) {
            itemNames[i] = marketItems.get(i).getName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (getContext(), android.R.layout.select_dialog_item, itemNames);
        autoTextView.setThreshold(1); // will start working from the first character
        autoTextView.setAdapter(adapter);

        final NumberPicker numberPickerView = viewInflated.findViewById(R.id.quantity);
        numberPickerView.setMinValue(1);
        numberPickerView.setMaxValue(10);
        numberPickerView.setValue(1);
        numberPickerView.setWrapSelectorWheel(false);

        alertDialogBuilder.setPositiveButton(R.string.ok,  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // On success
                mAddItemDialogListener.onItemAdd(autoTextView.getText().toString(), numberPickerView.getValue());
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        return alertDialogBuilder.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Show keyboard
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        mAddItemDialogListener.onItemAdd(s, 1);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    public void setAddItemDialogListener(AddItemDialogListener addItemDialogListener) {
        mAddItemDialogListener = addItemDialogListener;
    }

    public interface AddItemDialogListener {
        void onItemAdd(String inputText, int quantity);
    }
}
