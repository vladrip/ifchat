import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.googleServices)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            //compose
            implementation(libs.compose.paging)
            implementation(libs.jetbrans.navigation.compose)

            //android, google
            implementation(libs.androidx.paging.runtime.ktx)
            implementation(libs.androidx.material3.android)
            implementation(libs.androidx.ui.tooling.preview.android)
            implementation(libs.gson)
            implementation(libs.accompanist.pager)

            //firebase
            implementation(project.dependencies.platform(libs.google.firebase.bom))
            implementation(libs.google.firebase.messaging.ktx)
            implementation(libs.google.firebase.auth.ktx)
            implementation(libs.firebaseui.firebase.ui.auth)

            //database
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.room.ktx)
            implementation(libs.androidx.room.paging)

            //api
            implementation(libs.retrofit)
            implementation(libs.converter.gson)
            implementation(libs.logging.interceptor)
            implementation(libs.networkresponseadapter)

            //dependency injection
            implementation(libs.koin.android)
            implementation(libs.koin.viewmodel)
            implementation(libs.koin.viewmodel.navigation)
        }

        commonMain.dependencies {
            implementation(projects.shared)

            //jetpack compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)

            //dependency injection
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            api(libs.koin.annotations)

            //adapting ui units
            implementation(libs.sdp.android)
            implementation(libs.ssp.android)
        }
    }

    sourceSets.named("commonMain").configure {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }
}

dependencies {
    add("kspAndroid", libs.koin.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
}

android {
    namespace = "com.vladrip.ifchat"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.vladrip.ifchat"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
    arg("KOIN_USE_COMPOSE_VIEWMODEL","true")
}
