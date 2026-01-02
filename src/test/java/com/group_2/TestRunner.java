package com.group_2;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

/**
 * Simple test runner that can be executed via main() method.
 * Run this class to execute all tests in the project.
 * 
 * Usage: Right-click → Run 'TestRunner.main()'
 */
public class TestRunner {

    public static void main(String[] args) {
        System.out.println("=== Starting Test Suite ===\n");

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage("com.group_2"))
                .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();

        System.out.println("\n=== Test Results ===");
        System.out.println("Tests found:    " + summary.getTestsFoundCount());
        System.out.println("Tests started:  " + summary.getTestsStartedCount());
        System.out.println("Tests passed:   " + summary.getTestsSucceededCount());
        System.out.println("Tests failed:   " + summary.getTestsFailedCount());
        System.out.println("Tests skipped:  " + summary.getTestsSkippedCount());

        if (summary.getTestsFailedCount() > 0) {
            System.out.println("\n=== Failures ===");
            summary.getFailures().forEach(failure -> {
                System.out.println("FAILED: " + failure.getTestIdentifier().getDisplayName());
                System.out.println("  Reason: " + failure.getException().getMessage());
            });
            System.exit(1);
        } else {
            System.out.println("\n✓ All tests passed!");
            System.exit(0);
        }
    }
}
