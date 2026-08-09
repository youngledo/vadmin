package io.github.vaadinadminstarter.app;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "io.github.vaadinadminstarter")
class ArchitectureTest {
    @ArchTest
    static final ArchRule core_does_not_depend_on_spring = noClasses()
            .that().resideInAnyPackage("..contracts..", "..platform..", "..flow..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "org.flywaydb..", "..springjpa..");

    @ArchTest
    static final ArchRule flow_navigation_does_not_depend_on_reference_app = noClasses()
            .that().resideInAnyPackage("..flow.navigation..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "org.flywaydb..", "..app..");
}
