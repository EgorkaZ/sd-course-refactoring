package ru.akirakozov.sd.refactoring.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.akirakozov.sd.refactoring.products.Product;
import ru.akirakozov.sd.refactoring.storage.ProductStorage;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author akirakozov
 */
public class QueryServlet extends ProductServlet {
    public QueryServlet(ProductStorage storage) {
        super(storage);
    }

    private enum Command {
        MIN, MAX, SUM, COUNT, UNKNOWN;

        public static Command getFromRequest(String command) {
            if (command == null) {
                return UNKNOWN;
            }
            switch (command) {
                case "min": return MIN;
                case "max": return MAX;
                case "sum": return SUM;
                case "count": return COUNT;
                default: return UNKNOWN;
            }
        }
    }

    private static final EnumMap<Command, String> cmdToHeader = new EnumMap<Command, String>(Map.of(
        Command.MAX, "<h1>Product with max price: </h1>",
        Command.MIN, "<h1>Product with min price: </h1>",
        Command.SUM, "Summary price: ",
        Command.COUNT, "Number of products: "
    ));

    private static void printResult(HttpServletResponse response, Product product) throws IOException {
        if (product != null) {
            response.getWriter().printf("%s\t%d</br>\n", product.getName(), product.getPrice());
        }
    }

    private static void printResult(HttpServletResponse response, int result) throws IOException {
        response.getWriter().println(result);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String commandStr = request.getParameter("command");
        Command cmd = Command.getFromRequest(commandStr);

        if (cmd == Command.UNKNOWN) {
            response.getWriter().printf("Unknown command: %s", commandStr);
            finishResponse(response);
            return;
        }

        response.getWriter().println("<html><body>");
        response.getWriter().println(cmdToHeader.get(cmd));

        switch (cmd) {
            case MIN: {
                printResult(response, storage.getMinPrice());
                break;
            }
            case MAX: {
                printResult(response, storage.getMaxPrice());
                break;
            }
            case SUM: {
                printResult(response, storage.getPriceSum());
                break;
            }
            case COUNT: {
                printResult(response, storage.getProductCount());
                break;
            }
            default: throw new IllegalArgumentException(cmd.name());
        }

        finishResponse(response);
    }

    private static void finishResponse(HttpServletResponse response) {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
