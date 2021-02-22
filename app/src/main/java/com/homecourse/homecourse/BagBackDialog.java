package com.homecourse.homecourse;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class BagBackDialog extends AppCompatDialogFragment {
    private TextView textViewName;
    private TextView textViewBagSlot;
    private BagBackDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_bag_back_dialog, null);

        builder.setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        textViewName = view.findViewById(R.id.dialog_name);
        textViewBagSlot = view.findViewById(R.id.dialog_bag_slot);

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (BagBackDialogListener) context;
        } catch (ClassCastException e) {
            throw  new ClassCastException(context.toString() + "must implement BagBackDialogListener");
        }
    }

    public interface BagBackDialogListener{
        void storeBag(String name, String bagSlot);
    }

}
