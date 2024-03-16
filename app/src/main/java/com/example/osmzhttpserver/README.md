1. Add permissions to Android Manifest
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"/>

2. Change minsdk to 30 in build.gradle file

Possible values for sensor type in android manifest are:
accelerometer, barometer, compass, gyroscope, light, proximity