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

package com.example.android.architecture.blueprints.todoapp.data;


import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.example.android.architecture.blueprints.todoapp.data.model.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.remote.TasksRemoteDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.remote.TodoApi;
import com.example.android.architecture.blueprints.todoapp.data.source.remote.helper.ApiHelper;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.OkHttpClient;

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
public class FakeTasksRemoteDataSource extends TasksRemoteDataSource implements TasksDataSource {

    private static final Map<Integer, Task> TASKS_SERVICE_DATA = new LinkedHashMap<>();
    private static FakeTasksRemoteDataSource INSTANCE;

    // Prevent direct instantiation.
    private FakeTasksRemoteDataSource() {
        super(new ApiHelper(new OkHttpClient(), new Gson()).getAPI("http://faketodoapp.com/api", TodoApi.class));
    }

    public static FakeTasksRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeTasksRemoteDataSource();
        }
        return INSTANCE;
    }

    @Override
    public Single<List<Task>> getTasks() {
        List<Task> values = new ArrayList<>(TASKS_SERVICE_DATA.values());
        return Single.just(values);
    }

    @Override
    public Observable<Task> getTask(@NonNull Integer taskId) {
        Task task = TASKS_SERVICE_DATA.get(taskId);
        return Observable.just(task);
    }

    @Override
    public Completable saveTask(@NonNull Task task) {
        return Completable.fromAction(() -> TASKS_SERVICE_DATA.put(task.getId(), task));
    }


    @Override
    public Completable saveTasks(@NonNull List<Task> tasks) {
        return Observable.fromIterable(tasks)
                .doOnNext(this::saveTask)
                .ignoreElements();
    }

    @Override
    public Completable completeTask(@NonNull Task task) {
        return Completable.fromAction(() -> {
            Task completedTask = new Task(task.getTitle(), task.getDescription(), task.getId(), true);
            TASKS_SERVICE_DATA.put(task.getId(), completedTask);
        });
    }

    @Override
    public Completable completeTask(@NonNull Integer taskId) {
        return Completable.fromAction(() -> {
            Task task = TASKS_SERVICE_DATA.get(taskId);
            Task completedTask = new Task(task.getTitle(), task.getDescription(), task.getId(), true);
            TASKS_SERVICE_DATA.put(taskId, completedTask);
        });
    }

    @Override
    public Completable activateTask(@NonNull Task task) {
        return Completable.fromAction(() -> {
            Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());
            TASKS_SERVICE_DATA.put(task.getId(), activeTask);
        });
    }

    @Override
    public Completable activateTask(@NonNull Integer taskId) {
        return Completable.fromAction(() -> {
            Task task = TASKS_SERVICE_DATA.get(taskId);
            Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());
            TASKS_SERVICE_DATA.put(taskId, activeTask);
        });
    }

    @Override
    public Completable clearCompletedTasks() {
        return Completable.fromCallable(() -> {
            Iterator<Map.Entry<Integer, Task>> it = TASKS_SERVICE_DATA.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, Task> entry = it.next();
                if (entry.getValue().isCompleted()) {
                    it.remove();
                }
            }
            return it;
        });
    }

    public Completable refreshTasks() {
        return Completable.complete();
    }

    @Override
    public Completable deleteTask(@NonNull Integer taskId) {
        return Completable.fromCallable(() -> TASKS_SERVICE_DATA.remove(taskId));
    }

    @Override
    public Completable deleteAllTasks() {
        return Completable.fromAction(TASKS_SERVICE_DATA::clear);
    }

    @VisibleForTesting
    public void addTasks(Task... tasks) {
        for (Task task : tasks) {
            TASKS_SERVICE_DATA.put(task.getId(), task);
        }
    }
}
