package ru.akirakozov.sd.refactoring;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.akirakozov.sd.refactoring.Main;
import ru.akirakozov.sd.refactoring.products.Product;

public class ApplicationTest {
    // public class Product implements Comparable<Product> {
    // public Product(String name, int price) {
    // this.name = name;
    // this.price = price;
    // }

    // @Override
    // public String toString() {
    // return String.format("Product(name=%s, price=%d)", this.name, this.price);
    // }

    // @Override
    // public boolean equals(Object other) {
    // if (other instanceof Product) {
    // Product asProduct = (Product) other;
    // return name.equals(asProduct.name) && price == asProduct.price;
    // }
    // return false;
    // }

    // @Override
    // public int compareTo(Product other) {
    // return Comparator.comparingInt((Product prod) -> prod.price)
    // .thenComparing(prod -> prod.name)
    // .compare(this, other);
    // }

    // }

    private static final ExecutorService mainExecutor = Executors.newSingleThreadExecutor();
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeClass
    public static void setUpAll() throws InterruptedException {
        mainExecutor.submit(() -> {
            try {
                Main.main(new String[] {});
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        });
        Thread.sleep(1000);
    }

    @AfterClass
    public static void tearDownAll() {
        mainExecutor.shutdown();
    }

    @After
    public void tearDown() {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:test.db")) {
            Statement stmt = connection.createStatement();

            System.out.println("Deleting elements");
            stmt.executeUpdate("DELETE FROM PRODUCT");
            stmt.close();
        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

    private List<String> makeRequest(String method) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(String.format("http://localhost:8081%s", method)))
                .build();
        String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        Element parsedHtml = Jsoup.parse(response).body();

        return parsedHtml.textNodes()
                .stream()
                .map(node -> node.text().trim())
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toList());
    }

    private void addProduct(Product product) throws IOException, InterruptedException {
        makeRequest(String.format("/add-product?name=%s&price=%d", product.getName(), product.getPrice()));
    }

    private int parsePrice(String priceStr) {
        if (priceStr.chars().allMatch(ch -> Character.isDigit(ch))) {
            return Integer.parseUnsignedInt(priceStr);
        }
        return -1;
    }

    private Product parseProduct(String asString) {
        int splitIdx = asString.indexOf(' ');

        String name = asString.substring(0, splitIdx);
        int price = parsePrice(asString.substring(splitIdx + 1));
        return new Product(name, price);
    }

    private static Comparator<Product> getProductComparator() {
        return Comparator.comparingInt(Product::getPrice).thenComparing(Product::getName);
    }

    private List<Product> getProducts() throws IOException, InterruptedException {
        return makeRequest("/get-products")
                .stream()
                .map(line -> parseProduct(line))
                .collect(Collectors.toList());
    }

    private int makeNumberQuery(String query) throws IOException, InterruptedException {
        String response = makeRequest(String.format("/query?command=%s", query)).get(0);
        for (int i = response.length() - 1; i >= 0; --i) {
            if (!Character.isDigit(response.charAt(i))) {
                return parsePrice(response.substring(i + 1));
            }
        }
        return -1;
    }

    private Optional<Product> makeProductQuery(String query) throws IOException, InterruptedException {
        List<String> response = makeRequest(String.format("/query?command=%s", query));
        if (response.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(parseProduct(response.get(0)));
    }

    // private void runTestWith(List<Product> products) throws IOException, InterruptedException {
    //     TreeSet<Product> asSet = new TreeSet<Product>(getProductComparator());
    //     for (Product product : products) {
    //         asSet.add(product);
    //     }
    //     runTestWith(asSet);
    // }

    private void runTestWith(List<Product> products) throws IOException, InterruptedException {
        int sum = 0;
        for (Product product : products) {
            addProduct(product);
            sum += product.getPrice();
        }

        Assert.assertEquals(products, getProducts());
        Assert.assertEquals(products.size(), makeNumberQuery("count"));
        Assert.assertEquals(sum, makeNumberQuery("sum"));

        Optional<Product> minPrice = makeProductQuery("min");
        Optional<Product> maxPrice = makeProductQuery("max");
        if (products.isEmpty()) {
            Assert.assertTrue("found minimum in empty storage", minPrice.isEmpty());
            Assert.assertTrue("found minimum in empty storage", maxPrice.isEmpty());
        } else {
            Assert.assertEquals(Collections.min(products, getProductComparator()), minPrice.get());
            Assert.assertEquals(Collections.max(products, getProductComparator()), maxPrice.get());
        }

        Assert.assertEquals(makeRequest("/query").get(0), "Unknown command: null");
        Assert.assertEquals(makeRequest("/query?command=kek").get(0), "Unknown command: kek");
    }

    @Test
    public void none() throws IOException, InterruptedException {
    }

    @Test
    public void getEmptyList() throws IOException, InterruptedException {
        runTestWith(List.of());
    }

    @Test
    public void addCheckSingle() throws IOException, InterruptedException {
        runTestWith(List.of(new Product("ab", 10)));
    }

    @Test
    public void addCheckMultiple() throws IOException, InterruptedException {
        runTestWith(List.of(
                new Product("ab", 12),
                new Product("cd", 23),
                new Product("ef", 34),
                new Product("gh", 45)));
    }

    @Test
    public void addCheckSingleDuplicated() throws IOException, InterruptedException {
        runTestWith(List.of(
                new Product("ab", 12),
                new Product("ab", 12),
                new Product("ab", 12)));
    }

    @Test
    public void addCheckMultipleDuplicated() throws IOException, InterruptedException {
        runTestWith(List.of(
                new Product("ab", 12),
                new Product("ab", 12),
                new Product("cd", 23),
                new Product("ef", 34),
                new Product("ef", 35),
                new Product("ef", 36),
                new Product("gh", 45),
                new Product("gh", 46)));
    }
}