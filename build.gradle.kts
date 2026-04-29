// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    id("org.jetbrains.kotlinx.kover") version "0.9.8" apply false
    id("org.sonarqube") version "7.2.3.7755"
}

sonar {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "ramprasad441")
        property("sonar.projectKey", "ramprasad441_Countries")
        property("sonar.projectName", "Countries")
        property(
            "sonar.exclusions",
            "**/test/res/**," +
                    "app/src/test/res/**," +
                    "app/src/androidTest/res/**," +
                    "**/R.java," +
                    "**/R$*.java," +
                    "**/BuildConfig.java," +
                    "**/*Manifest*"
        )
        property ("sonar.sourceEncoding", "UTF-8")

        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "app/build/reports/kover/report.xml"
        )
        // For PR analysis - prevents looking for missing branches
        if (System.getenv("GITHUB_EVENT_NAME") == "pull_request") {
            property("sonar.scm.forceReloadBranchConfiguration", "true")
        }
    }
}

tasks.named("sonar") {
    dependsOn(":app:koverXmlReport")
}

