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
    public static String KEY_POST_ID = "POST_ID";
    public static String KEY_BUTTON_ID = "BUTTON_ID";
    public ReactDialogListener listener;
    private int index;
    private String postId;
    private int buttonId;

    public int getIndex() {
        return index;
    }

    private void setIndex(int i) {
        index = i;
    }

    public String getPostId() {
        return postId;
    }

    public int getButtonId() {
        return buttonId;
    }

    public ReactDialogFragment() {
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        if (args != null) {
            postId = args.getString(KEY_POST_ID);
            buttonId = args.getInt(KEY_BUTTON_ID);
        }
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
