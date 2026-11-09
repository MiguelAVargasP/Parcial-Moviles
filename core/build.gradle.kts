import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "edu.upb.arcade.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        aidl = true // IArcadeService.aidl
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    // "api" para que cada juego reciba Compose y lifecycle sin repetirlos
    api(libs.androidx.core.ktx)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.runtime.compose)
    api(libs.androidx.activity.compose)
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.ui)
    api(libs.androidx.ui.graphics)
    api(libs.androidx.ui.tooling.preview)
    api(libs.androidx.material3)
}

// ---------------------------------------------------------------------------
// Fix "illegal unicode escape" en Windows:
// aidl.exe escribe en el comentario del .java generado la ruta del .aidl
// (...\edu\upb\arcade\...). Java lee "\u" como inicio de escape unicode incluso
// dentro de comentarios, y "\upb" no es válido. Se escapa cada "\u" que no esté ya
// escapado, justo después de que corre AIDL y antes de compilar el Java.
// ---------------------------------------------------------------------------
tasks.matching { it.name.startsWith("compile") && it.name.endsWith("Aidl") }
    .configureEach {
        doLast {
            val escapeSuelto = Regex("""(?<!\\)\\u""")
            outputs.files.files.forEach { raiz ->
                raiz.walkTopDown()
                    .filter { it.isFile && it.extension == "java" }
                    .forEach { archivo ->
                        val texto = archivo.readText()
                        if (escapeSuelto.containsMatchIn(texto)) {
                            archivo.writeText(escapeSuelto.replace(texto) { "\\\\u" })
                        }
                    }
            }
        }
    }
