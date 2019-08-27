/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.ui.addedittask.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.ui.addedittask.uimodel.AddEditTaskUiModel;
import com.example.android.architecture.blueprints.todoapp.ui.addedittask.viewmodel.AddEditTaskViewModel;
import com.example.android.architecture.blueprints.todoapp.ui.base.view.BaseFragment;
import com.example.android.architecture.blueprints.todoapp.ui.base.viewmodel.ViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProviders;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Main UI for the add task screen. Users can enter a task title and description.
 */
public class AddEditTaskFragment extends BaseFragment {

    public static final String ARGUMENT_EDIT_TASK_ID = "EDIT_TASK_ID";
    private static final String TASK_TITLE_KEY = "title";
    private static final String TASK_DESCRIPTION_KEY = "description";
    private static final String TAG = AddEditTaskFragment.class.getSimpleName();

    private TextView mTitle;

    private TextView mDescription;

    @Inject
    ViewModelFactory viewModelFactory;
    @Nullable
    private AddEditTaskViewModel mViewModel;

    /**
     * using a CompositeSubscription to gather all the subscriptions
     * so all of them can be later unsubscribed together
     * */
    @Inject
    CompositeDisposable mDisposable;

    public static AddEditTaskFragment newInstance(String taskId) {
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_EDIT_TASK_ID, taskId);
        AddEditTaskFragment fragment = new AddEditTaskFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.addtask_frag, container, false);
        mTitle = root.findViewById(R.id.add_task_title);
        mDescription = root.findViewById(R.id.add_task_description);
        setHasOptionsMenu(true);

        setupFab();

        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(AddEditTaskViewModel.class);
        restoreData(savedInstanceState);

        return root;
    }

    @Override
    public void onResume() {
        bindViewModel();
        super.onResume();
    }

    @Override
    public void onPause() {
        unbindViewModel();
        super.onPause();
    }

    private void bindViewModel() {
        // subscribe to the emissions of the snackbar text.
        // whenever a new snackbar text is emitted, show the snackbar
        mDisposable.add(mViewModel.getSnackbarText()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        // onNext
                        this::showSnackbar,
                        // onError
                        throwable -> Log.e(TAG, "Error retrieving snackbar text", throwable)));

        // The ViewModel holds an observable containing the state of the UI.
        // subscribe to the emissions of the UiModel
        // every time a new UiModel is emitted update the Ui
        mDisposable.add(mViewModel.getUiModel(getTaskId())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        // onNext
                        this::updateUi,
                        // onError
                        throwable -> Log.e(TAG, "Error retrieving the task", throwable)));

    }

    private void unbindViewModel() {
        // disposing from all the subscriptions to ensure we don't have any memory leaks
        mDisposable.dispose();
    }

    private void restoreData(@Nullable Bundle bundle) {
        if (bundle == null) {
            return;
        }
        /*mViewModel.setRestoredState(bundle.getString(TASK_TITLE_KEY),
                bundle.getString(TASK_DESCRIPTION_KEY));*/
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(TASK_TITLE_KEY, mTitle.getText().toString());
        outState.putString(TASK_DESCRIPTION_KEY, mDescription.getText().toString());
        super.onSaveInstanceState(outState);
    }

    private void setupFab() {
        FloatingActionButton fab =
                getActivity().findViewById(R.id.fab_edit_task_done);
        fab.setImageResource(R.drawable.ic_done);
        fab.setOnClickListener(__ -> saveTask());
    }

    private void saveTask() {
        String title = mTitle.getText().toString();
        String description = mDescription.getText().toString();
        mDisposable.add(mViewModel.saveTask(getTaskId(), title, description)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        // onNext
                        () -> {
                            // nothing to do here
                        },
                        // onError
                        throwable -> Log.e(TAG, "Error saving task", throwable)));
    }

    private void showSnackbar(@StringRes Integer textId) {
        Snackbar.make(mTitle, textId, Snackbar.LENGTH_LONG).show();
    }

    private void updateUi(AddEditTaskUiModel model) {
        mTitle.setText(model.getTitle());
        mDescription.setText(model.getDescription());
    }

    private Integer getTaskId() {
        if (getArguments() != null) {
            return getArguments().getInt(ARGUMENT_EDIT_TASK_ID);
        }
        return null;
    }
}