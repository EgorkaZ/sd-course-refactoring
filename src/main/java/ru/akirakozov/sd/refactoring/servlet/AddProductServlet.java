package ru.akirakozov.sd.refactoring.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.akirakozov.sd.refactoring.products.Product;
import ru.akirakozov.sd.refactoring.storage.ProductStorage;
import ru.akirakozov.sd.refactoring.view.HtmlResponseView;
import ru.akirakozov.sd.refactoring.view.ResponseView;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * @author akirakozov
 */
public class AddProductServlet extends ProductServlet {

    public AddProductServlet(ProductStorage storage) {
        super(storage);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        int price = Integer.parseInt(request.getParameter("price"));

        storage.addProduct(new Product(name, price));

        ResponseView responseView = new HtmlResponseView(response);
        responseView.beginResponse();
        responseView.print("OK");
        responseView.finishResponse();
    }
}
