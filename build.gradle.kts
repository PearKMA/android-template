import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.android.application") apply false
    id("com.android.library") apply false
    kotlin("android") apply false
    alias(libs.plugins.versions)
//    alias(libs.plugins.safeargs) apply false
    alias(libs.plugins.hilt).apply(false)
//    alias(libs.plugins.google.services) apply false
//    alias(libs.plugins.crashlytics) apply false
    cleanup
    base
}

//allprojects {
//    group = PUBLISHING_GROUP
//}

tasks {
    withType<DependencyUpdatesTask>().configureEach {
        rejectVersionIf {
            candidate.version.isStableVersion().not()
        }
    }
//    withType<JavaCompile> {
//        options.compilerArgs.plusAssign("-Xlint:deprecation")
//    }

//    register("clean", Delete::class){
//        delete(rootProject.buildDir)
//    }
}