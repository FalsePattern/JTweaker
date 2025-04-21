plugins {
    idea
    `maven-publish`
    `kotlin-dsl`
}

group = "com.falsepattern"
version = "0.5.0"

kotlin {
    jvmToolchain(21)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(JavaVersion.VERSION_21.majorVersion)
        vendor = JvmVendorSpec.ADOPTIUM
    }
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
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
    implementation("org.apache.bcel:bcel:6.10.0")
}