package ru.akirakozov.sd.refactoring.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ru.akirakozov.sd.refactoring.aspect.Profile;
import ru.akirakozov.sd.refactoring.products.Product;

public class SqliteProductStorage implements ProductStorage {

    private final String connectionUrl;

    public SqliteProductStorage(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    @Override
    @Profile
    public int getPriceSum() {
        return getStatistic("SELECT SUM(price) FROM PRODUCT");
    }

    @Override
    @Profile
    public int getProductCount() {
        return getStatistic("SELECT COUNT(*) FROM PRODUCT");
    }

    private int getStatistic(String request) {
        SingleValueAggregator result = new SingleValueAggregator();
        makeDbQuery(request, result);
        return result.result;
    }

    @Override
    @Profile
    public Product getMinPrice() {
        String request = "SELECT * FROM PRODUCT ORDER BY PRICE LIMIT 1";
        return requestSingleProduct(request);
    }

    @Override
    @Profile
    public Product getMaxPrice() {
        String request = "SELECT * FROM PRODUCT ORDER BY PRICE DESC LIMIT 1";
        return requestSingleProduct(request);
    }

    @Override
    @Profile
    public List<Product> getAllProducts() {
        String request = "SELECT * FROM PRODUCT";
        return requestProductList(request);
    }

    private List<Product> requestProductList(String request) {
        ListAggregator result = new ListAggregator();
        makeDbQuery(request, result);
        return result.products;
    }

    private Product requestSingleProduct(String request) {
        List<Product> products = requestProductList(request);
        if (products.isEmpty()) {
            return null;
        }
        return products.get(0);
    }

    @Override
    @Profile
    public void addProduct(Product product) {
        String request = String.format("INSERT INTO PRODUCT (NAME, PRICE) VALUES (\"%s\", %d)",
                product.getName(), product.getPrice());
        makeDbUpdate(request);
    }

    @Override
    @Profile
    public void initializeStorage() {
        String request = "CREATE TABLE IF NOT EXISTS PRODUCT" +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " NAME           TEXT    NOT NULL, " +
                " PRICE          INT     NOT NULL)";
        makeDbUpdate(request);
    }

    private interface ResultAggregator {
        public void consumeResult(ResultSet rs) throws SQLException;
    }

    private class ListAggregator implements ResultAggregator {
        public final List<Product> products = new ArrayList<Product>();

        @Override
        public void consumeResult(ResultSet rs) throws SQLException {
            while (rs.next()) {
                String name = rs.getString("name");
                int price = rs.getInt("price");
                Product product = new Product(name, price);

                products.add(product);
            }
        }
    }

    private class SingleValueAggregator implements ResultAggregator {
        private int result = 0;

        @Override
        public void consumeResult(ResultSet rs) throws SQLException {
            if (rs.next()) {
                result = rs.getInt(1);
            }
        }
    }

    @Profile
    private void makeDbQuery(String command, ResultAggregator aggregator) {
        try (Connection c = DriverManager.getConnection(connectionUrl)) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(command);

            aggregator.consumeResult(rs);

            rs.close();
            stmt.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Profile
    private void makeDbUpdate(String command) {
        try (Connection c = DriverManager.getConnection(connectionUrl)) {
            Statement stmt = c.createStatement();
            stmt.executeUpdate(command);
            stmt.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
