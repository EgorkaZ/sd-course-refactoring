package ru.akirakozov.sd.refactoring.view;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import ru.akirakozov.sd.refactoring.aspect.Profile;
import ru.akirakozov.sd.refactoring.products.Product;

public class HtmlResponseView implements ResponseView {
    private final HttpServletResponse response;

    public HtmlResponseView(HttpServletResponse response) {
        this.response = response;
    }

    @Override
    @Profile
    public void print(Product product) throws IOException {
        response.getWriter().printf("%s\t%d</br>\n", product.getName(), product.getPrice());
    }

    @Override
    @Profile
    public void print(int number) throws IOException {
        response.getWriter().println(number);
    }

    @Override
    public void print(String line) throws IOException {
        response.getWriter().println(line);
    }

    @Override
    public void printHeader(String header) throws IOException {
        response.getWriter().printf("<h1>%s</h1>", header);
    }

    @Override
    public void beginResponse() throws IOException {
        response.getWriter().println("<html><body>");
    }

    @Override
    public void finishResponse() throws IOException {
        response.getWriter().println("</body></html>");
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html");
    }
}
