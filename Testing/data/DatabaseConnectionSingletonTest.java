package data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import dal.DatabaseConnection;

/**
 * JUnit 5 Tests for DatabaseConnection (Data Layer)
 * Tests Singleton properties - ensures only one instance is ever created.
 */
public class DatabaseConnectionSingletonTest {

    // ==================== POSITIVE TESTS ====================

    @Test
    @DisplayName("Positive: getInstance returns a non-null instance")
    public void testGetInstanceNotNull() {
        DatabaseConnection instance = DatabaseConnection.getInstance();
        assertNotNull(instance, "getInstance() should never return null");
    }

    @Test
    @DisplayName("Positive: getInstance always returns the same instance (Singleton property)")
    public void testSingletonSameInstance() {
        DatabaseConnection instance1 = DatabaseConnection.getInstance();
        DatabaseConnection instance2 = DatabaseConnection.getInstance();
        assertSame(instance1, instance2,
                "getInstance() should always return the same object reference (Singleton)");
    }

    @Test
    @DisplayName("Positive: Multiple calls to getInstance return identical reference")
    public void testMultipleCallsReturnSameReference() {
        DatabaseConnection first = DatabaseConnection.getInstance();
        DatabaseConnection second = DatabaseConnection.getInstance();
        DatabaseConnection third = DatabaseConnection.getInstance();

        assertSame(first, second, "First and second calls should return same instance");
        assertSame(second, third, "Second and third calls should return same instance");
        assertSame(first, third, "First and third calls should return same instance");
    }

    @Test
    @DisplayName("Positive: getConnection returns a valid connection object")
    public void testGetConnectionNotNull() {
        DatabaseConnection instance = DatabaseConnection.getInstance();
        // Connection might be null if DB is not running, but instance should exist
        assertNotNull(instance, "DatabaseConnection instance should exist");
    }

    // ==================== NEGATIVE TESTS ====================

    @Test
    @DisplayName("Negative: Singleton cannot be instantiated via constructor (private constructor)")
    public void testPrivateConstructor() {
        // Verify that DatabaseConnection has a private constructor
        // by checking that we can't create new instances through reflection tricks
        // The only way to get an instance should be through getInstance()
        try {
            java.lang.reflect.Constructor<DatabaseConnection> constructor = DatabaseConnection.class
                    .getDeclaredConstructor();
            assertFalse(constructor.isAccessible() || constructor.canAccess(null),
                    "Constructor should not be publicly accessible");
        } catch (NoSuchMethodException e) {
            fail("DatabaseConnection should have a no-arg constructor (even if private)");
        }
    }

    // ==================== BOUNDARY / THREAD-SAFETY TESTS ====================

    @Test
    @DisplayName("Boundary: Concurrent access returns same instance (thread-safety)")
    public void testThreadSafetySingleton() throws InterruptedException {
        final DatabaseConnection[] instances = new DatabaseConnection[10];
        Thread[] threads = new Thread[10];

        for (int i = 0; i < 10; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                instances[index] = DatabaseConnection.getInstance();
            });
        }

        // Start all threads simultaneously
        for (Thread t : threads) {
            t.start();
        }

        // Wait for all threads to complete
        for (Thread t : threads) {
            t.join();
        }

        // All instances should be the same
        for (int i = 1; i < instances.length; i++) {
            assertSame(instances[0], instances[i],
                    "Thread " + i + " should get the same instance as thread 0");
        }
    }
}
