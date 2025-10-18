package lorek;

import java.sql.*;
import java.util.Scanner;

public class MainApp {
    private Connection connection;

    public MainApp(Connection connection) {
        this.connection = connection;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Система управления запасами ===");
            System.out.println("1 - Добавить товар");
            System.out.println("2 - Редактировать товар");
            System.out.println("3 - Продать товар");
            System.out.println("4 - Показать все товары");
            System.out.println("5 - Показать ожидающие заказы");
            System.out.println("6 - Проверить уровни запасов и создать заказы");
            System.out.println("7 - Выход");
            System.out.print("Выберите опцию: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    this.addProduct(scanner);
                    break;
                case 2:
                    this.editProduct(scanner);
                    break;
                case 3:
                    this.sellProduct(scanner);
                    break;
                case 4:
                    this.showAllProducts();
                    break;
                case 5:
                    this.showPendingOrders();
                    break;
                case 6:
                    this.checkAllStockLevels();
                    break;
                case 7:
                    System.out.println("Выход из программы.");
                    return;
                default:
                    System.out.println("Неверный выбор, попробуйте снова.");
            }
        }
    }

    private void addProduct(Scanner scanner) {
        System.out.print("Введите название товара: ");
        String name = scanner.nextLine();
        System.out.print("Введите категорию (Газета, Журнал, Книга): ");
        String category = scanner.nextLine();
        System.out.print("Введите количество товара: ");
        int quantity = scanner.nextInt();
        System.out.print("Введите цену товара: ");
        double price = scanner.nextDouble();
        System.out.print("Введите минимальный уровень запаса: ");
        int minimalLevel = scanner.nextInt();
        scanner.nextLine();

        executeInsertProduct(name, category, quantity, price, minimalLevel);
    }

    private void editProduct(Scanner scanner) {
        System.out.print("Введите ID товара для редактирования: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Введите новое название товара: ");
        String name = scanner.nextLine();
        System.out.print("Введите новое количество товара: ");
        int quantity = scanner.nextInt();
        System.out.print("Введите новую цену товара: ");
        double price = scanner.nextDouble();
        System.out.print("Введите новый минимальный уровень запаса: ");
        int minimalLevel = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Введите новую категорию (Газета, Журнал, Книга): ");
        String category = scanner.nextLine();

        executeUpdateProduct(id, name, quantity, price, minimalLevel, category);
    }

    private void sellProduct(Scanner scanner) {
        System.out.print("Введите ID товара для продажи: ");
        int id = scanner.nextInt();
        System.out.print("Введите количество для продажи: ");
        int quantity = scanner.nextInt();

        executeSellProduct(id, quantity);
    }

    private void showAllProducts() {
        executeShowAllProducts();
    }

    private void showPendingOrders() {
        executeShowPendingOrders();
    }

    private void checkAllStockLevels() {
        executeCheckAllStockLevels();
    }

    // === РЕАЛИЗАЦИЯ АВТОМАТИЗАЦИИ ЗАКАЗОВ ===

    private void checkAndCreateOrder(int productId) {
        String selectQuery = "SELECT id, name, quantity, minimal_level FROM products WHERE id = ?";

        try (PreparedStatement selectStmt = this.connection.prepareStatement(selectQuery)) {
            selectStmt.setInt(1, productId);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                int currentQuantity = rs.getInt("quantity");
                int minimalLevel = rs.getInt("minimal_level");
                String productName = rs.getString("name");

                 //Если количество ниже минимального уровня, создаем заказ
                if (currentQuantity < minimalLevel) {
                    int quantityToOrder = minimalLevel * 2;
                    createOrder(productId, productName, quantityToOrder);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при проверке уровня запасов: " + e.getMessage());
        }
    }

    private void createOrder(int productId, String productName, int quantityToOrder) {
        // Сначала проверяем, нет ли уже активного заказа для этого товара
        String checkQuery = "SELECT id FROM orders WHERE product_id = ? AND status = 'PENDING'";
        String insertQuery = "INSERT INTO orders (product_id, product_name, quantity_ordered, status) VALUES (?, ?, ?, 'PENDING')";

        try (
                PreparedStatement checkStmt = this.connection.prepareStatement(checkQuery);
                PreparedStatement insertStmt = this.connection.prepareStatement(insertQuery);
        ) {
            checkStmt.setInt(1, productId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) { // Если активного заказа нет, создаем новый
                insertStmt.setInt(1, productId);
                insertStmt.setString(2, productName);
                insertStmt.setInt(3, quantityToOrder);
                insertStmt.executeUpdate();
                System.out.println("Создан заказ на товар: " + productName + ", количество: " + quantityToOrder);
            } else {
                System.out.println("Для товара " + productName + " уже есть активный заказ");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при создании заказа: " + e.getMessage());
        }
    }

    private void executeCheckAllStockLevels() {
        String query = "SELECT id, name, quantity, minimal_level FROM products";

        try (
                Statement statement = this.connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
        ) {
            System.out.println("\n=== Проверка уровней запасов ===");
            boolean ordersCreated = false;

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int quantity = resultSet.getInt("quantity");
                int minimalLevel = resultSet.getInt("minimal_level");

                System.out.println("Товар: " + name + ", Текущий запас: " + quantity + ", Минимальный уровень: " + minimalLevel);

                if (quantity < minimalLevel) {
                    checkAndCreateOrder(id);
                    ordersCreated = true;
                }
            }

            if (!ordersCreated) {
                System.out.println("Все запасы в норме, заказы не требуются.");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при проверке уровней запасов: " + e.getMessage());
        }
    }

    // === БАЗОВЫЕ МЕТОДЫ ===

    private void executeInsertProduct(String name, String category, int quantity, double price, int minimalLevel) {
        String query = "INSERT INTO products (name, quantity, price, category, minimal_level) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setInt(2, quantity);
            statement.setDouble(3, price);
            statement.setString(4, category);
            statement.setInt(5, minimalLevel);
            statement.executeUpdate();
            System.out.println("Товар успешно добавлен.");
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении товара: " + e.getMessage());
        }
    }

    private void executeUpdateProduct(int id, String name, int quantity, double price, int minimalLevel, String category) {
        String query = "UPDATE products SET name = ?, quantity = ?, price = ?, category = ?, minimal_level = ? WHERE id = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setInt(2, quantity);
            statement.setDouble(3, price);
            statement.setString(4, category);
            statement.setInt(5, minimalLevel);
            statement.setInt(6, id);
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Товар успешно отредактирован.");
                // Проверяем уровень запасов после редактирования
                checkAndCreateOrder(id);
            } else {
                System.out.println("Товар с таким ID не найден.");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при редактировании товара: " + e.getMessage());
        }
    }

    private void executeSellProduct(int id, int quantity) {
        String selectQuery = "SELECT quantity FROM products WHERE id = ?";
        String updateQuery = "UPDATE products SET quantity = ? WHERE id = ?";

        try (
                PreparedStatement selectStmt = this.connection.prepareStatement(selectQuery);
                PreparedStatement updateStmt = this.connection.prepareStatement(updateQuery);
        ) {
            selectStmt.setInt(1, id);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                int currentQuantity = rs.getInt("quantity");
                if (quantity > currentQuantity) {
                    System.err.println("Ошибка: Недостаточно товара. Доступно: " + currentQuantity);
                } else {
                    int newQuantity = currentQuantity - quantity;
                    updateStmt.setInt(1, newQuantity);
                    updateStmt.setInt(2, id);
                    updateStmt.executeUpdate();
                    System.out.println("Товар успешно продан. Оставшееся количество: " + newQuantity);

                    // Проверка уровня запасов после продажи
                    checkAndCreateOrder(id);

                    if (newQuantity == 0) {
                        deleteProduct(id);
                    }
                }
            } else {
                System.err.println("Ошибка: Товар с ID " + id + " не найден.");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при продаже товара: " + e.getMessage());
        }
    }

    private void deleteProduct(int id) {
        String query = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
            System.out.println("Товар с ID " + id + " был удалён, так как его количество стало равно нулю.");
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении товара: " + e.getMessage());
        }
    }

    private void executeShowAllProducts() {
        String query = "SELECT * FROM products";
        try (
                Statement statement = this.connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
        ) {
            System.out.println("\n=== Товары в базе данных ===");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int quantity = resultSet.getInt("quantity");
                double price = resultSet.getDouble("price");
                String category = resultSet.getString("category");
                int minimalLevel = resultSet.getInt("minimal_level");
                System.out.println("ID: " + id + ", Название: " + name + ", Категория: " + category +
                        ", Количество: " + quantity + ", Цена: " + price + ", Мин. уровень: " + minimalLevel);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при выводе товаров: " + e.getMessage());
        }
    }

    private void executeShowPendingOrders() {
        String query = "SELECT * FROM orders WHERE status = 'PENDING' ORDER BY order_date DESC";
        try (
                Statement statement = this.connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
        ) {
            System.out.println("\n=== Ожидающие заказы ===");
            boolean hasOrders = false;
            while (resultSet.next()) {
                hasOrders = true;
                int id = resultSet.getInt("id");
                String productName = resultSet.getString("product_name");
                int quantity = resultSet.getInt("quantity_ordered");
                Date orderDate = resultSet.getDate("order_date");
                System.out.println("ID заказа: " + id + ", Товар: " + productName +
                        ", Количество: " + quantity + ", Дата: " + orderDate);
            }
            if (!hasOrders) {
                System.out.println("Нет ожидающих заказов.");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при выводе заказов: " + e.getMessage());
        }
    }
}
