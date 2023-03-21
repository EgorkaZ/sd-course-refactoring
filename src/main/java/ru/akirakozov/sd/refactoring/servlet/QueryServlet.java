package ru.akirakozov.sd.refactoring.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.akirakozov.sd.refactoring.products.Product;
import ru.akirakozov.sd.refactoring.storage.ProductStorage;
import ru.akirakozov.sd.refactoring.view.HtmlResponseView;
import ru.akirakozov.sd.refactoring.view.ResponseView;

import java.io.IOException;

/**
 * @author akirakozov
 */
public class QueryServlet extends ProductServlet {
    public QueryServlet(ProductStorage storage) {
        super(storage);
    }

    private enum Command {
        MIN {
            @Override
            public void runCommand(ResponseView responseView, ProductStorage storage) throws IOException {
                responseView.printHeader("Product with min price:");
                printResult(responseView, storage.getMinPrice());
            }
        },
        MAX {
            @Override
            public void runCommand(ResponseView responseView, ProductStorage storage) throws IOException {
                responseView.printHeader("Product with max price:");
                printResult(responseView, storage.getMaxPrice());
            }
        },
        SUM {
            @Override
            public void runCommand(ResponseView responseView, ProductStorage storage) throws IOException {
                responseView.print("Summary price: ");
                printResult(responseView, storage.getPriceSum());
            }
        },
        COUNT {
            @Override
            public void runCommand(ResponseView responseView, ProductStorage storage) throws IOException {
                responseView.print("Number of products: ");
                printResult(responseView, storage.getProductCount());
            }
        },
        UNKNOWN {
            @Override
            public void runCommand(ResponseView responseView, ProductStorage storage) {
                throw new IllegalArgumentException(name());
            }
        };

        public abstract void runCommand(ResponseView responseView, ProductStorage storage) throws IOException;
    }

    private static void printResult(ResponseView responseView, Product product) throws IOException {
        if (product != null) {
            responseView.print(product);
        }
    }

    private static void printResult(ResponseView responseView, int result) throws IOException {
        responseView.print(result);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String commandStr = request.getParameter("command");

        final ResponseView responseView = new HtmlResponseView(response);
        responseView.beginResponse();

        Command cmd;
        try {
            cmd = Command.valueOf(commandStr.toUpperCase());
        } catch (Exception e) {
            responseView.print(String.format("Unknown command: %s", commandStr));
            responseView.finishResponse();
            return;
        }

        cmd.runCommand(responseView, storage);
        responseView.finishResponse();
    }

}
