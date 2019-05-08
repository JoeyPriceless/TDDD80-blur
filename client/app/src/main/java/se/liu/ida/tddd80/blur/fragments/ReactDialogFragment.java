package se.liu.ida.tddd80.blur.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.models.ReactionType;

public class ReactDialogFragment extends DialogFragment {
    public static String KEY_POST_ID = "POST_ID";
    public static String KEY_ADAPTER_POSITION = "ADAPTER_POSITION";
    public static String KEY_CURRENT_SELECTION = "CURRENT_SELECTION";
    public ReactDialogListener listener;
    private int index;
    private String postId;
    private int adapterPosition;
    private int currentSelection;

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
            // adapterPosition may be null. Only used when post is interacted with in a recyclerview
            adapterPosition = args.getInt(KEY_ADAPTER_POSITION);
            currentSelection = args.getInt(KEY_CURRENT_SELECTION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_reaction, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final LinearLayout linear = view.findViewById(R.id.linearlayout_dialog_reactions);
        for (int i = 0; i < linear.getChildCount(); i++) {
            final int j = i;
            final ImageView iv = (ImageView)linear.getChildAt(j);
            if (currentSelection == i + 1) {
                setWeight(iv, 3f);
            }
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setWeight(iv, 3f);
                    setIndex(j);
                    listener.onClickReactionDialog(ReactDialogFragment.this);
                    dismiss();
                }
            });
        }
    }

    private void setWeight(View v, float weight) {
            v.setLayoutParams(new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    weight));
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public interface ReactDialogListener {
        void onClickReactionDialog(ReactDialogFragment dialog);
    }

    @Override
    public void onAttach(Context activityContext) {
        super.onAttach(activityContext);
        // TargetFragment is set when the dialog is launched through a Fragment rather than an
        // activity.
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
