package io.github.vaadinadminstarter.app;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = {"io.github.vaadinadminstarter", "com.example.orders"})
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

    @ArchTest
    static final ArchRule orders_example_does_not_depend_on_reference_app = noClasses()
            .that().resideInAnyPackage("com.example.orders..")
            .should().dependOnClassesThat().resideInAnyPackage("..app..");

    @ArchTest
    static final ArchRule flow_module_does_not_depend_on_spring = noClasses()
            .that().resideInAnyPackage("..flow..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..");

    @ArchTest
    static final ArchRule only_the_host_and_flow_adapter_depend_on_springflow = noClasses()
            .that().resideOutsideOfPackages("..app..", "..springflow..")
            .should().dependOnClassesThat().resideInAnyPackage("..springflow..");
}
