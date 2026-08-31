// Gradle port of build.xml — same jar artifacts in build/ (jpf-symbc.jar, jpf-symbc-classes.jar,
// jpf-symbc-annotations.jar) as `ant build`. Ant build kept as reference.
//
// jpf-core location resolution (same precedence as ant: -Djpf-core / site.properties / peer dir):
//   1. -Pjpf-core=/path/to/jpf-core
//   2. JPF_CORE_DIR env var
//   3. ../jpf-core peer checkout
plugins { java }

java {
    toolchain { languageVersion = JavaLanguageVersion.of(8) }
}

val jpfCoreDir = providers.gradleProperty("jpf-core")
    .orElse(providers.environmentVariable("JPF_CORE_DIR"))
    .map(::File)
    .orElse(project.file("../jpf-core"))
    .get()

if (!File(jpfCoreDir, "build/jpf.jar").isFile) {
    throw GradleException(
        "jpf-core not built at $jpfCoreDir (missing build/jpf.jar). " +
            "Build jpf-core first (./gradlew build), or point at it via -Pjpf-core= or JPF_CORE_DIR."
    )
}

// ant source layout: src/{annotations,main,peers,classes,tests,examples}
sourceSets {
    create("annotations") { java.srcDir("src/annotations") }
    main { java.srcDirs("src/main") }
    create("peers") { java.srcDir("src/peers") }
    create("model") { java.srcDir("src/classes") }
    create("tests") { java.srcDir("src/tests") }
    create("examples") { java.srcDir("src/examples") }
}

// ant lib.path: build/main + lib/*.jar + jpf-core native_classpath (jpf.jar + jpf-annotations.jar)
val symbcClasspath = files(
    File(jpfCoreDir, "build/jpf.jar"),
    File(jpfCoreDir, "build/jpf-annotations.jar"),
    fileTree("lib").matching { include("*.jar") },
)

dependencies {
    "implementation"(symbcClasspath)
    "peersImplementation"(sourceSets.main.get().output)
    "peersImplementation"(symbcClasspath)
    "modelImplementation"(sourceSets.main.get().output)
    "modelImplementation"(symbcClasspath)
    "modelImplementation"(sourceSets["annotations"].output)
    "testsImplementation"(sourceSets.main.get().output)
    "testsImplementation"(symbcClasspath)
    "testsImplementation"(sourceSets["model"].output)
    "testsImplementation"(sourceSets["annotations"].output)
    "examplesImplementation"(sourceSets.main.get().output)
    "examplesImplementation"(symbcClasspath)
    "examplesImplementation"(sourceSets["model"].output)
    "examplesImplementation"(sourceSets["annotations"].output)
}

val jpfSymbcJar = tasks.register<Jar>("jpfSymbcJar") {
    archiveFileName = "jpf-symbc.jar"
    destinationDirectory = layout.buildDirectory
    from(sourceSets.main.get().output)
    from(sourceSets["peers"].output)
}

val jpfSymbcClassesJar = tasks.register<Jar>("jpfSymbcClassesJar") {
    archiveFileName = "jpf-symbc-classes.jar"
    destinationDirectory = layout.buildDirectory
    from(sourceSets["model"].output)
    from(sourceSets["annotations"].output)
}

val jpfSymbcAnnotationsJar = tasks.register<Jar>("jpfSymbcAnnotationsJar") {
    archiveFileName = "jpf-symbc-annotations.jar"
    destinationDirectory = layout.buildDirectory
    from(sourceSets["annotations"].output)
}

// ./gradlew build → same artifacts as `ant build`
tasks.jar { enabled = false }
tasks.assemble {
    dependsOn(jpfSymbcJar, jpfSymbcClassesJar, jpfSymbcAnnotationsJar)
    // ant `build` compiles these too
    dependsOn(tasks.named("testsClasses"), tasks.named("examplesClasses"))
}
