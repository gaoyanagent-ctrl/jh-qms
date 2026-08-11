package com.company.iaf.platform.core.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ModuleBoundaryRulesTest {

    private static final Path BACKEND_ROOT = Path.of("..").normalize();

    @Test
    void platformModulesDoNotDependOnManufacturingOrWmsModules() throws IOException {
        List<Path> offenders = javaFiles("iaf-platform-")
                .stream()
                .filter(path -> containsAny(path, "com.company.iaf.manufacturing.", "com.company.iaf.wms."))
                .toList();

        assertThat(offenders).isEmpty();
    }

    @Test
    void manufacturingModulesDoNotDependOnWmsModules() throws IOException {
        List<Path> offenders = javaFiles("iaf-manufacturing-")
                .stream()
                .filter(path -> containsAny(path, "com.company.iaf.wms."))
                .toList();

        assertThat(offenders).isEmpty();
    }

    @Test
    void modulesDoNotImportAnotherModuleInfrastructurePackage() throws IOException {
        List<Path> offenders = allModuleJavaFiles()
                .stream()
                .filter(path -> {
                    String module = moduleName(path);
                    String source = read(path);
                    return source.lines()
                            .filter(line -> line.startsWith("import com.company.iaf."))
                            .filter(line -> line.contains(".infrastructure."))
                            .anyMatch(line -> !line.contains(modulePackage(module) + "."));
                })
                .toList();

        assertThat(offenders).isEmpty();
    }

    @Test
    void controllersDoNotInjectRepositoriesOrJdbcTemplate() throws IOException {
        List<Path> offenders = allModuleJavaFiles()
                .stream()
                .filter(path -> path.toString().contains("/interfaces/controller/"))
                .filter(path -> containsAny(path, "Repository ", "Repository;", "JdbcTemplate "))
                .toList();

        assertThat(offenders).isEmpty();
    }

    @Test
    void applicationServicesDoNotDependOnPersistenceImplementationDetails() throws IOException {
        List<Path> offenders = allModuleJavaFiles()
                .stream()
                .filter(path -> path.toString().contains("/application/"))
                .filter(path -> containsAny(
                        path,
                        "org.springframework.jdbc.",
                        "JdbcTemplate",
                        "org.apache.ibatis.",
                        "com.baomidou.mybatisplus.",
                        ".infrastructure.mapper.",
                        "Mapper "
                ))
                .toList();

        assertThat(offenders).isEmpty();
    }

    private static List<Path> javaFiles(String modulePrefix) throws IOException {
        try (var stream = Files.list(BACKEND_ROOT)) {
            return stream
                    .filter(path -> path.getFileName().toString().startsWith(modulePrefix))
                    .flatMap(ModuleBoundaryRulesTest::walkJavaFiles)
                    .toList();
        }
    }

    private static List<Path> allModuleJavaFiles() throws IOException {
        try (var stream = Files.list(BACKEND_ROOT)) {
            return stream
                    .filter(path -> path.getFileName().toString().startsWith("iaf-"))
                    .filter(path -> !path.getFileName().toString().equals("iaf-app"))
                    .flatMap(ModuleBoundaryRulesTest::walkJavaFiles)
                    .toList();
        }
    }

    private static java.util.stream.Stream<Path> walkJavaFiles(Path module) {
        try {
            return Files.walk(module.resolve("src/main/java"))
                    .filter(path -> path.toString().endsWith(".java"));
        } catch (IOException exception) {
            return java.util.stream.Stream.empty();
        }
    }

    private static boolean containsAny(Path path, String... fragments) {
        String source = read(path);
        for (String fragment : fragments) {
            if (source.contains(fragment)) {
                return true;
            }
        }
        return false;
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static String moduleName(Path path) {
        return BACKEND_ROOT.relativize(path).getName(0).toString();
    }

    private static String modulePackage(String moduleName) {
        return moduleName
                .replace("iaf-", "")
                .replace("-", ".");
    }
}
