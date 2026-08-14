package io.github.youngledo.vadmin.starter;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

class StarterDependencyTest {
    @Test
    void composesTheSpringAdaptersWithoutDependingOnAnApplicationOrBusinessExample() throws Exception {
        var pom = Path.of("pom.xml");
        assertThat(Files.exists(pom)).isTrue();

        var document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pom.toFile());
        var dependencies = document.getElementsByTagName("dependency");
        var coordinates = new java.util.HashMap<String, Element>();
        for (var index = 0; index < dependencies.getLength(); index++) {
            var dependency = (Element) dependencies.item(index);
            coordinates.put(text(dependency, "artifactId"), dependency);
        }

        assertThat(coordinates).containsKeys("vadmin-spring-security", "vadmin-spring-jpa", "vadmin-spring-boot",
                "vadmin-spring-flow", "spring-boot-starter", "vaadin-dev", "postgresql");
        assertThat(text(coordinates.get("vaadin-dev"), "optional")).isEqualTo("true");
        assertThat(text(coordinates.get("postgresql"), "scope")).isEqualTo("runtime");
        assertThat(coordinates).doesNotContainKeys("vadmin-reference-app", "admin-example-orders");
    }

    private static String text(Element parent, String tagName) {
        var elements = parent.getElementsByTagName(tagName);
        return elements.getLength() == 0 ? "" : elements.item(0).getTextContent().strip();
    }
}
