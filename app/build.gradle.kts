plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    id("jacoco")
}

android {
    namespace = "com.ramprasad.countries"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ramprasad.countries"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-Xjvm-default=all"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    testOptions {
        unitTests.all {
            it.extensions.configure(JacocoTaskExtension::class) {
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java")
            res.srcDirs("src/main/res")
        }
        getByName("test") {
            java.srcDirs("src/test/java")
            res.srcDirs("src/test/res") // Ensure this line is present for unit tests
        }
        getByName("androidTest") {
            java.srcDirs("src/androidTest/java")
            res.srcDirs("src/androidTest/res") // For instrumented tests
        }

    }

    packaging {
        resources {
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }



    tasks.register<JacocoReport>("testDebugUnitTestCoverage") {
        group = "coverage" // or "coverage", "reporting", etc.
        description = "Generates code coverage report for the Debug unit tests using JaCoCo."
        dependsOn("testDebugUnitTest") // Make sure this runs the tests first

        reports {
            xml.required.set(true)
            html.required.set(true)
        }

        val fileFilter = listOf(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "**/model/**"
        )

        val debugTree = fileTree(layout.buildDirectory.dir("intermediates/javac/debug/classes")) {
            exclude(fileFilter)
        }

        val kotlinDebugTree = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
            exclude(fileFilter)
        }

        classDirectories.setFrom(debugTree, kotlinDebugTree)
        sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
        executionData.setFrom(
            fileTree(layout.buildDirectory.get().asFile).include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "jacoco/testDebugUnitTest.exec"
            )
        )

        doLast {
            val reportFile = reports.html.outputLocation.get().asFile.resolve("index.html")
            val fileUri = "file://${reportFile.absolutePath}"
            println("Test coverage report available here:\n$fileUri")
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    // --- Implementation dependencies ---
    // AndroidX
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.test.core.ktx)
    implementation(libs.androidx.junit.ktx)

    // Google
    implementation(libs.google.material)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // OkHttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Kotlinx
    implementation(libs.kotlinx.coroutines.core)


    // --- Test Implementation dependencies ---
    // JUnit & Truth
    testImplementation(libs.junit)
    testImplementation(libs.truth)

    // Espresso
    testImplementation(libs.androidx.espresso.core)

    // AndroidX
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.androidxTestCore)

    // MockK
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.agent)

    // Kotlin
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlintest)

    // Robolectric
    testImplementation(libs.robolectric)


    // --- Android Test Implementation dependencies ---
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.mockk.android)


    // --- Debug Implementation dependencies ---
    debugImplementation(libs.fragment.testing)
}
