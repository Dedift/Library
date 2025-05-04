package main.dao;

import main.domain.BaseEntity;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface BaseDAO<T extends BaseEntity<PK>, PK extends Serializable> {

    Optional<T> findById(PK id);
    List<T> findAll();
    T save(T entity);
    void update(T entity);
    void deleteById(PK id);
}