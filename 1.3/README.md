### The following enumeration describes the process of creating a signed APK in detail:

1. In android studio, naviagate to build and select Create Signed Bundle or APK in the dropdown menu. 
2. Select APK and click next. 
3. To sign our APK, we have to create both a Key and KeyStore (if we don't have one already). Click on "Create new..." to begin the process to create a KeyStore and Key pair. 
4. To create a keystore, we need to specify a path where the keystore will be saved and a keystore password. To create the key, we need to give it a password, name, validty and optionally other personal information about the owner of the key. When all information have been entered, click ok.
5. Continue the APK creation process by clicking next with your new keystore and key selected to proceed to the final step.
6. The last step involves specifying the path where the signed APK will be saved and a "build type". The build type specyfies the build target, which can be either release or test. If the APK is supposed to be released to the public, then the release build type should be selected.
7. Choose a signature type, choose v2 which is the most recent and more secure one.
8. We have now created a signed APK of our application that is ready to be distributed. 
