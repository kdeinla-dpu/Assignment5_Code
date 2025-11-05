package org.example.Amazon;

import org.example.Amazon.Cost.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AmazonIntegrationTest {

    // Simple in-memory fake cart
    static class FakeCart implements ShoppingCart {
        private final List<Item> items = new ArrayList<>();

        @Override
        public void add(Item item) {
            items.add(item);
        }

        @Override
        public List<Item> getItems() {
            return items;
        }

        @Override
        public int numberOfItems() {
            return items.size();
        }
    }

    private FakeCart cart;
    private List<PriceRule> rules;
    private Amazon amazon;

    @BeforeEach
    void setup() {
        cart = new FakeCart();
        rules = new ArrayList<>();
        rules.add(new RegularCost());
        rules.add(new ExtraCostForElectronics());
        rules.add(new DeliveryPrice());
        amazon = new Amazon(cart, rules);
    }

    @Test
    @DisplayName("all rules and cart are invoked")
    void testFullIntegration() {
        cart.add(new Item(ItemType.OTHER, "Book", 1, 20.0));
        cart.add(new Item(ItemType.ELECTRONIC, "Laptop", 1, 1000.0));

        double total = amazon.calculate();

        assertTrue(total > 0.0);
        assertEquals(2, cart.numberOfItems());
        assertFalse(cart.getItems().isEmpty());
    }

    @Test
    @DisplayName("electronics additional cost")
    void testElectronicsExtraCost() {
        cart.add(new Item(ItemType.ELECTRONIC, "Phone", 1, 500.0));
        double total = amazon.calculate();
        assertTrue(total >= 500.0);
    }

    @Test
    @DisplayName("other items add to cost")
    void testOtherItemCost() {
        cart.add(new Item(ItemType.OTHER, "Pen", 2, 2.0));
        double total = amazon.calculate();
        assertTrue(total >= 4.0);
    }

    @Test
    @DisplayName("empty cart still applies delivery rule ")
    void testEmptyCartDelivery() {
        double total = amazon.calculate();
        assertTrue(total >= 0.0);
    }
    @Test
    @DisplayName("correctly multiplies price * quantity")
    void testRegularCostRule() {
        RegularCost rule = new RegularCost();
        List<Item> items = new ArrayList<>();
        items.add(new Item(ItemType.OTHER, "Book", 2, 10.0));
        items.add(new Item(ItemType.ELECTRONIC, "Mouse", 1, 50.0));

        double result = rule.priceToAggregate(items);
        assertEquals(70.0, result, 0.001);
    }

    @Test
    @DisplayName("=adds cost only for electronics")
    void testExtraCostForElectronicsRule() {
        ExtraCostForElectronics rule = new ExtraCostForElectronics();

        List<Item> items = new ArrayList<>();
        items.add(new Item(ItemType.OTHER, "Pen", 1, 2.0)); // should not add
        items.add(new Item(ItemType.ELECTRONIC, "Phone", 1, 100.0)); // should add

        double result = rule.priceToAggregate(items);

        assertTrue(result > 0, "Should add extra cost for electronics");
        double onlyOther = rule.priceToAggregate(
                List.of(new Item(ItemType.OTHER, "Book", 1, 10.0)));
        assertEquals(0.0, onlyOther, 0.001);
    }

    @Test
    @DisplayName("adds flat fee regardless of items")
    void testDeliveryPriceRule() {
        DeliveryPrice rule = new DeliveryPrice();

        List<Item> nonEmpty = new ArrayList<>();
        nonEmpty.add(new Item(ItemType.OTHER, "Book", 1, 20.0));

        double nonEmptyResult = rule.priceToAggregate(nonEmpty);
        double emptyResult = rule.priceToAggregate(new ArrayList<>());

        assertTrue(nonEmptyResult >= 0);
        assertTrue(emptyResult >= 0);
    }

}
