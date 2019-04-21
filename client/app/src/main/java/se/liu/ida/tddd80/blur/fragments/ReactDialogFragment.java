package se.liu.ida.tddd80.blur.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import se.liu.ida.tddd80.blur.R;

import static android.app.AlertDialog.*;

public class ReactDialogFragment extends DialogFragment {
    public ReactDialogListener listener;
    private int index;

    public int getIndex() {
        return index;
    }

    private void setIndex(int i) {
        index = i;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose a reaction")
                .setItems(getResources().getStringArray(R.array.reaction_strings),
                        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                setIndex(index);
                listener.onClickReactionDialog(ReactDialogFragment.this);
            }
        });
        return builder.create();
    }

    public interface ReactDialogListener {
        void onClickReactionDialog(ReactDialogFragment dialog);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (ReactDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.getClass().toString()
                    + " must implement ReactDialogListener");
        }
    }
}
