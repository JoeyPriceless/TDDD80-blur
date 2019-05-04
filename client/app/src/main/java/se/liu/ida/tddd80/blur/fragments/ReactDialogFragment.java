package se.liu.ida.tddd80.blur.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import se.liu.ida.tddd80.blur.R;

import static android.app.AlertDialog.Builder;

public class ReactDialogFragment extends DialogFragment {
    public static String KEY_POST_ID = "POST_ID";
    public static String KEY_ADAPTER_POSITION = "ADAPTER_POSITION";
    public ReactDialogListener listener;
    private int index;
    private String postId;
    private int adapterPosition;

    public int getIndex() {
        return index;
    }

    private void setIndex(int i) {
        index = i;
    }

    public String getPostId() {
        return postId;
    }

    public int getAdapterPosition() {
        return adapterPosition;
    }

    public ReactDialogFragment() {
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        if (args != null) {
            postId = args.getString(KEY_POST_ID);
            adapterPosition = args.getInt(KEY_ADAPTER_POSITION);
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
    public void onAttach(Context activityContext) {
        super.onAttach(activityContext);
        Fragment targetFragment = getTargetFragment();
        if (targetFragment != null) {
            try {
                listener = (ReactDialogListener) targetFragment;
            } catch (ClassCastException e) {
                // The fragment doesn't implement the interface, throw exception
                throw new ClassCastException(targetFragment.getClass().getSimpleName()
                        + " must implement ReactDialogListener");
            }
        }
        else {
            try {
                listener = (ReactDialogListener) activityContext;
            } catch (ClassCastException e) {
                // The activity doesn't implement the interface, throw exception
                throw new ClassCastException(activityContext.getClass().getSimpleName()
                        + " must implement ReactDialogListener");
            }
        }
    }
}
