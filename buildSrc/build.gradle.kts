plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    jcenter()
}

dependencies {
    /* Depend on the android gradle plugin, since we want to access it in our plugin */
    implementation("com.android.tools.build:gradle:7.1.3")

    /* Depend on the kotlin plugin, since we want to access it in our plugin */
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")

    /* Depend on the default Gradle API's since we want to build a custom plugin */
    implementation(gradleApi())
    implementation(localGroovy())
}

gradlePlugin {
    plugins {
        create("bundleCheck") {
            id = "com.frolo.plugin.bundle_check"
            implementationClass = "com.frolo.plugin.BundleCheckPlugin"
        }

        create("measureBuild") {
            id = "com.frolo.plugin.measure_build"
            implementationClass = "com.frolo.plugin.MeasureBuildPlugin"
        }
    }
}