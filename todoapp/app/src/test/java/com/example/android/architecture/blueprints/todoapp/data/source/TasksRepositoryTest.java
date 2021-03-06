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

import android.content.Context;

import com.example.android.architecture.blueprints.todoapp.data.model.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.remote.TasksRemoteDataSource;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.ImmediateSchedulerProvider;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
public class TasksRepositoryTest {

    private final static String TASK_TITLE = "title";

    private static List<Task> TASKS = Lists.newArrayList(new Task("Title1", "Description1", 1),
            new Task("Title2", "Description2", 2));

    private final static Task ACTIVE_TASK = new Task(TASK_TITLE, "Some Task Description", 10);

    private final static Task COMPLETED_TASK = new Task(TASK_TITLE, "Some Task Description", 20, true);

    private TasksRepository mTasksRepository;

    private TestObserver<List<Task>> mTasksTestSubscriber;

    @Mock
    private TasksRemoteDataSource mTasksRemoteDataSource;

    @Mock
    private TasksLocalDataSource mTasksLocalDataSource;

    @Mock
    private Context mContext;

    private TestObserver mTestSubscriber = new TestObserver();

    @Before
    public void setupTasksRepository() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mTasksRepository = new TasksRepository(
                mTasksRemoteDataSource, mTasksLocalDataSource, new ImmediateSchedulerProvider());

        mTasksTestSubscriber = new TestObserver<>();
    }

    @Test
    public void getTasks_requestsAllTasksFromLocalDataSource() {
        // Given that the local data source has data available
        // And the remote data source does not have any data available
        new ArrangeBuilder()
                .withTasksAvailable(mTasksLocalDataSource, TASKS);

        // When tasks are requested from the tasks repository
        mTasksRepository.getTasks().subscribe(mTasksTestSubscriber);

        // Then tasks are loaded from the local data source
        verify(mTasksLocalDataSource).getTasks();
        mTasksTestSubscriber.assertValue(TASKS);
    }

    @Test
    public void saveTasks_savesTasksToRemoteDataSource() {
        // Given that a task is saved successfully in local and remote data sources
        new ArrangeBuilder()
                .withTasksSaved(mTasksLocalDataSource, TASKS)
                .withTasksSaved(mTasksRemoteDataSource, TASKS);

        // When a task is saved to the tasks repository
        mTasksRepository.saveTasks(TASKS)
                .subscribe(mTestSubscriber);

        // Then completable completes without error
        mTestSubscriber.assertComplete();
        mTestSubscriber.assertNoErrors();
    }

    @Test
    public void saveTask_savesTaskToRemoteDataSource() {
        // Given that a task is saved successfully in local and remote data sources
        new ArrangeBuilder()
                .withTaskSaved(mTasksLocalDataSource, ACTIVE_TASK)
                .withTaskSaved(mTasksRemoteDataSource, ACTIVE_TASK);

        // When a task is saved to the tasks repository
        mTasksRepository.saveTask(ACTIVE_TASK)
                .subscribe(mTestSubscriber);

        // Then completable completes without error
        mTestSubscriber.assertComplete();
        mTestSubscriber.assertNoErrors();
    }

    @Test
    public void completeTask_completesTask() {
        // Given that a task is completed successfully in local and remote data source
        new ArrangeBuilder()
                .withCompletedTask(mTasksLocalDataSource, ACTIVE_TASK)
                .withCompletedTask(mTasksRemoteDataSource, ACTIVE_TASK);

        // When a task is completed to the tasks repository
        mTasksRepository.completeTask(ACTIVE_TASK)
                .subscribe(mTestSubscriber);

        // The completable completes without error
        mTestSubscriber.assertComplete();
        mTestSubscriber.assertNoErrors();
    }

    @Test
    public void completeTask_whenLocalDataSourceCompletesWithError_doesNotComplete() {
        // Given that a task is not completed successfully in local data source
        Exception exception = new RuntimeException("test");
        new ArrangeBuilder()
                .withTaskCompletesWithError(mTasksLocalDataSource, ACTIVE_TASK, exception)
                .withCompletedTask(mTasksRemoteDataSource, ACTIVE_TASK);

        // When a task is completed to the tasks repository
        mTasksRepository.completeTask(ACTIVE_TASK)
                .subscribe(mTestSubscriber);

        // The completable completes with error
        mTestSubscriber.assertError(exception);
    }

    @Test
    public void completeTaskId_completesTask() {
        // Given that a task is completed successfully in local and remote data source
        new ArrangeBuilder()
                .withCompletedTaskId(mTasksLocalDataSource, ACTIVE_TASK.getId())
                .withCompletedTaskId(mTasksRemoteDataSource, ACTIVE_TASK.getId());

        // When a task is completed to the tasks repository
        mTasksRepository.completeTask(ACTIVE_TASK.getId())
                .subscribe(mTestSubscriber);

        // The completable completes without error
        mTestSubscriber.assertComplete();
        mTestSubscriber.assertNoErrors();
    }


    @Test
    public void activateTask_activatesTask() {
        // Given that a task is activated successfully in local and remote data source
        new ArrangeBuilder()
                .withActivatedTask(mTasksLocalDataSource, COMPLETED_TASK)
                .withActivatedTask(mTasksRemoteDataSource, COMPLETED_TASK);

        // When a completed task is activated to the tasks repository
        mTasksRepository.activateTask(COMPLETED_TASK)
                .subscribe(mTestSubscriber);

        // The completable completes without error
        mTestSubscriber.assertComplete();
        mTestSubscriber.assertNoErrors();
    }

    @Test
    public void activateTaskId_activatesTask() {
        // Given that a task is activated successfully in local and remote data source
        new ArrangeBuilder()
                .withActivatedTaskId(mTasksLocalDataSource, COMPLETED_TASK.getId())
                .withActivatedTaskId(mTasksRemoteDataSource, COMPLETED_TASK.getId());

        // When a completed task is activated with its id to the tasks repository
        mTasksRepository.activateTask(COMPLETED_TASK.getId())
                .subscribe(mTestSubscriber);

        // The completable completes without error
        mTestSubscriber.assertComplete();
        mTestSubscriber.assertNoErrors();
    }

    @Test
    public void getTask_requestsSingleTaskFromLocalDataSource() {
        // Given a stub completed task with title and description in the local repository
        new ArrangeBuilder()
                .withTaskAvailable(mTasksLocalDataSource, COMPLETED_TASK);

        // When a task is requested from the tasks repository
        mTasksRepository.getTask(COMPLETED_TASK.getId()).subscribe(mTestSubscriber);

        // Then the task is loaded from the database
        mTestSubscriber.assertValue(COMPLETED_TASK);
    }

    @Test
    public void getTask_whenDataNotLocal_fails() {
        // TODO: check the building of this test case again.
        // Given a stub completed task not available in the local repository
        new ArrangeBuilder()
                .withTaskNotAvailable(mTasksLocalDataSource, COMPLETED_TASK.getId());

        // When a task is requested from the tasks repository
        mTasksRepository.getTask(COMPLETED_TASK.getId())
                .subscribe(mTestSubscriber);

        // Verify null is returned
//        mTestSubscriber.assertValue(null);
        mTestSubscriber.assertNoValues();
    }

    @Test
    public void clearCompletedTasks_deletesTasksFromRemoteDataSource() {
        // Given that all completed tasks are cleared from the tasks repository
        new ArrangeBuilder()
                .withCompletedTaskCleared(mTasksRemoteDataSource)
                .withCompletedTaskCleared(mTasksLocalDataSource);

        // When all completed tasks are cleared from the tasks repository
        mTasksRepository.clearCompletedTasks().subscribe();

        // Verify that tasks are cleared from remote
        verify(mTasksRemoteDataSource).clearCompletedTasks();
    }

    @Test
    public void clearCompletedTasks_deletesTasksFromLocalDataSource() {
        // Given that all completed tasks are cleared from the tasks repository
        new ArrangeBuilder()
                .withCompletedTaskCleared(mTasksRemoteDataSource)
                .withCompletedTaskCleared(mTasksLocalDataSource);

        // When all completed tasks are cleared from the tasks repository
        mTasksRepository.clearCompletedTasks().subscribe();

        // Verify that tasks are cleared from local
        verify(mTasksLocalDataSource).clearCompletedTasks();
    }

    @Test
    public void deleteAllTasks_deletesTasksFromRemoteDataSource() {
        // Given that all tasks are deleted from the tasks repository
        new ArrangeBuilder()
                .withDeleteAllTasks(mTasksRemoteDataSource)
                .withDeleteAllTasks(mTasksLocalDataSource);

        // When all tasks are deleted from the tasks repository
        mTasksRepository.deleteAllTasks();

        // Verify that tasks deleted from remote
        verify(mTasksRemoteDataSource).deleteAllTasks();
    }

    @Test
    public void deleteAllTasks_deletesTasksFromLocalDataSource() {
        // Given that all tasks are deleted from the tasks repository
        new ArrangeBuilder()
                .withDeleteAllTasks(mTasksRemoteDataSource)
                .withDeleteAllTasks(mTasksLocalDataSource);

        // When all tasks are deleted to the tasks repository
        mTasksRepository.deleteAllTasks().subscribe();

        // Verify that tasks deleted from local
        verify(mTasksLocalDataSource).deleteAllTasks();
    }

    @Test
    public void deleteTask_deletesTaskFromRemoteDataSource() {
        // Given that a task is deleted from the tasks repository
        new ArrangeBuilder()
                .withDeleteTask(mTasksRemoteDataSource, COMPLETED_TASK.getId())
                .withDeleteTask(mTasksLocalDataSource, COMPLETED_TASK.getId());

        // When task deleted
        mTasksRepository.deleteTask(COMPLETED_TASK.getId()).subscribe();

        // Verify that the task was deleted from remote
        verify(mTasksRemoteDataSource).deleteTask(COMPLETED_TASK.getId());
    }

    @Test
    public void deleteTask_deletesTaskFromLocalDataSource() {
        // Given that a task is deleted from the tasks repository
        new ArrangeBuilder()
                .withDeleteTask(mTasksRemoteDataSource, COMPLETED_TASK.getId())
                .withDeleteTask(mTasksLocalDataSource, COMPLETED_TASK.getId());

        // When task deleted
        mTasksRepository.deleteTask(COMPLETED_TASK.getId()).subscribe();

        // Verify that the task was deleted from local
        verify(mTasksLocalDataSource).deleteTask(COMPLETED_TASK.getId());
    }

    @Test
    public void refreshTasks_completesWithData() {
        // Given that the remote data source has data available
        // And the data is then saved in the local data source
        new ArrangeBuilder()
                .withTasksAvailable(mTasksRemoteDataSource, TASKS)
                .withTasksSaved(mTasksLocalDataSource, TASKS);

        // When refreshing tasks
        mTasksRepository.refreshTasks()
                .subscribe(mTasksTestSubscriber);

        // The correct task are emitted
        mTasksTestSubscriber.assertComplete();
    }


    class ArrangeBuilder {

        ArrangeBuilder withTasksNotAvailable(TasksDataSource dataSource) {
            when(dataSource.getTasks()).thenReturn(Single.just(Collections.emptyList()));
            return this;
        }

        ArrangeBuilder withTasksAvailable(TasksDataSource dataSource, List<Task> tasks) {
            // don't allow the data sources to complete.
            when(dataSource.getTasks()).thenReturn(Single.just(tasks));
            return this;
        }

        ArrangeBuilder withTaskNotAvailable(TasksDataSource dataSource, int taskId) {
//            when(dataSource.getTask(eq(taskId))).thenReturn(Observable.<Task>just(null).concatWith(Observable.never()));
            when(dataSource.getTask(eq(taskId))).thenReturn(Observable.never());
            return this;
        }

        ArrangeBuilder withTaskAvailable(TasksDataSource dataSource, Task task) {
            when(dataSource.getTask(eq(task.getId()))).thenReturn(Observable.just(task).concatWith(Observable.never()));
            return this;
        }

        ArrangeBuilder withActivatedTask(TasksDataSource dataSource, Task task) {
            when(dataSource.activateTask(task)).thenReturn(Completable.complete());
            return this;
        }

        ArrangeBuilder withActivatedTaskId(TasksDataSource dataSource, int taskId) {
            when(dataSource.activateTask(taskId)).thenReturn(Completable.complete());
            return this;
        }

        ArrangeBuilder withCompletedTask(TasksDataSource dataSource, Task task) {
            when(dataSource.completeTask(task)).thenReturn(Completable.complete());
            return this;
        }

        ArrangeBuilder withCompletedTaskId(TasksDataSource dataSource, int taskId) {
            when(dataSource.completeTask(taskId)).thenReturn(Completable.complete());
            return this;
        }

        ArrangeBuilder withTaskCompletesWithError(TasksDataSource dataSource,
                                                  Task task,
                                                  Exception exception) {
            when(dataSource.completeTask(task)).thenReturn(Completable.error(exception));
            return this;
        }

        ArrangeBuilder withTaskSaved(TasksDataSource dataSource, Task task) {
            when(dataSource.saveTask(task)).thenReturn(Completable.complete());
            return this;
        }

        ArrangeBuilder withTasksSaved(TasksDataSource dataSource, List<Task> tasks) {
            when(dataSource.saveTasks(tasks)).thenReturn(Completable.complete());
            return this;
        }

        ArrangeBuilder withCompletedTaskCleared(TasksDataSource dataSource) {
            when(dataSource.clearCompletedTasks()).thenReturn(Completable.complete());
            return this;
        }

        ArrangeBuilder withDeleteTask(TasksDataSource dataSource, int taskId) {
            when(dataSource.deleteTask(taskId)).thenReturn(Completable.complete());
            return this;
        }

        ArrangeBuilder withDeleteAllTasks(TasksDataSource dataSource) {
            when(dataSource.deleteAllTasks()).thenReturn(Completable.complete());
            return this;
        }
    }
}
