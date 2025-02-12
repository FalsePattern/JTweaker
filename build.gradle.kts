plugins {
    idea
    `maven-publish`
    `kotlin-dsl`
}

group = "com.falsepattern"
version = "0.4.0"

kotlin {
    jvmToolchain(8)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(JavaVersion.VERSION_1_8.majorVersion)
        vendor = JvmVendorSpec.ADOPTIUM
    }
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:all")
}

gradlePlugin {
    isAutomatedPublishing = false
    plugins.create("jtweaker") {
        id = "jtweaker"
        group = project.group
        version = project.version
        implementationClass = "com.falsepattern.jtweaker.JTweakerPlugin"
    }
}

publishing {
    publications {
        create<MavenPublication>("pluginPublication") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }
    }
    repositories {
        maven {
            name = "mavenpattern"
            setUrl("https://mvn.falsepattern.com/releases/")

            val user = System.getenv("MAVEN_DEPLOY_USER")
            val pass = System.getenv("MAVEN_DEPLOY_PASSWORD")
            if (user != null && pass != null) {
                credentials {
                    username = user
                    password = pass
                }
            } else {
                credentials(PasswordCredentials::class)
            }
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("org.apache.bcel:bcel:6.9.0")
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
tasks.named<Test>("test") {
    useJUnitPlatform()

    maxHeapSize = "1G"

    testLogging {
        events("passed")
    }
}