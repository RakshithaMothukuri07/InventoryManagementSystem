package com.inventory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InventoryManagement {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        Connection con = DBConnection.getConnection();

        if (con == null) {
            System.out.println("Database connection failed.");
            sc.close();
            return;
        }

        int choice;

        do {

            System.out.println("\n===== INVENTORY MANAGEMENT SYSTEM =====");
            System.out.println("1. Add Product");
            System.out.println("2. Update Stock");
            System.out.println("3. Record Sales");
            System.out.println("4. Generate Reports");
            System.out.println("5. Exit");
            System.out.print("\nEnter your choice: ");

            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("\nPlease enter a valid number.");
                choice = 0;
                continue;
            }

            switch (choice) {

                case 1:
                    addProduct(con, sc);
                    break;

                case 2:
                    updateStock(con, sc);
                    break;

                case 3:
                    recordSales(con, sc);
                    break;

                case 4:
                    generateReports(con);
                    break;

                case 5:
                    System.out.println(
                            "\nThank you for using the Inventory Management System."
                    );
                    break;

                default:
                    System.out.println(
                            "\nInvalid choice. Please try again."
                    );
            }

        } while (choice != 5);

        try {
            con.close();
        } catch (SQLException e) {
            System.out.println("Error while closing database connection.");
        }

        sc.close();
    }

    public static void addProduct(Connection con, Scanner sc) {

        System.out.println("\n========== ADD PRODUCT ==========");

        try {

            System.out.println("Available suppliers:");

            String supplierSql =
                    "SELECT supplier_id, supplier_name FROM suppliers";

            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(supplierSql)) {

                while (rs.next()) {

                    System.out.println(
                            rs.getInt("supplier_id")
                            + " - "
                            + rs.getString("supplier_name")
                    );
                }
            }

            System.out.print("\nEnter product name: ");
            String name = sc.nextLine().trim();

            if (name.isEmpty()) {
                System.out.println("\nProduct name cannot be empty.");
                return;
            }

            System.out.print("Enter category: ");
            String category = sc.nextLine().trim();

            System.out.print("Enter price: ");

            BigDecimal price;

            try {
                price = new BigDecimal(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("\nPlease enter a valid price.");
                return;
            }

            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("\nPrice must be greater than 0.");
                return;
            }

            System.out.print("Enter initial stock quantity: ");

            int stock;

            try {
                stock = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("\nPlease enter a valid quantity.");
                return;
            }

            if (stock < 0) {
                System.out.println(
                        "\nStock quantity cannot be negative."
                );
                return;
            }

            System.out.print("Enter supplier_id: ");

            int supplierId;

            try {
                supplierId = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println(
                        "\nPlease enter a valid supplier_id."
                );
                return;
            }

            String checkSql =
                    "SELECT supplier_name FROM suppliers WHERE supplier_id = ?";

            try (PreparedStatement psCheck =
                         con.prepareStatement(checkSql)) {

                psCheck.setInt(1, supplierId);

                try (ResultSet rs = psCheck.executeQuery()) {

                    if (!rs.next()) {

                        System.out.println(
                                "\nNo supplier found with id "
                                + supplierId
                                + ". Product not added."
                        );

                        return;
                    }
                }
            }

            String sql =
                    "INSERT INTO products "
                    + "(product_name, category, price, stock_quantity, supplier_id) "
                    + "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement ps =
                         con.prepareStatement(
                                 sql,
                                 Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, name);
                ps.setString(2, category);
                ps.setBigDecimal(3, price);
                ps.setInt(4, stock);
                ps.setInt(5, supplierId);

                int rows = ps.executeUpdate();

                if (rows > 0) {

                    int productId = 0;

                    try (ResultSet keys = ps.getGeneratedKeys()) {

                        if (keys.next()) {
                            productId = keys.getInt(1);
                        }
                    }

                    System.out.println(
                            "\n============================================"
                    );

                    System.out.println(
                            "       PRODUCT ADDED SUCCESSFULLY"
                    );

                    System.out.println(
                            "============================================"
                    );

                    System.out.println("Product ID : " + productId);
                    System.out.println("Name       : " + name);
                    System.out.println("Category   : " + category);
                    System.out.println("Price      : Rs." + price);
                    System.out.println("Stock      : " + stock);

                    System.out.println(
                            "============================================"
                    );
                }
            }

        } catch (SQLException e) {

            System.out.println(
                    "\nError while adding product."
            );

            e.printStackTrace();
        }
    }

    public static void updateStock(Connection con, Scanner sc) {

        System.out.println("\n========== UPDATE STOCK ==========");

        try {

            System.out.print("Enter product_id: ");

            int productId;

            try {
                productId = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println(
                        "\nPlease enter a valid product_id."
                );
                return;
            }

            String findSql =
                    "SELECT product_name, stock_quantity "
                    + "FROM products WHERE product_id = ?";

            String productName;
            int currentStock;

            try (PreparedStatement ps =
                         con.prepareStatement(findSql)) {

                ps.setInt(1, productId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {

                        System.out.println(
                                "\nNo product found with id "
                                + productId
                        );

                        return;
                    }

                    productName = rs.getString("product_name");
                    currentStock = rs.getInt("stock_quantity");
                }
            }

            System.out.println(
                    "Product: "
                    + productName
                    + " | Current stock: "
                    + currentStock
            );

            System.out.println(
                    "1. Add stock"
            );

            System.out.println(
                    "2. Reduce stock"
            );

            System.out.print("Choose an option: ");

            int option;

            try {
                option = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println(
                        "\nPlease enter 1 or 2."
                );
                return;
            }

            if (option != 1 && option != 2) {

                System.out.println(
                        "\nInvalid option."
                );

                return;
            }

            System.out.print("Enter quantity: ");

            int quantity;

            try {
                quantity = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println(
                        "\nPlease enter a valid quantity."
                );
                return;
            }

            if (quantity <= 0) {

                System.out.println(
                        "\nQuantity must be greater than 0."
                );

                return;
            }

            if (option == 2 && quantity > currentStock) {

                System.out.println(
                        "\nCannot reduce by "
                        + quantity
                        + " - only "
                        + currentStock
                        + " in stock."
                );

                return;
            }

            int newStock;

            if (option == 1) {
                newStock = currentStock + quantity;
            } else {
                newStock = currentStock - quantity;
            }

            String updateSql =
                    "UPDATE products "
                    + "SET stock_quantity = ? "
                    + "WHERE product_id = ?";

            try (PreparedStatement ps =
                         con.prepareStatement(updateSql)) {

                ps.setInt(1, newStock);
                ps.setInt(2, productId);

                ps.executeUpdate();
            }

            System.out.println(
                    "\nStock updated for "
                    + productName
                    + ": "
                    + currentStock
                    + " -> "
                    + newStock
            );

        } catch (SQLException e) {

            System.out.println(
                    "\nError while updating stock."
            );

            e.printStackTrace();
        }
    }

    private static class PendingSale {

        int productId;
        String productName;
        int quantity;
        BigDecimal price;
    }

    public static void recordSales(Connection con, Scanner sc) {

        List<PendingSale> pendingSales =
                new ArrayList<>();

        boolean transactionStarted = false;

        try {

            System.out.println(
                    "\n========== RECORD SALES =========="
            );

            System.out.println(
                    "Enter product_id 0 to finish."
            );

            while (true) {

                System.out.print(
                        "\nEnter product_id (0 to finish): "
                );

                int productId;

                try {
                    productId = Integer.parseInt(sc.nextLine());
                } catch (NumberFormatException e) {

                    System.out.println(
                            "Please enter a valid number."
                    );

                    continue;
                }

                if (productId == 0) {
                    break;
                }

                String productSql =
                        "SELECT product_name, price, stock_quantity "
                        + "FROM products WHERE product_id = ?";

                String productName;
                BigDecimal price;
                int availableStock;

                try (PreparedStatement ps =
                             con.prepareStatement(productSql)) {

                    ps.setInt(1, productId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {

                            System.out.println(
                                    "No product found with id "
                                    + productId
                            );

                            continue;
                        }

                        productName =
                                rs.getString("product_name");

                        price =
                                rs.getBigDecimal("price");

                        availableStock =
                                rs.getInt("stock_quantity");
                    }
                }

                System.out.print(
                        "Enter quantity sold (available: "
                        + availableStock
                        + "): "
                );

                int quantity;

                try {
                    quantity =
                            Integer.parseInt(sc.nextLine());
                } catch (NumberFormatException e) {

                    System.out.println(
                            "Please enter a valid quantity."
                    );

                    continue;
                }

                if (quantity <= 0) {

                    System.out.println(
                            "Quantity must be greater than 0."
                    );

                    continue;
                }

                int alreadyReserved = 0;

                for (PendingSale sale : pendingSales) {

                    if (sale.productId == productId) {
                        alreadyReserved += sale.quantity;
                    }
                }

                int remainingStock =
                        availableStock - alreadyReserved;

                if (quantity > remainingStock) {

                    System.out.println(
                            "Not enough stock for "
                            + productName
                            + " - only "
                            + remainingStock
                            + " left."
                    );

                    continue;
                }

                PendingSale sale =
                        new PendingSale();

                sale.productId = productId;
                sale.productName = productName;
                sale.quantity = quantity;
                sale.price = price;

                pendingSales.add(sale);

                BigDecimal lineTotal =
                        price.multiply(
                                BigDecimal.valueOf(quantity)
                        );

                System.out.println(
                        "Added to batch: "
                        + productName
                        + " x"
                        + quantity
                        + " @ Rs."
                        + price
                        + " = Rs."
                        + lineTotal
                );
            }

            if (pendingSales.isEmpty()) {

                System.out.println(
                        "\nNo sales recorded."
                );

                return;
            }

            con.setAutoCommit(false);
            transactionStarted = true;

            String insertSql =
                    "INSERT INTO sales "
                    + "(product_id, quantity_sold, total_amount) "
                    + "VALUES (?, ?, ?)";

            String updateStockSql =
                    "UPDATE products "
                    + "SET stock_quantity = stock_quantity - ? "
                    + "WHERE product_id = ?";

            BigDecimal batchTotal =
                    BigDecimal.ZERO;

            try (PreparedStatement psInsert =
                         con.prepareStatement(insertSql);
                 PreparedStatement psStock =
                         con.prepareStatement(updateStockSql)) {

                for (PendingSale sale : pendingSales) {

                    BigDecimal lineTotal =
                            sale.price.multiply(
                                    BigDecimal.valueOf(
                                            sale.quantity
                                    )
                            );

                    batchTotal =
                            batchTotal.add(lineTotal);

                    psInsert.setInt(
                            1,
                            sale.productId
                    );

                    psInsert.setInt(
                            2,
                            sale.quantity
                    );

                    psInsert.setBigDecimal(
                            3,
                            lineTotal
                    );

                    psInsert.addBatch();

                    psStock.setInt(
                            1,
                            sale.quantity
                    );

                    psStock.setInt(
                            2,
                            sale.productId
                    );

                    psStock.addBatch();
                }

                int[] insertResults =
                        psInsert.executeBatch();

                psStock.executeBatch();

                con.commit();

                System.out.println(
                        "\n============================================"
                );

                System.out.println(
                        "      BATCH SALES RECORDED SUCCESSFULLY"
                );

                System.out.println(
                        "============================================"
                );

                System.out.println(
                        "Sales recorded : "
                        + insertResults.length
                );

                System.out.println(
                        "Batch Total    : Rs."
                        + batchTotal
                );

                System.out.println(
                        "============================================"
                );
            }

        } catch (SQLException e) {

            System.out.println(
                    "\nError while recording sales."
            );

            System.out.println(
                    "Rolling back the transaction."
            );

            if (transactionStarted) {

                try {
                    con.rollback();
                } catch (SQLException rollbackError) {

                    System.out.println(
                            "Rollback failed."
                    );
                }
            }

            e.printStackTrace();

        } finally {

            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {

                System.out.println(
                        "Could not reset transaction mode."
                );
            }
        }
    }

    public static void generateReports(Connection con) {

        System.out.println(
                "\n========== REPORTS =========="
        );

        try {

            String overallSql =
                    "SELECT "
                    + "COALESCE(SUM(total_amount), 0) AS revenue, "
                    + "COUNT(*) AS num_sales "
                    + "FROM sales";

            try (Statement st =
                         con.createStatement();
                 ResultSet rs =
                         st.executeQuery(overallSql)) {

                if (rs.next()) {

                    BigDecimal revenue =
                            rs.getBigDecimal("revenue");

                    int numberOfSales =
                            rs.getInt("num_sales");

                    System.out.println(
                            "\n--- Overall ---"
                    );

                    System.out.println(
                            "Total Revenue : Rs."
                            + revenue
                    );

                    System.out.println(
                            "Total Sales   : "
                            + numberOfSales
                    );
                }
            }

            String productReportSql =
                    "SELECT p.product_name, "
                    + "SUM(s.quantity_sold) AS total_qty, "
                    + "SUM(s.total_amount) AS revenue, "
                    + "AVG(s.total_amount) AS avg_sale_value "
                    + "FROM sales s "
                    + "JOIN products p "
                    + "ON p.product_id = s.product_id "
                    + "GROUP BY p.product_id, p.product_name "
                    + "ORDER BY revenue DESC";

            System.out.println(
                    "\n--- Sales By Product ---"
            );

            try (Statement st =
                         con.createStatement();
                 ResultSet rs =
                         st.executeQuery(
                                 productReportSql)) {

                while (rs.next()) {

                    System.out.printf(
                            "%-20s | Qty Sold: %-5d | "
                            + "Revenue: Rs.%-10s | "
                            + "Avg Sale: Rs.%s%n",

                            rs.getString(
                                    "product_name"
                            ),

                            rs.getInt(
                                    "total_qty"
                            ),

                            rs.getBigDecimal(
                                    "revenue"
                            ),

                            rs.getBigDecimal(
                                    "avg_sale_value"
                            )
                    );
                }
            }

            System.out.println(
                    "\n--- Low Stock Alert (below 20 units) ---"
            );

            String lowStockSql =
                    "SELECT product_name, stock_quantity "
                    + "FROM products "
                    + "WHERE stock_quantity < 20 "
                    + "ORDER BY stock_quantity ASC";

            boolean foundLowStock = false;

            try (Statement st =
                         con.createStatement();
                 ResultSet rs =
                         st.executeQuery(lowStockSql)) {

                while (rs.next()) {

                    foundLowStock = true;

                    System.out.println(
                            rs.getString("product_name")
                            + " -> "
                            + rs.getInt("stock_quantity")
                            + " left"
                    );
                }
            }

            if (!foundLowStock) {

                System.out.println(
                        "No products are low on stock."
                );
            }

            System.out.println(
                    "\n--- Products Per Supplier ---"
            );

            String supplierReportSql =
                    "SELECT s.supplier_name, "
                    + "COUNT(p.product_id) AS num_products "
                    + "FROM suppliers s "
                    + "LEFT JOIN products p "
                    + "ON p.supplier_id = s.supplier_id "
                    + "GROUP BY s.supplier_id, s.supplier_name "
                    + "ORDER BY num_products DESC";

            try (Statement st =
                         con.createStatement();
                 ResultSet rs =
                         st.executeQuery(
                                 supplierReportSql)) {

                while (rs.next()) {

                    System.out.println(
                            rs.getString(
                                    "supplier_name"
                            )
                            + " -> "
                            + rs.getInt(
                                    "num_products"
                            )
                            + " product(s)"
                    );
                }
            }

        } catch (SQLException e) {

            System.out.println(
                    "\nError while generating reports."
            );

            e.printStackTrace();
        }
    }
}
