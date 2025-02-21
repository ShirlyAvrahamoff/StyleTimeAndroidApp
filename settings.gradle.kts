pluginManagement {
    repositories {
        google()  // שינוי זה - הסרת המגבלות
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "StyleTimeAndroidApp"
include(":app")