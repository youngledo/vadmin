# Ant Flow Profile Phase B Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give the existing Ant Flow profile a coherent, accessible skin for high-frequency controls and overlays while preserving Vaadin Flow behavior and the parallel Vaadin profile.

**Architecture:** The host-owned `admin-theme` extends the semantic token contract with repeated control and overlay roles, then applies Ant-only rules through Vaadin public CSS custom properties and exported `::part(...)` selectors. Existing Flow page patterns retain ownership of dialogs, validation, and feedback behavior; the customer editor exposes native invalid field state for the theme to render.

**Tech Stack:** Java 25, Vaadin Flow 25.2, Spring Boot 4.1, CSS custom properties, Vaadin exported parts, JUnit 5, AssertJ, Playwright, Testcontainers PostgreSQL, Maven 4.

---

## File Structure

- Modify `admin-reference-app/src/main/frontend/themes/admin-theme/styles.css`: semantic control/overlay tokens and Ant-only component rules.
- Modify `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/theme/AdminThemeTokenTest.java`: token and public-selector contract tests.
- Modify `admin-reference-app/src/main/java/io/github/vaadinadminstarter/app/views/CustomersView.java`: field-level invalid state for required customer fields.
- Modify `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/AntVisualLanguageE2EIT.java`: light-mode component, overlay, validation, and notification evidence.
- Modify `admin-reference-app/src/test/java/io/github/vaadinadminstarter/app/e2e/AntCompactVisualLanguageE2EIT.java`: compact dark-mode geometry evidence.
- Modify `docs/en/theme-tokens.md`, `docs/en/appearance-profiles.md`, and `docs/zh-CN/appearance-profiles.md`: host ownership and Phase B scope.

### Task 1: Define the control and overlay token contract

- [x] Write an `AdminThemeTokenTest` requiring `control-fill`, `control-border`, `control-hover-border`, `control-disabled-fill`, `overlay-surface`, and `overlay-shadow` in documentation and Ant light/dark CSS.
- [x] Run `./mvnw -B -ntp -pl :admin-reference-app -am -Dsurefire.failIfNoSpecifiedTests=false -Dtest=AdminThemeTokenTest test` and observe the contract failure.
- [x] Add only host-owned semantic tokens and Ant-scoped rules for button states; text, password, number, textarea, combo, select, and date fields; menu/select/combo/date overlays; dialogs; notifications; focus, disabled, and invalid states.
- [x] Document the new semantic roles in Chinese and English; prohibit modules from selecting Ant profiles or targeting Vaadin overlay internals.
- [x] Re-run the unit contract and `./mvnw -B -ntp -Pproduction -pl :admin-reference-app -am package -DskipTests`.

### Task 2: Expose actual field validation state

- [x] Add a failing Ant E2E test which submits the customer editor blank, expects both required fields to be `invalid`, then expects state clearing after correction.
- [x] Run `./mvnw -B -ntp -pl :admin-reference-app -am '-Dit.test=AntVisualLanguageE2EIT#antProfileRendersNativeInvalidFieldsInTheCustomerEditor' -Dfailsafe.failIfNoSpecifiedTests=false verify` and observe the missing native field state.
- [x] Make `CustomersView` set required, invalid, and localized field messages without changing the dialog alert, authorization, command, or error-handling semantics.
- [x] Re-run the E2E test and existing `BrowserE2EIT#customerCrudWithAttachment` regression.

### Task 3: Prove controls and overlays through real workflows

- [x] Add Ant browser checks for menu overlay surface, focused field, invalid border, editor dialog geometry, notification surface, disabled action, and compact dark 390px geometry.
- [x] Run `./mvnw -B -ntp -pl :admin-reference-app -am '-Dit.test=AntVisualLanguageE2EIT,AntCompactVisualLanguageE2EIT' -Dfailsafe.failIfNoSpecifiedTests=false verify` and observe failing visual contracts before the complete skin.
- [x] Complete the Ant-only CSS rules using the Task 1 tokens, preserving native Vaadin keyboard, focus-trap, close, validation, disabled, and notification behavior.
- [x] Re-run the two visual suites and update the bilingual appearance guides.

### Task 4: Phase B regression and production verification

- [x] Run `./mvnw -B -ntp -pl :admin-reference-app -am '-Dit.test=BrowserE2EIT,AntVisualLanguageE2EIT,AntCompactVisualLanguageE2EIT' -Dfailsafe.failIfNoSpecifiedTests=false verify`.
- [x] Run `./mvnw -B -ntp verify`, `./mvnw -B -ntp -Pproduction verify`, and `git diff --check`.

## Plan Self-Review

The plan covers the approved Phase B scope without adding a frontend runtime, a synthetic component library, global resets, or profile-specific business code. CSS is limited to host-owned semantic tokens and Vaadin public parts. The customer editor change is a targeted behavior-preserving accessibility improvement, validated through the pre-existing CRUD workflow.
