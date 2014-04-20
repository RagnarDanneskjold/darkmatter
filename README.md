DarkMatter monitors the operational environment, indicative of hostile
access to the device (or absence of friendly access to the device) and
sets the device to a more hardened state by killing sensitive applications,
wiping RAM, unmounting TrueCrypt volume, etc. In addition, the built-in
security administrator disables the camera and keyguard widgets.

Warning! When building this app, make sure you build a release version.
Building a debug version will cause libsuperuser to log passwords in 
clear text.

### Setting up DarkMatter in Eclipse
1. Download and install ADT Bundle (http://developer.android.com/sdk/installing/bundle.html) or install Eclipse/ADT separately
2. Clone DarkMatter to some place (not in your workspace folder)
3. In Eclipse choose File-menu > Import > Android > Existing Android Code Into Workspace
4. Browse to the source code folder
5. Check the box and click Finish
6. Make step 2-5 for libsuperuser (https://github.com/Chainfire/libsuperuser)

### Run the project in debug mode
1. Right click the project in the Package Explorer and select Run As > Android Application
The app will be built and signed with your default debug keys. Warning!
In debug mode libsuperuser will currently log the TrueCrypt passwords and
other information in clear text.

### Build the release version
1. In the project menu, uncheck "Build automatically"
2. In the project menu, select "Clean..."
3. Check libsuperuser and darkmatter and click Ok
4. Right click darkmatter in the Package Explorer and select Export > Android > Export Android Application
5. Click Next
6. Create a new key store or use a previous one and enter passwords
When you create a new key store make sure you make a backup and make sure you remember the password.
7. Follow the rest of the instructions in the wizard