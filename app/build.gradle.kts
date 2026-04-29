plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    kotlin("kapt")

    id("org.jetbrains.kotlinx.kover")
    id("org.sonarqube")
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
}

android {
    namespace = "com.ramprasad.countries"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.ramprasad.countries"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
            freeCompilerArgs.addAll("-Xjvm-default=all")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    packaging {
        resources {
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.databinding.*",
                    "*R",
                    "*R$*",
                    "*BuildConfig*",
                    "*Manifest*",
                    "*Companion*",
                    "*DefaultImpls*",
                )
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    if (name.contains("Release")) {
        enabled = false
    }
}

tasks.register("coverageCheck") {
    group = "verification"
    description = "Fails build if coverage is below 80%"

    dependsOn("testDebugUnitTest", "koverXmlReport", "koverHtmlReport")

    doLast {
        val xml = file("build/reports/kover/report.xml")
        val html = file("build/reports/kover/html/index.html")

        val text = xml.readText()

        val match =
            Regex("""<counter type="LINE" missed="(\d+)" covered="(\d+)"""")
                .find(text)
                ?: throw GradleException("Cannot parse coverage")

        val missed = match.groupValues[1].toInt()
        val covered = match.groupValues[2].toInt()

        val total = missed + covered
        val coverage = if (total > 0) covered * 100.0 / total else 0.0

        println("📊 Coverage: %.2f%%".format(coverage))

        if (html.exists()) {
            println("📄 HTML: file://${html.absolutePath}")
        }

        if (coverage < 80) {
            throw GradleException("Coverage below 80%")
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {

    // --- Implementation ---
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.google.material)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.kotlinx.coroutines.core)

    // --- Unit Test ---
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.espresso.core)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.androidxTestCore)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.agent)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlintest)
    testImplementation(libs.robolectric)

    // --- Android Test ---
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.mockk.android)

    // --- Debug ---
    debugImplementation(libs.fragment.testing)
}
