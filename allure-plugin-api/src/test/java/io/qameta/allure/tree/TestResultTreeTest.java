package io.qameta.allure.tree;

import io.qameta.allure.entity.TestLabel;
import io.qameta.allure.entity.TestResult;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static io.qameta.allure.entity.LabelName.FEATURE;
import static io.qameta.allure.entity.LabelName.STORY;
import static io.qameta.allure.tree.TreeUtils.groupByLabels;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author charlie (Dmitry Baev).
 */
public class TestResultTreeTest {

    @Test
    public void shouldCreateEmptyTree() throws Exception {
        final TestResultTree tree = new TestResultTree(
                "default",
                item -> Collections.emptyList()
        );

        assertThat(tree.getChildren())
                .hasSize(0);
    }

    @Test
    public void shouldCrossGroup() throws Exception {
        final TestResultTree behaviors = new TestResultTree(
                "behaviors",
                testResult -> groupByLabels(testResult, FEATURE, STORY)
        );

        final TestResult first = new TestResult()
                .setName("first")
                .setLabels(new HashSet<>(asList(feature("f1"), feature("f2"), story("s1"), story("s2"))));
        final TestResult second = new TestResult()
                .setName("second")
                .setLabels(new HashSet<>(asList(feature("f2"), feature("f3"), story("s2"), story("s3"))));
        behaviors.add(first);
        behaviors.add(second);

        assertThat(behaviors.getChildren())
                .hasSize(3)
                .extracting(Node::getName)
                .containsExactlyInAnyOrder("f1", "f2", "f3");

        assertThat(behaviors.getChildren())
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("s1", "s2", "s1", "s2", "s3", "s2", "s3");

        assertThat(behaviors.getChildren())
                .filteredOn("name", "f2")
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("s1", "s2", "s3");

        assertThat(behaviors.getChildren())
                .filteredOn("name", "f2")
                .flatExtracting("children")
                .filteredOn("name", "s1")
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("first");

        assertThat(behaviors.getChildren())
                .filteredOn("name", "f2")
                .flatExtracting("children")
                .filteredOn("name", "s2")
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("first", "second");

        assertThat(behaviors.getChildren())
                .filteredOn("name", "f2")
                .flatExtracting("children")
                .filteredOn("name", "s3")
                .flatExtracting("children")
                .extracting("name")
                .containsExactlyInAnyOrder("second");
    }

    private TestLabel feature(final String value) {
        return new TestLabel().setName("feature").setValue(value);
    }

    private TestLabel story(final String value) {
        return new TestLabel().setName("story").setValue(value);
    }

}