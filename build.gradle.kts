plugins {
    id("java")
    id("jacoco")
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

val junitVersion = "5.10.2"

dependencies {
    // 테스트를 위한 JUnit 5
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    // Mockito (모의 객체) 라이브러리
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    // Mockito가 Core의 static 메소드를 가로챌 수 있게 함
    testImplementation("org.mockito:mockito-inline:5.2.0")
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

jacoco {
    toolVersion = "0.8.10"
}
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
    }
}