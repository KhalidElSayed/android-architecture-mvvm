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

package com.example.android.architecture.blueprints.todoapp.data.source;


import com.example.android.architecture.blueprints.todoapp.data.model.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.remote.TasksRemoteDataSource;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.BaseSchedulerProvider;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Concrete implementation to load tasks from the data sources into a cache.
 * <p/>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */
@Singleton
public class TasksRepository implements TasksDataSource {

    @NonNull
    private final TasksDataSource mTasksRemoteDataSource;

    @NonNull
    private final TasksDataSource mTasksLocalDataSource;

    @NonNull
    private final BaseSchedulerProvider mBaseSchedulerProvider;

    @Inject
    public TasksRepository(@NonNull TasksRemoteDataSource tasksRemoteDataSource,
                            @NonNull TasksLocalDataSource tasksLocalDataSource,
                            @NonNull BaseSchedulerProvider schedulerProvider) {
        mTasksRemoteDataSource = checkNotNull(tasksRemoteDataSource);
        mTasksLocalDataSource = checkNotNull(tasksLocalDataSource);
        mBaseSchedulerProvider = checkNotNull(schedulerProvider);
    }

    /**
     * Gets tasks from  local data source (Room Db).
     */
    @Override
    public Single<List<Task>> getTasks() {
        return mTasksLocalDataSource.getTasks();
    }

    /**
     * Saves a task in the local and then in the remote repository
     *
     * @param task the task to be saved
     * @return a completable that emits when the task was saved or in case of error.
     */
    @NonNull
    @Override
    public Completable saveTask(@NonNull Task task) {
        checkNotNull(task);
        return mTasksLocalDataSource.saveTask(task)
                .andThen(mTasksRemoteDataSource.saveTask(task));
    }

    /**
     * Saves a list of tasks in the local and then in the remote repository
     *
     * @param tasks the tasks to be saved
     * @return a completable that emits when the tasks were saved or in case of error.
     */
    @Override
    public Completable saveTasks(@NonNull List<Task> tasks) {
        checkNotNull(tasks);
        return mTasksLocalDataSource.saveTasks(tasks)
                .andThen(mTasksRemoteDataSource.saveTasks(tasks));
    }

    @Override
    public Completable completeTask(@NonNull Task task) {
        checkNotNull(task);
        return mTasksLocalDataSource.completeTask(task)
                .andThen(mTasksRemoteDataSource.completeTask(task));
    }

    @Override
    public Completable completeTask(@NonNull Integer taskId) {
        checkNotNull(taskId);
        return mTasksLocalDataSource.completeTask(taskId)
                .andThen(mTasksRemoteDataSource.completeTask(taskId));
    }

    @Override
    public Completable activateTask(@NonNull Task task) {
        checkNotNull(task);
        return mTasksLocalDataSource.activateTask(task)
                .andThen(mTasksRemoteDataSource.activateTask(task));
    }

    @Override
    public Completable activateTask(@NonNull Integer taskId) {
        checkNotNull(taskId);
        return mTasksLocalDataSource.activateTask(taskId)
                .andThen(mTasksRemoteDataSource.activateTask(taskId));
    }

    @Override
    public Completable clearCompletedTasks() {
        return mTasksRemoteDataSource.clearCompletedTasks()
                .andThen(mTasksLocalDataSource.clearCompletedTasks());
    }

    /**
     * Gets task from local data source (room db).
     */
    @Override
    public Observable<Task> getTask(@NonNull final Integer taskId) {
        checkNotNull(taskId);
        return mTasksLocalDataSource.getTask(taskId);
    }

    /**
     * Get the tasks from the remote data source and save them in the local data source.
     */
    @Override
    public Completable refreshTasks() {
        return mTasksRemoteDataSource.getTasks()
                .subscribeOn(mBaseSchedulerProvider.io())
                .doOnSuccess(mTasksLocalDataSource::saveTasks)
                .ignoreElement();
    }

    /**
     * Delete tasks from remote and local repositories.
     */
    @Override
    public Completable deleteAllTasks() {
      return mTasksRemoteDataSource.deleteAllTasks()
              .andThen(mTasksLocalDataSource.deleteAllTasks());
    }

    /**
     * Delete a task based on the task id from remote and local repositories.
     *
     * @param taskId a task id
     */
    @Override
    public Completable deleteTask(@NonNull Integer taskId) {
      return mTasksRemoteDataSource.deleteTask(checkNotNull(taskId))
              .andThen(mTasksLocalDataSource.deleteTask(checkNotNull(taskId)));
    }
}
