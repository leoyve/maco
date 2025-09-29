package com.example.maco.domain.port.user;

import com.example.maco.domain.model.health.WeightResult;
import com.example.maco.domain.model.todo.TodoResult;

public interface WeightRepository {
    void save(String userToken, WeightResult result); // saveOrUpdate
}
