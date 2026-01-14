import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec
import javax.inject.Inject

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)
}

abstract class GitCommitValueSource: ValueSource<String, ValueSourceParameters.None> {

    @Inject
    abstract fun getExecOperations(): ExecOperations

    override fun obtain(): String {
        val output = ByteArrayOutputStream()
        val action = object: Action<ExecSpec> {
            override fun execute(t: ExecSpec) {
                t.commandLine("git", "rev-parse", "--verify", "--short", "HEAD")
                t.standardOutput = output
            }
        }
        getExecOperations().exec(action)
        return String(output.toByteArray(), Charset.defaultCharset()).trim()
    }
}

val gitCommitProvider = providers.of(GitCommitValueSource::class) {}
val gitCommit = gitCommitProvider.get()

android {
    namespace = "de.brittytino.android.launcher"
    compileSdk = 36

    defaultConfig {
        applicationId = "de.brittytino.android.launcher"
        minSdk = 23
        targetSdk = 36
        versionCode = 53
        versionName = "0.2.8"

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
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }


    defaultConfig {
        buildConfigField("String", "GIT_COMMIT", "\"${gitCommit}\"")
        buildConfigField("boolean", "USE_ACCESSIBILITY_SERVICE", "true")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
        compose = true
        dataBinding = true
        viewBinding = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    packaging {
        resources.excludes.addAll(
            listOf(
                "META-INF/LICENSE.md",
                "META-INF/NOTICE.md",
                "META-INF/LICENSE-notice.md"
            )
        )
    }

    lint {
        abortOnError = false
    }

    sourceSets {
        val main = getByName("main")
        main.java.srcDirs("build/generated/source/kapt/main", "build/generated/source/kaptKotlin/main")
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.coil.compose)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.google.material)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.jonahbauer.android.preference.annotations)
    kapt(libs.jonahbauer.android.preference.annotations)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
}
