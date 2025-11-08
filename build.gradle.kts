
plugins {
    id("java")
}

//sourceSets {
//    main {
//        resources {
//            srcDirs("src/main/resources")
//            exclude("game_data/**")
//        }
//    }
//}

group = "com.teampro9"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

val junitVersion = "5.10.2"

dependencies {
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// 이 블록이 Gradle이 JUnit 5를 사용하도록 지시합니다.
tasks.test {
    useJUnitPlatform()
}

tasks.test {
    useJUnitPlatform()
}