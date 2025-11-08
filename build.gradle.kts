
plugins {
    id("java")
}

sourceSets {
    main {
        resources {
            srcDirs("src/main/resources")
        }
    }
}

group = "com.teampro9"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.test {
    useJUnitPlatform()
}