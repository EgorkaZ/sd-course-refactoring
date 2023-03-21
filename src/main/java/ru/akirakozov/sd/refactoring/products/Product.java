package ru.akirakozov.sd.refactoring.products;

public class Product {
    private final String name;
    private final int price;

    public Product(String name, int price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("Product(name=%s, price=%d)", this.name, this.price);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Product) {
            Product asProduct = (Product) other;
            return name.equals(asProduct.name) && price == asProduct.price;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }
}
