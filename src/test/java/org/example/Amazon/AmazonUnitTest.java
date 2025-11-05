package org.example.Amazon;

import org.example.Amazon.Cost.ItemType;
import org.example.Amazon.Cost.PriceRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AmazonUnitTest {

    static class FakeItem extends Item {
        FakeItem(String name, double price) {
            super(ItemType.values()[0], name, 1, price);
        }
    }

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

    static class FakeRule implements PriceRule {
        private final double value;
        boolean called = false;

        FakeRule(double value) {
            this.value = value;
        }

        @Override
        public double priceToAggregate(List<Item> items) {
            called = true;
            return value * items.size();
        }
    }

    private FakeCart cart;
    private List<PriceRule> rules;
    private Amazon amazon;

    @BeforeEach
    void setup() {
        cart = new FakeCart();
        rules = new ArrayList<>();
        amazon = new Amazon(cart, rules);
    }
    @Test
    @DisplayName("Empty rule list returns total 0.0")
    void testEmptyRules() {
        double total = amazon.calculate();
        assertEquals(0.0, total, 0.001);
        assertEquals(0, cart.numberOfItems());
    }

    @Test
    @DisplayName("Single rule applies correctly to empty cart")
    void testSingleRuleEmptyCart() {
        rules.add(new FakeRule(5.0));
        double total = amazon.calculate();
        assertEquals(0.0, total, 0.001);
    }

    @Test
    @DisplayName("Single rule applies correctly to populated cart")
    void testSingleRuleWithItems() {
        cart.add(new FakeItem("Book", 10.0));
        cart.add(new FakeItem("Pen", 2.0));
        rules.add(new FakeRule(5.0));

        double total = amazon.calculate();

        assertEquals(10.0, total, 0.001);
    }

    @Test
    @DisplayName("Multiple rules aggregate correctly")
    void testMultipleRulesWithItems() {
        cart.add(new FakeItem("Laptop", 100.0));
        cart.add(new FakeItem("Mouse", 20.0));

        rules.add(new FakeRule(2.0));
        rules.add(new FakeRule(3.0));
        rules.add(new FakeRule(1.0));

        double total = amazon.calculate();
        assertEquals(12.0, total, 0.001);
    }

    @Test
    @DisplayName("Each PriceRule is invoked during calculation")
    void testRulesAreInvoked() {
        FakeRule rule1 = new FakeRule(5.0);
        FakeRule rule2 = new FakeRule(10.0);

        rules.add(rule1);
        rules.add(rule2);

        cart.add(new FakeItem("Phone", 500.0));
        amazon.calculate();

        assertTrue(rule1.called, "Rule1 should be called");
        assertTrue(rule2.called, "Rule2 should be called");
    }

    @Test
    @DisplayName("AddToCart actually stores items in FakeCart")
    void testAddToCart() {
        FakeItem item = new FakeItem("Keyboard", 50.0);
        amazon.addToCart(item);

        assertEquals(1, cart.numberOfItems());
        assertEquals(item, cart.getItems().get(0));
    }

    @Test
    @DisplayName("FakeCart numberOfItems() returns correct count")
    void testFakeCartNumberOfItems() {
        FakeItem i1 = new FakeItem("Item1", 10.0);
        FakeItem i2 = new FakeItem("Item2", 15.0);
        FakeItem i3 = new FakeItem("Item3", 25.0);

        cart.add(i1);
        cart.add(i2);
        cart.add(i3);

        assertEquals(3, cart.numberOfItems());
    }
}
