pluginManagement {
    repositories {
        google()  // שינוי זה - הסרת המגבלות
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "StyleTimeAndroidApp"
include(":app")