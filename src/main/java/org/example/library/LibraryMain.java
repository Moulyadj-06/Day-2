package org.example.library;

import com.mongodb.client.*;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

class Book {
    protected String title;
    protected String author;
    protected String type;

    public Book(String title, String author, String type) {
        this.title = title;
        this.author = author;
        this.type = type;
    }

    public Document toDocument() {
        return new Document("title", title)
                .append("author", author)
                .append("type", type);
    }

    public void displayDetails() {
        System.out.println("Title: " + title + ", Author: " + author);
    }
}

class FictionBook extends Book {
    public FictionBook(String title, String author) {
        super(title, author, "Fiction");
    }

    @Override
    public void displayDetails() {
        System.out.println("[Fiction] Title: " + title + ", Author: " + author);
    }
}

class NonFictionBook extends Book {
    public NonFictionBook(String title, String author) {
        super(title, author, "Non-Fiction");
    }

    @Override
    public void displayDetails() {
        System.out.println("[Non-Fiction] Title: " + title + ", Author: " + author);
    }
}

public class LibraryMain {
    public static void main(String[] args) {
        // MongoDB connection
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = client.getDatabase("LibraryDB");
        MongoCollection<Document> booksCollection = database.getCollection("books");

        // Book list
        List<Book> books = new ArrayList<>();

        books.add(new FictionBook("The Hobbit", "J.R.R. Tolkien"));
        books.add(new FictionBook("1984", "George Orwell"));
        books.add(new FictionBook("To Kill a Mockingbird", "Harper Lee"));
        books.add(new FictionBook("The Great Gatsby", "F. Scott Fitzgerald"));

        books.add(new NonFictionBook("Sapiens", "Yuval Noah Harari"));
        books.add(new NonFictionBook("Educated", "Tara Westover"));
        books.add(new NonFictionBook("Thinking, Fast and Slow", "Daniel Kahneman"));

        // Insert books into MongoDB
        for (Book b : books) {
            booksCollection.insertOne(b.toDocument());
        }

        System.out.println("📚 Books inserted into MongoDB!");

        // Retrieve from MongoDB and display
        System.out.println("\n📖 ---- All Books from DB ----");
        FindIterable<Document> docs = booksCollection.find();
        for (Document doc : docs) {
            System.out.printf("[%s] Title: %s, Author: %s\n",
                    doc.getString("type"), doc.getString("title"), doc.getString("author"));
        }

        client.close();
    }
}
