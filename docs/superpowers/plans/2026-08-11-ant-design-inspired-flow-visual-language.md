# Ant Design-Inspired Flow Visual Language Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add host-configured `ant` and `vaadin` visual-language profiles plus comfortable/compact density to the reference Flow application without changing business views or adding a frontend runtime.

**Architecture:** The reference application binds raw Spring properties, resolves them to whitelisted host-owned enums, and applies `data-admin-visual-language` and `data-admin-density` on the Vaadin UI root. The single host-owned `admin-theme` retains public `admin-*` semantic tokens and applies profile/density layers; existing `theme="dark"` remains the independent session color-mode axis.

**Tech Stack:** Java 25, Spring Boot 4.1, Vaadin Flow 25.2, CSS custom properties, JUnit 5, AssertJ, Testcontainers PostgreSQL, Playwright, Maven 4.

---

## File Structure

- Create `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/theme/AdminVisualLanguage.java`: whitelist and CSS value for the profile.
- Create `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/theme/AdminDensity.java`: whitelist and CSS value for density.
- Create `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/theme/AdminAppearanceProperties.java`: lenient binding and typed fallback resolution.
- Modify `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/ApplicationConfiguration.java`: register host appearance properties.
- Modify `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/MainLayout.java`: apply root attributes on attach without changing the palette menu.
- Modify `admin-reference-app/src/main/resources/application.yaml`: declare appearance defaults.
- Create `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/theme/AdminAppearancePropertiesTest.java`: unit/configuration coverage.
- Modify `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/ApplicationConfigurationTest.java`: assert registration and configured values.
- Modify `admin-reference-app/src/main/frontend/themes/admin-theme/styles.css`: profile and density token layers with scoped Vaadin parts.
- Modify `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/theme/AdminThemeTokenTest.java`: stylesheet/documentation contract coverage.
- Create `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/AntVisualLanguageE2EIT.java`: Ant comfortable light/dark browser coverage.
- Create `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/AntCompactVisualLanguageE2EIT.java`: Ant compact desktop/narrow browser coverage.
- Modify `docs/en/theme-tokens.md`, `docs/en/extension-guide.md`, and `docs/zh-CN/extension-guide.md`; create `docs/en/appearance-profiles.md` and `docs/zh-CN/appearance-profiles.md`: public host/module contract.

### Task 1: Bind And Apply Host Appearance

**Files:**
- Create: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/theme/AdminVisualLanguage.java`
- Create: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/theme/AdminDensity.java`
- Create: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/theme/AdminAppearanceProperties.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/ApplicationConfiguration.java`
- Modify: `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/MainLayout.java`
- Modify: `admin-reference-app/src/main/resources/application.yaml`
- Create: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/theme/AdminAppearancePropertiesTest.java`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/ApplicationConfigurationTest.java`

- [ ] **Step 1: Write the failing resolution tests**

```java
@Test
void resolvesKnownAppearanceValuesCaseInsensitively() {
    var properties = new AdminAppearanceProperties();
    properties.setVisualLanguage("AnT");
    properties.setDensity("COMPACT");
    assertThat(properties.visualLanguage()).isEqualTo(AdminVisualLanguage.ANT);
    assertThat(properties.density()).isEqualTo(AdminDensity.COMPACT);
}

@Test
void fallsBackToSafeValuesForUnknownAppearanceValues() {
    var properties = new AdminAppearanceProperties();
    properties.setVisualLanguage("untrusted-selector");
    properties.setDensity(" ");
    assertThat(properties.visualLanguage()).isEqualTo(AdminVisualLanguage.VAADIN);
    assertThat(properties.density()).isEqualTo(AdminDensity.COMFORTABLE);
}
```

Extend `ApplicationConfigurationTest` to pass `app.appearance.visual-language=ant` and `app.appearance.density=compact` to its existing context runner, then assert the single `AdminAppearanceProperties` bean resolves to `ANT` and `COMPACT`.

- [ ] **Step 2: Run the test to verify it fails**

Run: `./mvnw -B -ntp -pl :admin-reference-app -Dtest=AdminAppearancePropertiesTest,ApplicationConfigurationTest test`

Expected: compilation fails because the new types do not exist.

- [ ] **Step 3: Implement strict values over lenient raw binding**

Use host-owned enums with CSS values and a null-safe parser; do not bind directly to an enum, because unknown deployment values must fall back rather than prevent startup.

```java
public enum AdminVisualLanguage {
    VAADIN("vaadin"), ANT("ant");

    public static AdminVisualLanguage from(String value) {
        var normalized = value == null ? "" : value.trim();
        return Arrays.stream(values())
                .filter(candidate -> candidate.cssValue.equalsIgnoreCase(normalized))
                .findFirst().orElse(VAADIN);
    }

    public String cssValue() { return cssValue; }
}
```

Implement `AdminDensity` in the same package with `COMFORTABLE("comfortable")`, `COMPACT("compact")`, and `COMFORTABLE` fallback. Add this class, keeping raw strings private and exposing only typed accessors:

```java
@ConfigurationProperties("app.appearance")
public final class AdminAppearanceProperties {
    private String visualLanguage = "vaadin";
    private String density = "comfortable";
    public AdminVisualLanguage visualLanguage() { return AdminVisualLanguage.from(visualLanguage); }
    public AdminDensity density() { return AdminDensity.from(density); }
    public void setVisualLanguage(String visualLanguage) { this.visualLanguage = visualLanguage; }
    public void setDensity(String density) { this.density = density; }
}
```

Register it together with the existing properties:

```java
@EnableConfigurationProperties({OidcIdentityLinkProperties.class, AdminAppearanceProperties.class})
```

Add, within the existing top-level `app:` mapping in `application.yaml`:

```yaml
  appearance:
    visual-language: ${APP_APPEARANCE_VISUAL_LANGUAGE:vaadin}
    density: ${APP_APPEARANCE_DENSITY:comfortable}
```

- [ ] **Step 4: Apply the typed values on UI attachment**

Inject `AdminAppearanceProperties` into `MainLayout`, then add this method and call it before the existing `applyTheme(sessionThemeMode())` in `onAttach`:

```java
private void applyHostAppearance() {
    var root = UI.getCurrent().getElement();
    root.setAttribute("data-admin-visual-language", appearance.visualLanguage().cssValue());
    root.setAttribute("data-admin-density", appearance.density().cssValue());
}
```

Do not add profile or density to `appearanceMenu`; it remains a light/dark session control. Do not change `admin-flow` or `admin-spring-flow`.

- [ ] **Step 5: Run the test to verify it passes**

Run: `./mvnw -B -ntp -pl :admin-reference-app -Dtest=AdminAppearancePropertiesTest,ApplicationConfigurationTest test`

Expected: all targeted tests pass, including old context tests.

- [ ] **Step 6: Commit**

```bash
git add admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/theme \
  admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/ApplicationConfiguration.java \
  admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/MainLayout.java \
  admin-reference-app/src/main/resources/application.yaml \
  admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/theme/AdminAppearancePropertiesTest.java \
  admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/ApplicationConfigurationTest.java
git commit -m "feat: configure host visual language"
```

### Task 2: Add Token, Profile, And Density Layers

**Files:**
- Modify: `admin-reference-app/src/main/frontend/themes/admin-theme/styles.css`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/theme/AdminThemeTokenTest.java`

- [ ] **Step 1: Write failing stylesheet contract assertions**

Extend `REQUIRED_TOKENS` with `control-height` and `grid-cell-padding`, then require these selectors and mappings:

```java
assertThat(styles).contains("[data-admin-visual-language=\"ant\"]");
assertThat(styles).contains("[data-admin-visual-language=\"ant\"][theme~=\"dark\"]");
assertThat(styles).contains("[data-admin-density=\"compact\"]");
assertThat(styles).contains("--admin-control-height:");
assertThat(styles).contains("--admin-grid-cell-padding:");
assertThat(styles).contains("--lumo-size-m: var(--admin-control-height);");
assertThat(styles).contains("--admin-accent: #1677ff;");
```

Keep the existing assertions for both Vaadin-baseline color scopes and Lumo mappings.

- [ ] **Step 2: Run the test to verify it fails**

Run: `./mvnw -B -ntp -pl :admin-reference-app -Dtest=AdminThemeTokenTest test`

Expected: failure reports absent profile/density selectors and tokens.

- [ ] **Step 3: Implement ordered semantic token layers**

Retain current `:root` and dark values as the Vaadin baseline. Add these defaults to both baseline color scopes and map the control height once through Lumo:

```css
--admin-control-height: 2.25rem;
--admin-grid-cell-padding: 0.5rem 1rem;
--lumo-size-m: var(--admin-control-height);
```

After the baseline dark scope, add profile-specific semantic values. The Ant light block must include `--admin-canvas: #f5f5f5`, `--admin-surface: #ffffff`, `--admin-border: #d9d9d9`, `--admin-accent: #1677ff`, `--admin-accent-hover: #4096ff`, Ant-style text hierarchy, restrained elevation, and semantic state colors. Add a distinct `[data-admin-visual-language="ant"][theme~="dark"]` block with `#141414` canvas, `#1f1f1f` surface, `#424242` border, `#1668dc` accent, and dark-contrast text/state values. Lumo variables must continue to reference `admin-*` tokens rather than Ant literals.

- [ ] **Step 4: Implement density and constrained component rules**

Add these independent density selectors after profile layers:

```css
[data-admin-density="comfortable"] {
  --admin-space-sm: 0.5rem;
  --admin-space-md: 1rem;
  --admin-space-lg: 1.5rem;
  --admin-control-height: 2.25rem;
  --admin-grid-cell-padding: 0.5rem 1rem;
}

[data-admin-density="compact"] {
  --admin-space-sm: 0.375rem;
  --admin-space-md: 0.75rem;
  --admin-space-lg: 1rem;
  --admin-control-height: 2rem;
  --admin-grid-cell-padding: 0.375rem 0.75rem;
}

vaadin-grid { --vaadin-grid-cell-content-padding: var(--admin-grid-cell-padding); }
```

Use only `[data-admin-visual-language="ant"]` selectors to refine existing shell, controls, workspace, button, field, menu, dialog, and Grid parts. Use semantic tokens, modest radii/elevation, flat control surfaces, and a clear primary-action accent. Do not add global wildcard selectors, gradients, custom icons, page wrappers, or a profile-specific Java API.

- [ ] **Step 5: Run the test to verify it passes**

Run: `./mvnw -B -ntp -pl :admin-reference-app -Dtest=AdminThemeTokenTest test`

Expected: baseline, dark, Ant profile, density, and Lumo-contract assertions pass.

- [ ] **Step 6: Commit**

```bash
git add admin-reference-app/src/main/frontend/themes/admin-theme/styles.css \
  admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/theme/AdminThemeTokenTest.java
git commit -m "feat: add Ant-inspired Flow theme profile"
```

### Task 3: Add Cross-Profile Browser Acceptance

**Files:**
- Create: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/AntVisualLanguageE2EIT.java`
- Create: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/AntCompactVisualLanguageE2EIT.java`

- [ ] **Step 1: Write the failing Ant comfortable tests**

Create a self-contained Playwright/Testcontainers class based on the minimal fixture helpers in `BrowserE2EIT`, with:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"app.appearance.visual-language=ant", "app.appearance.density=comfortable"})
@Testcontainers
@ActiveProfiles("development")
class AntVisualLanguageE2EIT { }
```

After admin sign-in, verify `/users` and `/orders` share the unchanged page frame and visible grids, `/roles` and `/audit` retain read-only frames, and `/customers` retains its empty workspace. Assert root attributes are `ant` and `comfortable`. Switch the existing palette menu to dark and assert the root keeps `ant`, `theme=dark`, `--admin-canvas` is `#141414`, and the Customer workspace remains visible. Tab to a shell utility and assert its visible focus outline/focus-ring color is not transparent.

- [ ] **Step 2: Run it to verify it fails**

Run: `./mvnw -B -ntp -pl :admin-reference-app -Dit.test=AntVisualLanguageE2EIT verify`

Expected: test fails until host attributes and Ant tokens exist. Docker and Playwright Chromium are prerequisites.

- [ ] **Step 3: Write the failing Ant compact tests**

Create the same focused fixture with `app.appearance.density=compact`. Assert the root attributes, compare computed compact `--admin-control-height` and `--admin-grid-cell-padding` against the comfortable values, and prove both are smaller. At a 390px mobile viewport, open the drawer, navigate to Customers, and use the existing `assertNarrowShellDoesNotOverflow` bounding-box algorithm. Select dark mode and verify compact density remains active while the Customer create/edit dialog is visible and usable.

- [ ] **Step 4: Run it to verify it fails**

Run: `./mvnw -B -ntp -pl :admin-reference-app -Dit.test=AntCompactVisualLanguageE2EIT verify`

Expected: failure until compact profile behavior is present.

- [ ] **Step 5: Run Ant and baseline browser regressions**

Run:

```bash
./mvnw -B -ntp -pl :admin-reference-app -Dit.test=AntVisualLanguageE2EIT,AntCompactVisualLanguageE2EIT verify
./mvnw -B -ntp -pl :admin-reference-app -Dit.test=BrowserE2EIT verify
```

Expected: Ant light/dark comfortable, Ant light/dark compact, and Vaadin baseline suites all pass.

- [ ] **Step 6: Commit**

```bash
git add admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/AntVisualLanguageE2EIT.java \
  admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/AntCompactVisualLanguageE2EIT.java
git commit -m "test: cover Flow visual language profiles"
```

### Task 4: Document The Host And Module Contract

**Files:**
- Modify: `docs/en/theme-tokens.md`
- Create: `docs/en/appearance-profiles.md`
- Create: `docs/zh-CN/appearance-profiles.md`
- Modify: `docs/en/extension-guide.md`
- Modify: `docs/zh-CN/extension-guide.md`
- Modify: `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/theme/AdminThemeTokenTest.java`

- [ ] **Step 1: Write failing documentation assertions**

Extend `AdminThemeTokenTest.documentsEveryRequiredSemanticToken` so the new control-height and grid-padding tokens are required in `docs/en/theme-tokens.md`. Add a test reading `docs/en/appearance-profiles.md` that asserts it contains `app.appearance.visual-language`, `app.appearance.density`, `vaadin`, `ant`, `comfortable`, and `compact`.

- [ ] **Step 2: Run the test to verify it fails**

Run: `./mvnw -B -ntp -pl :admin-reference-app -Dtest=AdminThemeTokenTest test`

Expected: failure because the new public contract is undocumented.

- [ ] **Step 3: Publish bilingual, host-owned guidance**

Update `docs/en/theme-tokens.md` to explain the independent visual-language, color-mode, and density axes; document the two new tokens; and forbid module-level profile selection or global Lumo mutation. Create `docs/en/appearance-profiles.md` and `docs/zh-CN/appearance-profiles.md` with this exact configuration example:

```yaml
app:
  appearance:
    visual-language: ant # vaadin | ant
    density: compact # comfortable | compact
```

Explain that the profile is Flow-native, contains no React/Vue/Ant runtime dependency, is chosen by the host, and does not replace the user/session light-dark menu. Add the same module rule in English and Chinese extension guides: modules may use public `admin-*` tokens and shared patterns but may not import `admin-theme`, add a global `@Theme`, select a profile/density, or depend on Ant-only selectors.

- [ ] **Step 4: Run the test to verify it passes**

Run: `./mvnw -B -ntp -pl :admin-reference-app -Dtest=AdminThemeTokenTest test`

Expected: token and appearance-profile documentation contract passes.

- [ ] **Step 5: Commit**

```bash
git add docs/en/theme-tokens.md docs/en/appearance-profiles.md \
  docs/zh-CN/appearance-profiles.md docs/en/extension-guide.md \
  docs/zh-CN/extension-guide.md \
  admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/theme/AdminThemeTokenTest.java
git commit -m "docs: explain Flow visual language profiles"
```

### Task 5: Verify Reactor And Production Output

**Files:**
- Modify only files from Tasks 1-4 if verification demonstrates a defect.

- [ ] **Step 1: Run focused unit and architecture tests**

Run: `./mvnw -B -ntp -pl :admin-reference-app -am test`

Expected: all unit, architecture, configuration, CSS, and documentation tests pass; no appearance type has crossed into `admin-flow` or `admin-spring-flow`.

- [ ] **Step 2: Run full reference-application verification**

Run: `./mvnw -B -ntp -pl :admin-reference-app -am verify`

Expected: context, locale, existing browser, new profile browser, and OIDC integration scenarios pass. Apply `superpowers:systematic-debugging` before changing code for any unexpected failure.

- [ ] **Step 3: Run production verification**

Run: `./mvnw -B -ntp -Pproduction verify`

Expected: the production Vaadin bundle and complete reactor pass without React/Vue dependencies.

- [ ] **Step 4: Perform final diff and independent review**

Run:

```bash
git status --short
git log --oneline -5
git diff main...HEAD --check
```

Expected: no whitespace errors and only committed feature changes. Invoke `superpowers:requesting-code-review`, address findings, and rerun every affected verification command. Commit a correction only when a verification failure required one; never create an empty verification commit.
