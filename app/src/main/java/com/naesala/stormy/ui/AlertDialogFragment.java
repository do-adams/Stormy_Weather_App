package com.naesala.stormy.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

import com.naesala.stormy.R;

public class AlertDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        //getActivity() gives us the activity and hence, the context, in which this dialog is created

        //Builder is a nested (static inner) class in AlertDialog
        //This has to do with a software design pattern called
        //The factory method pattern
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.error_title)
                .setMessage(R.string.error_message)
                .setPositiveButton(R.string.error_ok_button_text, null);
        //Method chaining is awesome
        //You can chain methods when they return the same object type and address
        //In other words, chainable methods return "this"
        //You can pass in a null as an OnClickListener() if you don't want anything to happen
        //When the button is tapped
        AlertDialog dialog = builder.create();
        return dialog;
    }
}
