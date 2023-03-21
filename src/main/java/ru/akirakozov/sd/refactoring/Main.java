package ru.akirakozov.sd.refactoring;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.akirakozov.sd.refactoring.servlet.AddProductServlet;
import ru.akirakozov.sd.refactoring.servlet.GetProductsServlet;
import ru.akirakozov.sd.refactoring.servlet.QueryServlet;
import ru.akirakozov.sd.refactoring.storage.ProductStorage;
import ru.akirakozov.sd.refactoring.storage.SqliteProductStorage;

/**
 * @author akirakozov
 */
public class Main {
    public static void main(String[] args) throws Exception {
        String connectionUrl = "jdbc:sqlite:test.db";
        if (args.length > 0) {
            connectionUrl = String.format("jdbc:sqlite:%s", args[0]);
            System.out.println("Making DB in " + connectionUrl);
        }
        ProductStorage storage = new SqliteProductStorage(connectionUrl);
        storage.initializeStorage();

        Server server = new Server(8081);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new AddProductServlet(storage)), "/add-product");
        context.addServlet(new ServletHolder(new GetProductsServlet(storage)),"/get-products");
        context.addServlet(new ServletHolder(new QueryServlet(storage)),"/query");

        server.start();
        server.join();
    }
}
