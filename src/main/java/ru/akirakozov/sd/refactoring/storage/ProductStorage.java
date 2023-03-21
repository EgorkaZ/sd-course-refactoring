package ru.akirakozov.sd.refactoring.storage;

import java.util.List;

import ru.akirakozov.sd.refactoring.products.Product;

public interface ProductStorage {
    Product getMinPrice();

    Product getMaxPrice();

    int getPriceSum();

    int getProductCount();

    List<Product> getAllProducts();

    void addProduct(Product product);

    void initializeStorage();
}
