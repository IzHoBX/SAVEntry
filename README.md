# SafeEntry Logger
Application to keep track of the places where I've previously check-in to using SafeEntry. Some of the core features include: 
- Keep track of previous check-ins.
- Checkout without having to scan QR code again.
- Mark location as favorite to allow for easy check-in without scanning QR code.

Pre-built APK is available under [Releases](https://github.com/lamkeewei/SafeEntryLogger/releases). 

## Screenshots 
| Automated Checkin | Checkout Notification | Favorite Locations |
| ----------- | ----------- | --- |
| <img src="screenshots/checkin.gif" width=256 /> | <img src="screenshots/checkout.gif" width=256 /> | <img src="screenshots/favorite.gif" width=256 /> |

## Build and Run Locally
- Clone the repo locally 
```
git clone https://github.com/lamkeewei/SafeEntryLogger.git
```
- Create a Firebase project in the Firebase console. 
- Add a new Android application into your Firebase project with the package name `com.lamkeewei.android.safeentrylogger`.  
- Download and place `google-services.json` in the `app` folder.