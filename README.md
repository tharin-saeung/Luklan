    This is a Kotlin Multiplatform project targeting Android, iOS.
 
* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

## Temporary change: OCR flow disabled 

- OCR parsing has been found unstable and this project temporarily disables the OCR flow in the app navigation.
- The Home screen Add button now navigates directly to the manual `AddMedicine` screen.
- OCR source files were kept in repo for future re-enablement:
  - `composeApp/src/commonMain/kotlin/com/commu/luklan/ui/ocr/OcrParser.kt`
  - `composeApp/src/commonMain/kotlin/com/commu/luklan/ui/ocr/OcrScanScreen.kt`
  - `composeApp/src/commonMain/kotlin/com/commu/luklan/ui/ocr/AddMethodScreen.kt`
  - `composeApp/src/commonMain/kotlin/com/commu/luklan/ui/ocr/OcrResultStore.kt`

To re-enable OCR navigation later, restore the removed `composable` blocks in `App.kt` for `AddMethod` and `OcrScan` and change the Home add navigation back to `Screen.AddMethod`.
