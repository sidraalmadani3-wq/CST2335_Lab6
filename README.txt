CST2335 Lab 6 - Cat TV

Open this folder in Android Studio and allow Gradle sync to finish.

MainActivity downloads cat image metadata from https://cataas.com/cat?json=true,
checks the app's private files directory for an image with the same ID, downloads
and saves the image only when it is not already stored, and displays it on the TV.
The progress bar shows the time remaining before the next image.

To test:
1. Create an Android TV virtual device in Device Manager.
2. Run the app on the Android TV device.
3. Allow internet access if prompted and wait for the first picture.
4. Take 3-5 screenshots for the Word report required by the assignment.
5. Create a Git branch named lab_6 and add your GitHub repository link to the Brightspace submission comment.

Note: AsyncTask is deprecated in newer Android versions but is used because it is explicitly required by this lab.
