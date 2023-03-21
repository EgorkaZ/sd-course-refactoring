package ru.akirakozov.sd.refactoring.view;

import java.io.IOException;

import ru.akirakozov.sd.refactoring.products.Product;

public interface ResponseView {
    public void print(Product product) throws IOException;

    public void print(int number) throws IOException;

    public void print(String line) throws IOException;

    public void printHeader(String header) throws IOException;

    public void beginResponse() throws IOException;
    public void finishResponse() throws IOException;
}
