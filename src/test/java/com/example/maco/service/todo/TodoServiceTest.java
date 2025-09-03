package com.example.maco.service.todo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.example.maco.domain.model.todo.TodoResult;
import com.example.maco.domain.model.todo.TodoResult.TodoEntities;
import com.example.maco.domain.port.user.TodoRepository;

public class TodoServiceTest {

    private TodoRepository repo;
    private TodoService service;

    @BeforeEach
    void setup() {
        repo = Mockito.mock(TodoRepository.class);
        service = new TodoService(repo);
    }

    @Test
    void insertTodo_nullModel_throws() {
        assertThrows(RuntimeException.class, () -> service.insertTodo("null", null));
    }

    @Test
    void insertTodo_missingEntities_throws() {
        TodoResult r = new TodoResult();
        assertThrows(RuntimeException.class, () -> service.insertTodo("null", r));
    }

    @Test
    void insertTodo_addTodoWithoutTask_throws() {
        TodoResult r = new TodoResult();
        r.setIntent("addTodo");
        r.setEntities(new TodoEntities());
        assertThrows(RuntimeException.class, () -> service.insertTodo("null", r));
    }

    @Test
    void insertTodo_statusNotTodo_throws() {
        TodoResult r = new TodoResult();
        TodoEntities e = new TodoEntities();
        e.setTask("t");
        e.setStatus("DONE");
        r.setIntent("addTodo");
        r.setEntities(e);
        assertThrows(RuntimeException.class, () -> service.insertTodo("null", r));
    }

    @Test
    void insertTodo_valid_callsRepo() {
        TodoResult r = new TodoResult();
        TodoEntities e = new TodoEntities();
        e.setTask("t");
        e.setStatus("TODO");
        r.setIntent("addTodo");
        r.setEntities(e);

        service.insertTodo("null", r);
        Mockito.verify(repo).save("null", r);
    }
}
