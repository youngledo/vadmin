package io.github.youngledo.vadmin.contracts.build;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class RootPomIdentityTest {

    @Test
    void exposesAnIndependentVadminRootBuild() throws Exception {
        Element project = document(rootPom()).getDocumentElement();

        assertThat(directText(project, "groupId")).isEqualTo("io.github.youngledo");
        assertThat(directText(project, "artifactId")).isEqualTo("vadmin");
        assertThat(directChildren(project, "parent")).isEmpty();
        assertThat(dependency(project, "spring-boot-dependencies").getTextContent())
                .contains("org.springframework.boot", "pom", "import");
        assertThat(plugin(project, "spring-boot-maven-plugin").getTextContent())
                .contains("${spring-boot.version}");
    }

    private static Path rootPom() {
        return Path.of("..").toAbsolutePath().normalize().resolve("pom.xml");
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

    private static Element dependency(Element project, String artifactId) {
        return directChildren(directChildren(project, "dependencyManagement").getFirst(), "dependencies")
                .getFirst()
                .getElementsByTagNameNS("*", "dependency")
                .item(0) instanceof Element dependency && directText(dependency, "artifactId").equals(artifactId)
                ? dependency
                : dependencies(project).stream()
                        .filter(candidate -> directText(candidate, "artifactId").equals(artifactId))
                        .findFirst()
                        .orElseThrow();
    }

    private static java.util.List<Element> dependencies(Element project) {
        Element dependencyManagement = directChildren(project, "dependencyManagement").getFirst();
        Element dependencies = directChildren(dependencyManagement, "dependencies").getFirst();
        return directChildren(dependencies, "dependency");
    }

    private static Element plugin(Element project, String artifactId) {
        Element build = directChildren(project, "build").getFirst();
        Element pluginManagement = directChildren(build, "pluginManagement").getFirst();
        Element plugins = directChildren(pluginManagement, "plugins").getFirst();
        return directChildren(plugins, "plugin").stream()
                .filter(candidate -> directText(candidate, "artifactId").equals(artifactId))
                .findFirst()
                .orElseThrow();
    }

    private static java.util.List<Element> directChildren(Element parent, String name) {
        NodeList children = parent.getChildNodes();
        java.util.List<Element> matches = new java.util.ArrayList<>();
        for (int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            if (child instanceof Element element && name.equals(element.getLocalName())) {
                matches.add(element);
            }
        }
        return matches;
    }
}
