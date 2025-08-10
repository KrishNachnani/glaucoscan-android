![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)
![Stars](https://img.shields.io/github/stars/KrishNachnani/glaucoscan-android?style=social)
![Downloads](https://img.shields.io/github/downloads/KrishNachnani/glaucoscan-android/total)

# Glaucoscan AI Android App

## Description
Glaucoma AI is an Android application that leverages machine learning to assist in glaucoma detection from eye images. Users can capture or select images of their eyes, input demographic information, and receive instant analysis powered by a TensorFlow Lite model.

## Technologies Used
- **Kotlin** & **Java**  (Android development)
- **Android Jetpack** (Data Binding, View Binding, ConstraintLayout, CameraX)
- **Material Components** (UI/UX)
- **Hilt** for dependency injection
- **TensorFlow Lite** for on-device machine learning
- **ML Kit – Face Detection**
- **Retrofit** & **OkHttp** for networking
- **Glide** for image loading
- **Gradle** (build system)


## Installation

1. **Clone the repository:**
   ```sh
   git clone https://github.com/KrishNachnani/glaucoscan-android.git
   ```

2. **Open in Android Studio:**
   - Launch Android Studio.
   - Select **Open an existing project** and choose the cloned folder.

3. **Configure dependencies:**
   - Ensure you have the latest Android SDK and build tools installed.
   - Gradle will sync and download dependencies automatically.

4. **Add TensorFlow Lite model:**
   - Place your `.tflite` model file (e.g., `glaucoma_model.tflite`) in the `app/src/main/assets` directory.

5. **Run the app:**
   - Connect your Android device or start an emulator.
   - Click **Run** in Android Studio.

## Usage
1. Launch the app.
2. Enter your age, gender, and ethnicity (optional).
3. Capture or select an eye image (right or left).
4. Submit the form to analyze for glaucoma.
5. View results and confidence score.

## License
This project is for educational and research purposes. See the repository for license details.
