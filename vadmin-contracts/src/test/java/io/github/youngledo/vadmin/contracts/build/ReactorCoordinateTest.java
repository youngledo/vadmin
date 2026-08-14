package io.github.youngledo.vadmin.contracts.build;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class ReactorCoordinateTest {

    @Test
    void usesOnlyVadminReactorCoordinates() throws Exception {
        List<Path> pomFiles = reactorPoms();
        List<String> artifactIds = pomFiles.stream().map(ReactorCoordinateTest::artifactId).toList();
        String allPomContent = pomFiles.stream().map(this::read).reduce("", String::concat);

        assertThat(artifactIds).containsExactly(
                "vadmin", "vadmin-contracts", "vadmin-platform", "vadmin-flow",
                "vadmin-spring", "vadmin-spring-security", "vadmin-spring-jpa",
                "vadmin-spring-boot", "vadmin-spring-flow", "vadmin-spring-boot-starter",
                "vadmin-reference-app");
        assertThat(allPomContent)
                .doesNotContain("io.github.youngledo.vadmin", "<artifactId>admin-");
        assertThat(pomFiles.stream().skip(1).map(ReactorCoordinateTest::parentGroupId))
                .containsOnly("io.github.youngledo");
    }

    private static List<Path> reactorPoms() throws Exception {
        Path root = Path.of("..").toAbsolutePath().normalize();
        List<Path> pomFiles = new ArrayList<>();
        Path rootPom = root.resolve("pom.xml");
        pomFiles.add(rootPom);
        for (String module : modules(rootPom)) {
            Path modulePom = root.resolve(module).resolve("pom.xml");
            pomFiles.add(modulePom);
            for (String child : modules(modulePom)) {
                pomFiles.add(modulePom.getParent().resolve(child).resolve("pom.xml"));
            }
        }
        return pomFiles;
    }

    private static List<String> modules(Path pom) throws Exception {
        Element project = document(pom).getDocumentElement();
        return directChildren(directChildren(project, "modules").stream().findFirst().orElse(null), "module")
                .stream()
                .map(Element::getTextContent)
                .map(String::strip)
                .toList();
    }

    private static String artifactId(Path pom) {
        try {
            return directText(document(pom).getDocumentElement(), "artifactId");
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot read " + pom, exception);
        }
    }

    private static String parentGroupId(Path pom) {
        try {
            return directText(directChildren(document(pom).getDocumentElement(), "parent").getFirst(), "groupId");
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot read " + pom, exception);
        }
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot read " + path, exception);
        }
    }

    private static org.w3c.dom.Document document(Path pom) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder().parse(pom.toFile());
    }

    private static String directText(Element parent, String name) {
        return directChildren(parent, name).getFirst().getTextContent().strip();
    }

    private static List<Element> directChildren(Element parent, String name) {
        if (parent == null) {
            return List.of();
        }
        NodeList children = parent.getChildNodes();
        List<Element> matches = new ArrayList<>();
        for (int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            if (child instanceof Element element && name.equals(element.getLocalName())) {
                matches.add(element);
            }
        }
        return matches;
    }
}
