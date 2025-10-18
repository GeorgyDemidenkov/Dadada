package lorek;

public class Product {
    private int id;
    private String name;
    private int quantity;
    private double price;
    private int minimalLevel;
    private String category;

    public Product(int id, String name, int quantity, double price, int minimalLevel, String category) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.minimalLevel = minimalLevel;
        this.category = category;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getMinimalLevel() {
        return minimalLevel;
    }

    public void setMinimalLevel(int minimalLevel) {
        this.minimalLevel = minimalLevel;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Product ID: " + id + ", Name: " + name + ", Category: " + category +
                ", Quantity: " + quantity + ", Price: " + price + ", Minimal Level: " + minimalLevel;
    }
}
