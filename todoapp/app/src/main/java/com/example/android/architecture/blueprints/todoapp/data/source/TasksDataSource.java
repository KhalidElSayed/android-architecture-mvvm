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

import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Main entry point for accessing tasks data.
 */
public interface TasksDataSource {

    @NonNull
    Single<List<Task>> getTasks();

    @NonNull
    Observable<Task> getTask(@NonNull Integer taskId);

    @NonNull
    Completable saveTask(@NonNull Task task);

    @NonNull
    Completable saveTasks(@NonNull List<Task> tasks);

    @NonNull
    Completable completeTask(@NonNull Task task);

    @NonNull
    Completable completeTask(@NonNull Integer taskId);

    Completable activateTask(@NonNull Task task);

    Completable activateTask(@NonNull Integer taskId);

    Completable clearCompletedTasks();

    @NonNull
    Completable refreshTasks();

    Completable deleteTask(@NonNull Integer taskId);

    Completable deleteAllTasks();
}
