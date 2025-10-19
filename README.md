# Weight Tracker App

A simple Android weight tracking application built for CS-360 Mobile Architect & Programming course.

## Features

- User login and registration with SQLite database
- Weight logging with decimal support
- Goal setting with BMI calculation
- Progress tracking with visual circular progress indicator
- Weight history with date formatting
- Notification settings (SMS and push notifications)
- Modern Material Design UI with gradient app bars

## Technical Implementation

- **Database**: SQLite with repository pattern
- **UI**: Material Design components with custom gradients
- **Architecture**: Repository pattern for data management
- **Permissions**: Runtime permission handling for notifications
- **Validation**: Real-time input validation with user feedback

## Key Components

- `HomeActivity`: Main dashboard with progress overview
- `WeightTrackingActivity`: Weight logging and history
- `SettingsActivity`: User preferences and notification settings
- `SetGoalBottomSheetDialog`: Goal setting with BMI calculation
- `CircularProgressView`: Custom progress indicator
- `WeightsRepository`, `UserRepository`, `WeightGoalRepository`: Data management

## References

- [Material Design Icons](https://fonts.google.com/icons) - UI icons
- [Android Developer Documentation](https://developer.android.com/) - Core Android development
- [Material Design Guidelines](https://material.io/design) - UI/UX design principles
- [SQLite Documentation](https://www.sqlite.org/docs.html) - Database implementation
- [Android Permissions Guide](https://developer.android.com/guide/topics/permissions/overview) - Runtime permissions

## Requirements

- Android API 29+ (Android 10+)
- SQLite database support
- SMS and notification permissions (optional)

## Course Context

This project demonstrates fundamental Android development concepts including:

- Activity lifecycle management
- Database operations with SQLite
- Material Design implementation
- User interface design
- Data persistence and validation
