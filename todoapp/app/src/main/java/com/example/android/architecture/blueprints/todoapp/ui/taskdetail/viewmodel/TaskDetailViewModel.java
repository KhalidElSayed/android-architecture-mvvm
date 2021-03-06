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

package com.example.android.architecture.blueprints.todoapp.ui.taskdetail.viewmodel;

import android.app.Activity;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.model.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.ui.base.viewmodel.BaseViewModel;
import com.example.android.architecture.blueprints.todoapp.ui.taskdetail.navigator.TaskDetailNavigator;
import com.example.android.architecture.blueprints.todoapp.ui.taskdetail.uimodel.TaskUiModel;
import com.example.android.architecture.blueprints.todoapp.ui.taskdetail.view.TaskDetailActivity;
import com.example.android.architecture.blueprints.todoapp.ui.taskdetail.view.TaskDetailFragment;
import com.google.common.base.Strings;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link TaskDetailFragment}), retrieves the data and edits,
 * deletes, updates, activates and completes the task.
 */
public class TaskDetailViewModel extends BaseViewModel {

  private TasksRepository mTasksRepository;

  @NonNull
  private TaskDetailNavigator mNavigator;

  /**
   * using a BehaviourSubject because we are interested in the last object that was emitted before
   * subscribing. Like this we ensure that the progress indicator has the correct visibility.
   */
  @NonNull
  private final BehaviorSubject<Boolean> mLoadingSubject;

  /**
   * using a PublishSubject because we are not interested in the last object that was emitted
   * before subscribing. Like this we avoid displaying the snackbar multiple times
   */
  @NonNull
  private final PublishSubject<Integer> mSnackbarText;

  @Inject
  public TaskDetailViewModel(@NonNull TasksRepository tasksRepository) {
    mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null!");
    mLoadingSubject = BehaviorSubject.createDefault(false);
    mSnackbarText = PublishSubject.create();
  }

  /**
   * @return a stream notifying on whether the loading is in progress or not
   */
  @NonNull
  public Observable<Boolean> getLoadingIndicatorVisibility() {
    return mLoadingSubject.hide();
  }

  /**
   * @return a stream containing the task model. An error will be emitted
   * if the task id is invalid. The loading is updated before retrieving the task and when the
   * task has been retrieved.
   */
  @NonNull
  public Observable<TaskUiModel> getTaskUiModel(@Nullable Integer taskId, @NonNull TaskDetailNavigator navigator) {
    mNavigator = checkNotNull(navigator, "navigator cannot be null");
    //if (Strings.isNullOrEmpty(mTaskId)) {
    if (taskId == null) {
      return Observable.error(new Exception("Task id null or empty"));
    }

    return mTasksRepository.getTask(taskId)
            .map(this::createModel)
            .doOnSubscribe(__ -> mLoadingSubject.onNext(true))
            .doOnNext(__ -> mLoadingSubject.onNext(false));
  }

  @NonNull
  private TaskUiModel createModel(Task task) {
    boolean isTitleVisible = !Strings.isNullOrEmpty(task.getTitle());
    boolean isDescriptionVisible = !Strings.isNullOrEmpty(task.getDescription());

    return new TaskUiModel(isTitleVisible, task.getTitle(), isDescriptionVisible,
            task.getDescription(), task.isCompleted());
  }

  /**
   * @return a stream that emits when a snackbar should be displayed. The stream contains the
   * snackbar text
   */
  @NonNull
  public Observable<Integer> getSnackbarText() {
    return mSnackbarText.hide();
  }

  /**
   * Handle the response received on onActivityResult.
   *
   * @param requestCode the request with which the Activity was opened.
   * @param resultCode  the result of the Activity.
   */
  public void handleActivityResult(int requestCode, int resultCode) {
    if (TaskDetailActivity.REQUEST_EDIT_TASK == requestCode
            && Activity.RESULT_OK == resultCode) {
      mNavigator.onTaskEdited();
    }
  }

  /**
   * @return a stream that completes when the task was edited.
   */
  @NonNull
  public Completable editTask(@Nullable Integer taskId) {
    return Completable.fromAction(() -> {
      //if (Strings.isNullOrEmpty(mTaskId)) {
      if (taskId == null) {
        throw new RuntimeException("Task id null or empty");
      }
      mNavigator.onStartEditTask(taskId);
    });
  }

  /**
   * Deletes the task from repository. Emits when the task has been deleted. An error is emitted
   * if the task id is invalid.
   *
   * @return a stream notifying about the deletion of the task
   */
  @NonNull
  public Completable deleteTask(@Nullable Integer taskId) {
    return Completable.fromAction(() -> {
//            if (Strings.isNullOrEmpty(mTaskId)) {
      if (taskId == null) {
        throw new RuntimeException("Task id null or empty");
      }
      mTasksRepository.deleteTask(taskId);
      mNavigator.onTaskDeleted();
    });
  }

  /**
   * Marks a task as active or completed in the repository. Emits when the task has been marked as
   * active or completed. An error is emitted if the task id is invalid.
   */
  @NonNull
  public Completable taskCheckChanged(@Nullable Integer taskId, final boolean checked) {
//        if (Strings.isNullOrEmpty(mTaskId)) {
    if (taskId == null) {
      return Completable.error(new RuntimeException("Task id null or empty"));
    }
    if (checked) {
      return completeTask(taskId);
    } else {
      return activateTask(taskId);
    }
  }

  /**
   * Marks a task as completed in the repository.
   */
  @NonNull
  private Completable completeTask(@Nullable Integer taskId) {
    return mTasksRepository.completeTask(taskId)
            .doOnComplete(() -> mSnackbarText.onNext(R.string.task_marked_complete));
  }

  /**
   * Marks a task as active in the repository.
   */
  @NonNull
  private Completable activateTask(@Nullable Integer taskId) {
    return mTasksRepository.activateTask(taskId)
            .doOnComplete(() -> mSnackbarText.onNext(R.string.task_marked_active));
  }

}
