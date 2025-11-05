package org.example.Barnes;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class BarnesAndNobleTest {

    private BarnesAndNoble barnes;
    private FakeBookDatabase db;
    private FakeBuyBookProcess process;

    static class FakeBookDatabase implements BookDatabase {
        private final Map<String, Book> books = new HashMap<>();

        void addBook(String isbn, Book book) {
            books.put(isbn, book);
        }

        @Override
        public Book findByISBN(String ISBN) {
            return books.get(ISBN);
        }
    }

    static class FakeBuyBookProcess implements BuyBookProcess {
        boolean called = false;
        Book lastBook;
        int lastQuantity;

        @Override
        public void buyBook(Book book, int amount) {
            if (amount > 0) {
                called = true;
                lastBook = book;
                lastQuantity = amount;
            }
        }
    }

    @BeforeEach
    void setup() {
        db = new FakeBookDatabase();
        process = new FakeBuyBookProcess();
        barnes = new BarnesAndNoble(db, process);

        db.addBook("111", new Book("111", 10, 5));
        db.addBook("222", new Book("222", 5, 1));
        db.addBook("333", new Book("333", 0, 10));
        db.addBook("444", new Book("444", 10, 0));
    }

    @Test
    @DisplayName("Specification-Based")
    void testNullOrder() {
        assertNull(barnes.getPriceForCart(null));
        assertFalse(process.called);
    }

    @Test
    @DisplayName("Specification-Based")
    void testEmptyOrder() {
        Map<String, Integer> order = new HashMap<>();

        PurchaseSummary summary = barnes.getPriceForCart(order);

        assertNotNull(summary);
        assertEquals(0, summary.getTotalPrice(), 0.001);
        assertTrue(summary.getUnavailable().isEmpty());
        assertFalse(process.called);
    }

    @Test
    @DisplayName("Specification-Based")
    void testValidOrder() {
        Map<String, Integer> order = new HashMap<>();
        order.put("111", 2);

        PurchaseSummary summary = barnes.getPriceForCart(order);

        assertNotNull(summary);
        assertEquals(20, summary.getTotalPrice(), 0.001);
        assertTrue(process.called);
        assertEquals(db.findByISBN("111"), process.lastBook);
        assertEquals(2, process.lastQuantity);
        assertTrue(summary.getUnavailable().isEmpty());

    }

    @Test
    @DisplayName("Structural-Based")
    void testLowStock() {
        Map<String, Integer> order = new HashMap<>();
        order.put("222", 3);

        PurchaseSummary summary = barnes.getPriceForCart(order);

        assertEquals(5, summary.getTotalPrice(), 0.001);
        assertTrue(summary.getUnavailable().containsKey(db.findByISBN("222")));
        assertEquals(2, summary.getUnavailable().get(db.findByISBN("222")));
        assertTrue(process.called);
        assertEquals(1, process.lastQuantity);
    }

    @Test
    @DisplayName("Specification-Based")
    void testOutOfStock() {
        Map<String, Integer> order = new HashMap<>();
        order.put("444", 2);

        PurchaseSummary summary = barnes.getPriceForCart(order);

        assertEquals(0, summary.getTotalPrice(), 0.001);
        assertTrue(summary.getUnavailable().containsKey(db.findByISBN("444")));
        assertEquals(2, summary.getUnavailable().get(db.findByISBN("444")));

        if (process.called) {
            assertEquals(0, process.lastQuantity);
        }
    }

    @Test
    @DisplayName("Structural-Based")
    void testInvalidOrder() {
        Map<String, Integer> order = new HashMap<>();
        order.put("222", 3);

        PurchaseSummary summary = barnes.getPriceForCart(order);

        assertEquals(5, summary.getTotalPrice(), 0.001);
        assertTrue(summary.getUnavailable().containsKey(db.findByISBN("222")));
        assertEquals(2, summary.getUnavailable().get(db.findByISBN("222")));
        assertTrue(process.called);
        assertEquals(db.findByISBN("222"), process.lastBook);
        assertEquals(1, process.lastQuantity);
    }

    @Test
    @DisplayName("Specification-Based")
    void testFreeBook() {
        Map<String, Integer> order = new HashMap<>();
        order.put("333", 1);

        PurchaseSummary summary = barnes.getPriceForCart(order);

        assertEquals(0, summary.getTotalPrice(), 0.001);
        assertTrue(process.called);
        assertEquals(db.findByISBN("333"), process.lastBook);
        assertEquals(1, process.lastQuantity);
        assertTrue(summary.getUnavailable().isEmpty());
    }

    @Test
    @DisplayName("Structural-Based")
    void TestMultipleBooks() {
        Map<String, Integer> order = new HashMap<>();
        order.put("111", 2);
        order.put("222", 1);
        order.put("333", 1);

        PurchaseSummary summary = barnes.getPriceForCart(order);

        int expectedTotal = (2 * 10) + (1 * 5) + (1 * 0);
        assertEquals(expectedTotal, summary.getTotalPrice(), 0.001);
        assertTrue(summary.getUnavailable().isEmpty());
        assertTrue(process.called);
    }
    @Test
    @DisplayName("Structural-Based")
    void testBookNotFound() {
        Map<String, Integer> order = new HashMap<>();
        order.put("999", 2);

        assertThrows(NullPointerException.class, () -> barnes.getPriceForCart(order));
    }
    @Test
    @DisplayName("Structural-Based")
    void testExactStock() {
        Map<String, Integer> order = new HashMap<>();
        order.put("111", 5);

        PurchaseSummary summary = barnes.getPriceForCart(order);

        assertEquals(50, summary.getTotalPrice(), 0.001);
        assertTrue(summary.getUnavailable().isEmpty());
        assertTrue(process.called);
        assertEquals(5, process.lastQuantity);
    }
    @Test
    @DisplayName("Structural-Based")
    void testZeroQuantity() {
        Map<String, Integer> order = new HashMap<>();
        order.put("111", 0);

        PurchaseSummary summary = barnes.getPriceForCart(order);

        assertEquals(0, summary.getTotalPrice(), 0.001);
        assertTrue(summary.getUnavailable().isEmpty());
        assertFalse(process.called);
    }
}
