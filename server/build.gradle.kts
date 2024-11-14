plugins {
    java
    alias(libs.plugins.springframework.boot)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

springBoot  {
    buildInfo()
}

dependencies {
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.openapi.starter.webmvc.ui)
    implementation(libs.firebase.admin)
    implementation(libs.lombok)
    implementation(libs.mapstruct)
    runtimeOnly(libs.postgresql)
    annotationProcessor(libs.mapstruct.processor)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.lombok.mapstruct.binding)
}
