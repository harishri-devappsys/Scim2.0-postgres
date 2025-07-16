dependencies {

    implementation(project(":database"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("com.auth0:java-jwt:4.4.0")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation("com.unboundid.product.scim2:scim2-sdk-server:2.3.6")

    implementation("org.springframework.boot:spring-boot-starter-validation")



    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))

    testImplementation("org.junit.jupiter:junit-jupiter")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")



    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.testng:testng:7.8.0")

}



tasks.bootJar {

    enabled = false

}



tasks.jar {

    enabled = true

}