# Glaucoma AI Android App

## Description
Glaucoma AI is an Android application that leverages machine learning to assist in glaucoma detection from eye images. Users can capture or select images of their eyes, input demographic information, and receive instant analysis powered by a TensorFlow Lite model.

## Technologies Used
- **Kotlin** & **Java** (Android development)
- **Android Jetpack** (ViewModel, LiveData, Activity Result APIs)
- **TensorFlow Lite** (on-device ML inference)
- **Dagger Hilt** (dependency injection)
- **Gradle** (build system)
- **Material Components** (UI/UX)
- **Custom Bottom Sheets** & **RecyclerView Adapters**

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
