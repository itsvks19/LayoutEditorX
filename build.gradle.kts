// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.compose) apply false
}

android {
    lintOptions {
        // Enable lint auto-fix
        checkReleaseBuilds true
        // Optional: Disable lint for certain tasks if needed
        // disable 'LintWarning'
    }
}

