package pnnl.goss.core.itests;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Simple test runner to execute tests from command line
 */
public class TestRunner {
    public static void main(String[] args) {
        System.out.println("Running GOSS Core Tests...");

        Result result = JUnitCore.runClasses(
                BasicConnectionTest.class,
                CoreFunctionalityTest.class);

        System.out.println("\n=== Test Results ===");
        System.out.println("Tests run: " + result.getRunCount());
        System.out.println("Failures: " + result.getFailureCount());
        System.out.println("Ignored: " + result.getIgnoreCount());
        System.out.println("Success: " + result.wasSuccessful());

        if (!result.wasSuccessful()) {
            System.out.println("\n=== Failures ===");
            for (Failure failure : result.getFailures()) {
                System.out.println(failure.toString());
                System.out.println(failure.getTrace());
            }
        }

        System.exit(result.wasSuccessful() ? 0 : 1);
    }
}
