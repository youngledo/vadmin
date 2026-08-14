package io.github.vaadinadminstarter.app;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
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

    @ArchTest
    static final ArchRule reference_app_does_not_own_starter_shell_or_system_administration = noClasses()
            .that().resideInAnyPackage("io.github.vaadinadminstarter.app..")
            .should().haveSimpleNameStartingWith("Default")
            .orShould().haveSimpleNameEndingWith("View")
            .orShould().haveSimpleNameContaining("Administration")
            .orShould().haveSimpleNameContaining("Appearance")
            .orShould().haveSimpleNameContaining("Theme")
            .orShould().haveSimpleNameContaining("ModuleConfiguration");

    @ArchTest
    static final ArchRule flow_module_does_not_depend_on_spring = noClasses()
            .that().resideInAnyPackage("..flow..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..");

    @ArchTest
    static final ArchRule only_the_starter_and_flow_adapter_depend_on_springflow = noClasses()
            .that().resideOutsideOfPackages("..app..", "..springflow..", "..starter..")
            .should().dependOnClassesThat().resideInAnyPackage("..springflow..");

    @ArchTest
    static final ArchRule configured_external_identity_mapper_uses_only_contracts_and_its_configuration = classes()
            .that().haveSimpleName("ConfiguredExternalIdentityMapper")
            .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "..contracts..", "..app.auth..");
}
