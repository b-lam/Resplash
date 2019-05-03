package com.b_lam.resplash.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.b_lam.resplash.R;
import com.b_lam.resplash.data.model.Collection;
import com.b_lam.resplash.data.model.DeleteCollectionResult;
import com.b_lam.resplash.data.service.CollectionService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

public class EditCollectionDialog extends DialogFragment {

    private Collection mCollection;
    private CollectionService mService;
    private EditCollectionDialogListener mEditCollectionDialogListener;

    @BindView(R.id.edit_collection_name_input_layout) TextInputLayout mNameTextInputLayout;
    @BindView(R.id.edit_collection_name) TextInputEditText mNameEditText;
    @BindView(R.id.edit_collection_description) TextInputEditText mDescriptionEditText;
    @BindView(R.id.edit_collection_make_private_checkbox) CheckBox mMakePrivateCheckBox;
    @BindView(R.id.edit_collection_confirm_delete_layout) ConstraintLayout mConfirmDeleteLayout;
    @BindView(R.id.edit_collection_action_button_layout) ConstraintLayout mActionButtonLayout;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_collection, null, false);
        ButterKnife.bind(this, view);

        mService = CollectionService.getService();

        mNameEditText.setText(mCollection.title);
        mDescriptionEditText.setText(mCollection.description);
        mMakePrivateCheckBox.setChecked(mCollection.privateX);

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
        if (mNameEditText.getText() != null && !mNameEditText.getText().toString().isEmpty()) {
            mNameTextInputLayout.setError(null);
            mService.updateCollection(mCollection.id, mNameEditText.getText().toString(),
                    (mDescriptionEditText.getText() == null) ? "" : mDescriptionEditText.getText().toString(),
                    mMakePrivateCheckBox.isChecked(),
                    new CollectionService.OnRequestACollectionListener() {
                        @Override
                        public void onRequestACollectionSuccess(Call<Collection> call, Response<Collection> response) {
                            if (response.isSuccessful()) {
                                mEditCollectionDialogListener.onCollectionUpdated(response.body());
                            }
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
        mService.deleteCollection(mCollection.id,
                new CollectionService.OnDeleteCollectionListener() {
                    @Override
                    public void onDeleteCollectionSuccess(Call<DeleteCollectionResult> call, Response<DeleteCollectionResult> response) {
                        mEditCollectionDialogListener.onCollectionDeleted();
                        hideKeyboard();
                        dismiss();
                    }

                    @Override
                    public void onDeleteCollectionFailed(Call<DeleteCollectionResult> call, Throwable t) {
                        Toast.makeText(getActivity(), R.string.failed_to_delete_collection, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void setCollection(Collection collection) {
        mCollection = collection;
    }

    public void setListener(EditCollectionDialogListener editCollectionDialogListener) {
        mEditCollectionDialogListener = editCollectionDialogListener;
    }

    private void hideKeyboard() {
        if (getDialog() != null) {
            View view = getDialog().getCurrentFocus();
            if (view != null) {
                InputMethodManager inputManager = (InputMethodManager) getDialog().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public interface EditCollectionDialogListener {
        void onCollectionUpdated(Collection collection);
        void onCollectionDeleted();
    }
}
