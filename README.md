# differential-equations

The following steps were done for the project creation.

- setup of Java, Maven, Android SDK and Android artifacts
- created project with Android SDK
- added minimal pom.xml file
- built application with running emulator:

mvn clean install android:deploy

- if an avd with the name 16 with platform 1.6 or higher exists the emulator can be started with

mvn android:emulator-start
