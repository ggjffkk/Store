import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

// представлення товару
class Product {
    private String name;
    private double price;
    private Semaphore semaphore;

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.semaphore = new Semaphore(quantity);
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public boolean purchase() {
        return semaphore.tryAcquire();
    }

    public void restock(int quantity) {
        semaphore.release(quantity);
    }

    public int getAvailableQuantity() {
        return semaphore.availablePermits();
    }
}

// представлення замовлення
class Order {
    private String customerName;
    private String address;
    private Product product;
    private boolean isReserved; // поле для позначення, чи замовлення заброньоване

    public Order(String customerName, String address, Product product, boolean isReserved) {
        this.customerName = customerName;
        this.address = address;
        this.product = product;
        this.isReserved = isReserved; 
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getAddress() {
        return address;
    }

    public Product getProduct() {
        return product;
    }

    public boolean isReserved() { // метод для перевірки, чи замовлення заброньоване
        return isReserved;
    }
}

// представлення адміністратора
class Admin {
    private OnlineStore store;

    public Admin(OnlineStore store) {
        this.store = store;
    }

    public void addProduct() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введіть назву товару:");
        String name = scanner.nextLine();
        System.out.println("Введіть ціну товару:");
        double price = scanner.nextDouble();
        System.out.println("Введіть кількість товарів:");
        int quantity = scanner.nextInt();
        store.addProduct(new Product(name, price, quantity));
        System.out.println("Товар додано.");
    }

    public void viewOrders() {
        System.out.println("Перегляд замовлень:");
        if (store.getOrders().isEmpty()) {
            System.out.println("Немає оформлених замовлень.");
        } else {
            for (Order order : store.getOrders()) {
                String reservationStatus = order.isReserved() ? " (Бронь)" : ""; // перевірка статусу броні
                System.out.println("Користувач: " + order.getCustomerName() +
                        ", Товар: " + order.getProduct().getName() +
                        ", Адреса: " + order.getAddress() + reservationStatus);
            }
        }
    }
}

// представлення покупця
class Customer implements Runnable {
    private OnlineStore store;
    private String name;
    private String address;

    public Customer(OnlineStore store, String name, String address) {
        this.store = store;
        this.name = name;
        this.address = address;
    }

    @Override
    public void run() {
        if (!store.isOpen()) {
            System.out.println("Магазин зачинений. Спробуйте пізніше.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Доступні товари:");
        for (Product product : store.getProducts()) {
            System.out.println(product.getName() + " - Ціна: " + product.getPrice() + ", Доступно: " + product.getAvailableQuantity());
        }

        System.out.println("Введіть назву товару для замовлення:");
        String productName = scanner.nextLine();
        Product selectedProduct = null;

        // знаходження товару за назвою
        for (Product product : store.getProducts()) {
            if (product.getName().equalsIgnoreCase(productName)) {
                selectedProduct = product;
                break;
            }
        }

        if (selectedProduct != null) {
            if (selectedProduct.getAvailableQuantity() > 0) {
                if (selectedProduct.purchase()) {
                    System.out.println("Замовлення на товар " + selectedProduct .getName() + " виконано.");
                    store.addOrder(new Order(name, address, selectedProduct, false));
                }
            } else {
                System.out.println("Товар " + selectedProduct.getName() + " відсутній, але ми забронювали наступну партію для вас.");
                store.addOrder(new Order(name, address, selectedProduct, true)); // Бронь товару для користувача
            }
        } else {
            System.out.println("Товар не знайдений. Перевірте назву.");
        }
    }
}

// представлення інтернет-магазину
class OnlineStore {
    private List<Product> products;
    private List<Order> orders;
    private boolean isOpen;

    public OnlineStore() {
        this.products = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.isOpen = true; // відкритий за замовчуванням
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void closeStore() {
        isOpen = false;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void addOrder(Order order) {
        orders.add(order);
    }

    public List<Order> getOrders() {
        return orders;
    }
}

public class store {
    public static void main(String[] args) {
        OnlineStore store = new OnlineStore();

        // доступні товари
        store.addProduct(new Product("Комп'ютер 1", 15000, 3));
        store.addProduct(new Product("Комп'ютер 2", 12000, 3));
        store.addProduct(new Product("Комп'ютер 3", 18000, 3));
        store.addProduct(new Product("Телефон 1", 5000, 2));
        store.addProduct(new Product("Телефон 2", 6000, 2));

        Admin admin = new Admin(store);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Виберіть дію:");
            System.out.println("1. Додати товар (адміністратор)");
            System.out.println("2. Оформити замовлення (користувач)");
            System.out.println("3. Переглянути замовлення (адміністратор)");
            System.out.println("4. Закрити магазин");
            System.out.println("5. Вихід");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    admin.addProduct();
                    break;
                case 2:
                    System.out.println("Введіть ім'я:");
                    String customerName = scanner.next();
                    System.out.println("Введіть адресу:");
                    String customerAddress = scanner.next();
                    Customer customer = new Customer(store, customerName, customerAddress);
                    customer.run();
                    break;
                case 3:
                    admin.viewOrders();
                    break;
                case 4:
                    store.closeStore();
                    System.out.println("Магазин зачинений.");
                    break;
                case 5:
                    System.out.println("Вихід з програми.");
                    return;
                default:
                    System.out.println("Неправильний вибір.");
            }
        }
    }
}
