package ru.akirakozov.sd.refactoring.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.akirakozov.sd.refactoring.products.Product;
import ru.akirakozov.sd.refactoring.storage.ProductStorage;
import ru.akirakozov.sd.refactoring.storage.SqliteProductStorage;
import ru.akirakozov.sd.refactoring.view.HtmlResponseView;
import ru.akirakozov.sd.refactoring.view.ResponseView;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * @author akirakozov
 */
public class GetProductsServlet extends ProductServlet {
    public GetProductsServlet(ProductStorage storage) {
        super(storage);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final List<Product> products = storage.getAllProducts();

        ResponseView responseView = new HtmlResponseView(response);
        responseView.beginResponse();

        for (Product product : products) {
            responseView.print(product);
        }

        responseView.finishResponse();
    }
}
