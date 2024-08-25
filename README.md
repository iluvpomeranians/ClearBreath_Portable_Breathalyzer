# COEN390 Android Project: Breathalyzer App

## Overview

This project is an Android application that functions as a breathalyzer app, helping users monitor their Blood Alcohol Concentration (BAC) levels over time. The app records BAC data and provides real-time visualization through charts. Users can track their BAC history with different time intervals, including 15-second, minutely, and hourly views.

## Features

- **Real-time BAC Visualization**: View your BAC levels in real-time with a dynamic line chart.
- **BAC History Tracking**: Switch between different time intervals (15 seconds, minutely, hourly) to see your BAC history.
- **Persistent Data**: The app stores BAC data across sessions, ensuring that users can view their previous session data when they reopen the app.
- **Responsive UI**: The app provides a smooth, responsive UI that automatically updates the chart as new BAC data is recorded.
- **Customizable Settings**: Adjust settings for different BAC tracking intervals through the provided UI options.

## Installation

1. **Clone the repository**:
    ```bash
    git clone https://github.com/yourusername/breathalyzer-app.git
    ```
2. **Open the project in Android Studio**.
3. **Build and Run** the app on an Android device or emulator.

## Usage

1. Launch the app.
2. BAC levels will be automatically recorded and displayed on the line chart.
3. Use the interval buttons to switch between different BAC history views: 15 seconds, minutely, or hourly.
4. The chart updates every few seconds, showing the latest BAC readings.
5. The app stores data locally, so your BAC history will persist between sessions.

## Project Structure

- **`MainActivity.java`**: The entry point of the application.
- **`AccountHistoryActivity.java`**: Handles the chart visualization and BAC history tracking.
- **`DBHelper.java`**: Manages the SQLite database for storing BAC records.
- **`SettingsUtils.java`**: Provides utility functions for applying settings throughout the app.
- **`Utils.java`**: Contains helper functions, such as converting JSON strings to lists and vice versa.
- **`layout` folder**: Contains the XML layout files for the app’s UI.

## Technical Details

- **Charting Library**: The app uses [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) for rendering dynamic charts.
- **Database**: BAC data is stored using SQLite, managed by the `DBHelper` class.
- **SharedPreferences**: Persistent user data, such as the BAC history list, is stored using Android's `SharedPreferences`.

## How It Works

### BAC Data Handling
- BAC data is recorded in the app at regular intervals and saved to a local SQLite database.
- The data is periodically fetched, and the average BAC is calculated and displayed on a line chart.
- The app uses a `Handler` to periodically update the UI with the latest BAC data.

### Chart Visualization
- The app displays the BAC history using a line chart from the MPAndroidChart library.
- Users can switch between different time intervals to view their BAC history over different periods (15-second intervals, minute intervals, hourly intervals).

## Future Enhancements

- **Cloud Syncing**: Enable cloud synchronization of BAC data to allow users to access their history across devices.
- **Improved UI**: Refine the UI to enhance the user experience and add more customization options for chart visualization.
- **Notification System**: Implement notifications to alert users when their BAC reaches certain levels.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

For any inquiries or support, please contact us at `your-email@example.com`.

