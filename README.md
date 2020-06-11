# SaveEntry
This repo is forked from https://github.com/lamkeewei/SafeEntryLogger. It already have some great value proposition that I am looking for:  
- Mark location as favorite to allow for easy check-in without scanning QR code.
- Checkout without having to scan QR code again.

This repo extends the app to provide check-in functionality without having to unlock phone or return to homepage to find for this app.  


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

## Acknowledgments
- QR Code Scanner adapted from [Material ML Kit](https://github.com/firebase/mlkit-material-android).
- Illustrations from [Undraw](https://undraw.co/).
- QR Code Icon made by [surang](https://www.flaticon.com/authors/surang) from [www.flaticon.com](www.flaticon.com)
