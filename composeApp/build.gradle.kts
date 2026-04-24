import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.googleServices)
}

kotlin {
    tasks.register("testClasses")
    applyDefaultHierarchyTemplate()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.compilations.getByName("main") {
            val FirestoreBridge by cinterops.creating {
                defFile(project.file("src/nativeInterop/cinterop/FirestoreBridge.def"))
                packageName("platform.FirestoreBridge")
                includeDirs(project.file("../iosApp/iosApp"))
            }
        }
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(project.dependencies.platform(libs.android.firebase.bom))
            implementation(libs.android.firebase.auth)
            implementation(libs.android.firebase.firestore)
            implementation(libs.android.firebase.messaging)
            implementation(libs.google.firebase.functions)
            implementation(libs.gson)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.1")
            implementation(libs.kotlinx.datetime)
            implementation(libs.qr.kit)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }

    cocoapods {
        version = "1.0"
        ios.deploymentTarget = "16.0"
        summary = "Some description for a Kotlin/Native module"
        homepage = "Link to a Kotlin/Native module homepage"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "composeApp"
            isStatic = true
        }

        pod("FirebaseCore") {
            version = "~> 11.0"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
        pod("FirebaseAuth") {
            version = "~> 11.0"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }

        pod("FirebaseFirestore") {
            version = "~> 11.0"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
        
        pod("FirebaseFunctions") {
            version = "~> 11.0"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }

        pod("FirebaseMessaging") {
            version = "~> 11.0"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }

        xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] = NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] = NativeBuildType.RELEASE
    }
}

compose {
    resources {
        publicResClass = true
        packageOfResClass = "luklan.composeapp.generated.resources"
    }
}

android {
    namespace = "com.commu.luklan"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.commu.luklan"
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
        getByName("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-DEBUG"
        }
        getByName("release") {
            isMinifyEnabled = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    lint {
        disable += "InvalidFragmentVersionForActivityResult"
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}