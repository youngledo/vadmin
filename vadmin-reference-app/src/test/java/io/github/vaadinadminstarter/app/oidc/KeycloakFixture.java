package io.github.vaadinadminstarter.app.oidc;

import java.time.Duration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

final class KeycloakFixture extends GenericContainer<KeycloakFixture> {
    private static final int HTTP_PORT = 8080;
    private static final String REALM = "vaadin-admin";

    KeycloakFixture() {
        super(DockerImageName.parse("quay.io/keycloak/keycloak:26.4.0"));
        withExposedPorts(HTTP_PORT);
        withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "admin");
        withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "admin");
        withCopyFileToContainer(MountableFile.forClasspathResource("keycloak/realm-vaadin-admin.json"),
                "/opt/keycloak/data/import/realm-vaadin-admin.json");
        withCommand("start-dev", "--import-realm");
        waitingFor(Wait.forHttp("/realms/" + REALM + "/.well-known/openid-configuration")
                .forStatusCode(200)
                .withStartupTimeout(Duration.ofMinutes(2)));
    }

    String issuerUri() {
        return "http://" + getHost() + ":" + getMappedPort(HTTP_PORT) + "/realms/" + REALM;
    }
}
