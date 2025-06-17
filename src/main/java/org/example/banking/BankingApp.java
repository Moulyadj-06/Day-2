package org.example.banking;

import com.mongodb.client.*;
import org.bson.Document;
import org.example.library.MongoDBConnection;

import java.util.HashMap;
import java.util.Scanner;

public class BankingApp {
    public static void main(String[] args) {
        MongoDatabase database = MongoDBConnection.getDatabase("BankingDB");
        MongoCollection<Document> accountsCollection = database.getCollection("accounts");

        // Load existing accounts from MongoDB
        HashMap<String, BankAccount> accounts = loadAccounts(accountsCollection);

        System.out.println("🔐 Loaded " + accounts.size() + " accounts from MongoDB!");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n1. Create Account\n2. Deposit\n3. Withdraw\n4. Check Balance\n5. View All\n6. Exit");
            System.out.print("Choose: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1 -> createAccount(scanner, accounts, accountsCollection);
                case 2 -> deposit(scanner, accounts, accountsCollection);
                case 3 -> withdraw(scanner, accounts, accountsCollection);
                case 4 -> checkBalance(scanner, accounts);
                case 5 -> viewAll(accounts);
                case 6 -> {
                    System.out.println("👋 Thank you for using the bank system!");
                    return;
                }
                default -> System.out.println("❗ Invalid choice.");
            }
        }
    }

    private static HashMap<String, BankAccount> loadAccounts(MongoCollection<Document> collection) {
        HashMap<String, BankAccount> accounts = new HashMap<>();
        FindIterable<Document> docs = collection.find();
        for (Document doc : docs) {
            String accNo = doc.getString("accountNumber");
            String holder = doc.getString("accountHolder");
            Object balObj = doc.get("balance");
            double balance = (balObj instanceof Integer)
                    ? ((Integer) balObj).doubleValue()
                    : (Double) balObj;
            accounts.put(accNo, new BankAccount(accNo, holder, balance));
        }
        return accounts;
    }

    private static void createAccount(Scanner scanner, HashMap<String, BankAccount> accounts, MongoCollection<Document> collection) {
        System.out.print("Enter new account number: ");
        String accNo = scanner.nextLine();
        if (accounts.containsKey(accNo)) {
            System.out.println("⚠️ Account already exists.");
            return;
        }

        System.out.print("Enter account holder name: ");
        String name = scanner.nextLine();

        BankAccount newAcc = new BankAccount(accNo, name, 0.0);
        accounts.put(accNo, newAcc);

        Document doc = new Document("accountNumber", accNo)
                .append("accountHolder", name)
                .append("balance", 0.0);
        collection.insertOne(doc);

        System.out.println("✅ Account created.");
    }

    private static void deposit(Scanner scanner, HashMap<String, BankAccount> accounts, MongoCollection<Document> collection) {
        System.out.print("Enter account number: ");
        String accNo = scanner.nextLine();
        if (!accounts.containsKey(accNo)) {
            System.out.println("❌ Account not found.");
            return;
        }

        System.out.print("Enter amount to deposit: ");
        double amt = scanner.nextDouble();
        scanner.nextLine();

        try {
            BankAccount acc = accounts.get(accNo);
            acc.deposit(amt);
            collection.updateOne(
                    new Document("accountNumber", accNo),
                    new Document("$set", new Document("balance", acc.getBalance()))
            );
            System.out.println("✅ Deposit successful.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private static void withdraw(Scanner scanner, HashMap<String, BankAccount> accounts, MongoCollection<Document> collection) {
        System.out.print("Enter account number: ");
        String accNo = scanner.nextLine();
        if (!accounts.containsKey(accNo)) {
            System.out.println("❌ Account not found.");
            return;
        }

        System.out.print("Enter amount to withdraw: ");
        double amt = scanner.nextDouble();
        scanner.nextLine();

        try {
            BankAccount acc = accounts.get(accNo);
            acc.withdraw(amt);
            collection.updateOne(
                    new Document("accountNumber", accNo),
                    new Document("$set", new Document("balance", acc.getBalance()))
            );
            System.out.println("✅ Withdrawal successful.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private static void checkBalance(Scanner scanner, HashMap<String, BankAccount> accounts) {
        System.out.print("Enter account number: ");
        String accNo = scanner.nextLine();
        if (!accounts.containsKey(accNo)) {
            System.out.println("❌ Account not found.");
            return;
        }
        System.out.printf("💰 Balance: ₹%.2f%n", accounts.get(accNo).getBalance());
    }

    private static void viewAll(HashMap<String, BankAccount> accounts) {
        System.out.println("📄 All Accounts:");
        for (BankAccount acc : accounts.values()) {
            acc.displayDetails();
        }
    }
}
