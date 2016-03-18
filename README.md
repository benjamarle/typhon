Typhon, the synchronizing ebook reader for Android
========================================================

Typhon is a free, open-source ebook reader that allows you to keep your reading progress synchronized across multiple devices.
This means you can read a few pages on your phone, than grab your tablet continuing where you left off.

It is a fork of PageTurner ( http://www.pageturner-reader.org/ ) and it adds a feature for Japanese dictionary lookup.

The lookup code takes its root on JadeReader. I recycled the Android specific code and made another 
project for the dictionary lookup called JRikai.

Typhon is licensed under the GPL-V3 license.

Benjamin Marlé benjamarle@gmail.com

Building Typhon
-------------------

# Install Java
*   On Ubuntu

        sudo apt-get install openjdk-8-jdk

*   On Windows install the JDK from http://www.oracle.com/technetwork/java/javase/downloads/index.html

Typhon uses Java 8 lambda's through usage of the RetroLambda library.

# Install the Android SDK 

1.   Download at http://developer.android.com/sdk/index.html
2.   Unzip
3.   Update 

        sdk/tools/android update sdk --no-ui
4. On Ubuntu install ia32-libs

        apt-get install ia32-libs
5. Add sdk/tools/ and sdk/platform-tools to your PATH

# Install USB drivers for your device

*   Make sure adb devices shows your device, for example

        $ adb devices
        List of devices attached 
        015d18ad5c14000c        device

# Example PATH setup in .bashrc

    export ANDROID_HOME=$HOME/projects/adt-bundle-linux/sdk/
    if [ $(uname -m) == 'x86_64' ]; then
        export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/jre
    else
        export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-i386/jre
    fi

    PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools


# Gradle

Typhon is built using Gradle. If you want to use a local Gradle version, make sure it's at least version 2.9.
The preferred way is to run the Gradle wrapper. This will automatically download the correct version of gradle to your system.

Run the Gradle wrapper by running

    gradlew

# Build Typhon
Once everything is in place you can build Typhon and install it on your device with 

    gradlew build
    gradlew installDebug