package com.b_lam.resplash.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.Toast;

import com.b_lam.resplash.R;
import com.b_lam.resplash.data.data.Collection;
import com.b_lam.resplash.data.data.DeleteCollectionResult;
import com.b_lam.resplash.data.service.CollectionService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

public class EditCollectionDialog extends DialogFragment {

    private static final String TAG = "EditCollectionDialog";

    private CollectionService mService;

    @BindView(R.id.edit_collection_name_input_layout) TextInputLayout mNameTextInputLayout;
    @BindView(R.id.edit_collection_name) TextInputEditText mNameEditText;
    @BindView(R.id.edit_collection_description) TextInputEditText mDescriptionEditText;
    @BindView(R.id.edit_collection_make_private_checkbox) CheckBox mMakePrivateCheckBox;
    @BindView(R.id.edit_collection_confirm_delete_layout) ConstraintLayout mConfirmDeleteLayout;
    @BindView(R.id.edit_collection_action_button_layout) ConstraintLayout mActionButtonLayout;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_collection, null, false);
        ButterKnife.bind(this, view);

        mService = CollectionService.getService();

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.edit_collection_save_button)
    public void saveCollection() {
        if (!mNameEditText.getText().toString().isEmpty()) {
            mNameTextInputLayout.setError(null);
            mService.updateCollection(0, mNameEditText.getText().toString(),
                    mDescriptionEditText.getText().toString(),
                    mMakePrivateCheckBox.isChecked(),
                    new CollectionService.OnRequestACollectionListener() {
                        @Override
                        public void onRequestACollectionSuccess(Call<Collection> call, Response<Collection> response) {
                            hideKeyboard();
                            dismiss();
                        }

                        @Override
                        public void onRequestACollectionFailed(Call<Collection> call, Throwable t) {
                            Toast.makeText(getActivity(), R.string.failed_to_save_collection, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            mNameTextInputLayout.setError(getString(R.string.collection_name_is_required));
        }
    }

    @OnClick(R.id.edit_collection_cancel_button)
    public void cancelEditCollection() {
        dismiss();
    }

    @OnClick(R.id.edit_collection_delete_button)
    public void deleteCollection() {
        mConfirmDeleteLayout.setVisibility(View.VISIBLE);
        mActionButtonLayout.setVisibility(View.GONE);
    }

    @OnClick(R.id.edit_collection_no_button)
    public void deleteCollectionNo() {
        mConfirmDeleteLayout.setVisibility(View.GONE);
        mActionButtonLayout.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.edit_collection_yes_button)
    public void deleteCollectionYes() {
        mService.deleteCollection(0,
                new CollectionService.OnDeleteCollectionListener() {
                    @Override
                    public void onDeleteCollectionSuccess(Call<DeleteCollectionResult> call, Response<DeleteCollectionResult> response) {
                        hideKeyboard();
                    }

                    @Override
                    public void onDeleteCollectionFailed(Call<DeleteCollectionResult> call, Throwable t) {
                        Toast.makeText(getActivity(), R.string.failed_to_delete_collection, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void hideKeyboard() {
        View view = getDialog().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getDialog().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
