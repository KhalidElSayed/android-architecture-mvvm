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

package com.example.android.architecture.blueprints.todoapp.data.source.local;

import com.example.android.architecture.blueprints.todoapp.data.model.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.local.dao.TaskDao;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Concrete implementation of a data source as a db.
 */
@Singleton
public class TasksLocalDataSource implements TasksDataSource {

    @NonNull
    private final TaskDao mTaskDao;

    @Inject
    public TasksLocalDataSource(TaskDao taskDao) {
        mTaskDao = taskDao;
    }

    /**
     * @return an Observable that emits the list of tasks in the database, every time the Tasks
     * table is modified
     */
    @Override
    public Single<List<Task>> getTasks() {
        return mTaskDao.getTasks().firstOrError();
    }

    @Override
    public Observable<Task> getTask(@NonNull Integer taskId) {
        return mTaskDao.getTask(taskId).toObservable();
    }

    @Override
    public Completable saveTask(@NonNull Task task) {
        checkNotNull(task);
        return mTaskDao.insertTask(task);
    }

    @Override
    public Completable saveTasks(@NonNull List<Task> tasks) {
        checkNotNull(tasks);
        return mTaskDao.insertTasks(tasks);
    }

    @Override
    public Completable completeTask(@NonNull Task task) {
        checkNotNull(task);
        return completeTask(task.getId());
    }

    @Override
    public Completable completeTask(@NonNull Integer taskId) {
        return mTaskDao.updateTask(1, taskId);
    }

    @Override
    public Completable activateTask(@NonNull Task task) {
        return activateTask(task.getId());
    }

    @Override
    public Completable activateTask(@NonNull Integer taskId) {
        return mTaskDao.updateTask(0, taskId);
    }

    @Override
    public Completable clearCompletedTasks() {
        return mTaskDao.deleteTask(1);
    }

    @Override
    public Completable refreshTasks() {
        // Not required because the {@link TasksRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
        return Completable.complete();
    }

    @Override
    public Completable deleteTask(@NonNull Integer taskId) {
        return mTaskDao.deleteTask(taskId);
    }

    @Override
    public Completable deleteAllTasks() {
        return mTaskDao.deleteAllTasks();
    }
}
