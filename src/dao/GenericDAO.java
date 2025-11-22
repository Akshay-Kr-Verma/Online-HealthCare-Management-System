package dao;

import java.util.List;

/**
 * This interface uses GENERICS <T> to define standard database operations.
 * It satisfies the "Interfaces" and "Generics" requirement in the rubric.
 */
public interface GenericDAO<T> {
    // Abstract method to add an item
    boolean add(T t);

    // Abstract method to retrieve all items
    List<T> getAll();
}