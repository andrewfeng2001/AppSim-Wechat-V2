pluginManagement {
    repositories {
//        gradlePluginPortal()
//        mavenCentral()
        maven { url = uri("https://jf.guasemi.com/artifactory/maven-center/") }
        maven { url = uri("https://jf.guasemi.com/artifactory/maven-local/") }
        maven { url = uri("https://jf.guasemi.com/artifactory/maven-aliyun/") }
        maven { url = uri("https://jf.guasemi.com/artifactory/maven-google/") }
        maven { url = uri("https://jf.guasemi.com/artifactory/maven-gradle/") }
        maven { url = uri("https://jf.guasemi.com/artifactory/maven-jitpack/") }
        maven { url = uri("https://maven.aliyun.com/repository/google") } // 关键
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        google()
//        mavenCentral()
        maven { url = uri("https://jf.guasemi.com/artifactory/maven-center/") }
        maven { url = uri("https://jf.guasemi.com/artifactory/maven-local/") }
        maven { url = uri("https://jf.guasemi.com/artifactory/maven-aliyun/") }
        maven { url = uri("https://jf.guasemi.com/artifactory/maven-google/") }
        maven { url = uri("https://jf.guasemi.com/artifactory/maven-gradle/") }
        maven { url = uri("https://jf.guasemi.com/artifactory/maven-jitpack/") }

    }
}

rootProject.name = "FakeWechat"
include(":app")
