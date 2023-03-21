package ru.akirakozov.sd.refactoring.servlet;

import javax.servlet.http.HttpServlet;

import ru.akirakozov.sd.refactoring.storage.ProductStorage;

public abstract class ProductServlet extends HttpServlet {
    protected final ProductStorage storage;

    public ProductServlet(ProductStorage storage) {
        this.storage = storage;
    }
}
