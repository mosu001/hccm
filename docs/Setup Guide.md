# Setup Guide

## Git Installation

Details for setting up Git on Windows, macOS, and Linux

### Windows

 1. Download [Git for Windows](https://git-scm.com/downloads).
 2. Run the installer executable.
 3. Leave all options as default.
 4. Check that Git is installed with the command `git --version` in the Command Prompt, which should display the currently installed version of Git.

### macOS

1. Use `git` in the Terminal, which will prompt you to install Git if it is not already installed.
2. Check that Git is installed with the command `git --version` in the Terminal, which should display the currently installed version of Git.

### Linux

1. Follow the instructions [here](https://git-scm.com/download/linux) or use your distributions package manager
2. Check that Git is installed with the command `git --version` in the Terminal, which should display the currently installed version of Git.

## Git Configuration

Quick customization of the Git environment. Configuration is stored either in the gitconfig file (all users), the .gitconfig file (user specific), or the .git/config file (project specific). The following commands should be executed in the Terminal.

**Identity**:

This information is used in your Git commits.

```sh
$ git config --global user.name "John Doe"
$ git config --global user.email johndoe@example.com
```

## GitHub Setup

## GitHub Account

1. Create an account at GitHub.com
2. Email Mike to make a request to be added as a contributor (since the repository is private)

## Setting up the Project Files

### Cloning the project repository

This creates a local copy of the files in the repository, which can then be modified.

1. Create an empty folder where the project files will be stored.
2. Open the Terminal and navigate to this folder e.g. 
```sh
$ cd "<directory path>"
```
3. Clone the repository
```sh
$ git clone "https://github.com/mosu001/hccm.git"
```

### Cloning the JaamSim repository

Rather than including the entire JaamSim repository inside the project repository, we will clone it separately and link it within Eclipse. 

1. Create an empty folder where the project files will be stored.
2. Open the Terminal and navigate to this folder e.g. 
```sh
$ cd "<directory path>"
```
3. Clone the repository
```sh
$ git clone "https://github.com/jaamsim/jaamsim.git"
```

## Setting Up Eclipse

1. Download the Eclipse IDE [here](https://www.eclipse.org/downloads/) and choose Eclipse for Java development in the installer.
2. Install the Java JDK from [here](https://www.oracle.com/java/technologies/javase-jdk14-downloads.html). Accept the option to install the JRE  at  the same time.
3. Append the path for the JDK bin folder (e.g. C:\\Program Files\\Java\\jdk1.7.0\_45\\bin) to the PATH environment variable as follows (Windows 10)
    1. In the Window search bar, type "path" and select "Edit the system environment variables"
    2. Select "Environment Variables", select "Path" from the list and click "Edit"
    3. Select "New" and paste the path to the JDK bin folder
    4. Apply and exit
4. Set up the JRE in Eclipse as follows
    1. Launch Eclipse
    2. Select "Window" > "Preferences" > "Java" > "Installed JREs"
    3. Confirm that there is an entry for the JRE installed on your computer. If not, add it using the path to the the main jre directory (e.g. .\\jre1.8.0\_221)
5. Create a new project in Eclipse for the repository files
    1. Select "File" > "New" > "Java Project"
    2. Enter a project name (the name is unimportant)
    3. Unselect "Use default location" and select the directory into which the GitHub repository was cloned
    4. Check "Use default JRE" and "Create separate folders for sources and class files"
    5. Select "Next". You should see a list of the repository's files and folders under "Source". Under "Details" use "Link additional source" to link the following directories from the JaamSim repository cloned earlier (Note: links given are examples only, your paths may be slightly different up to \\src\\):
    
    | Folder Location | Folder Name |
    | ------ | ------ |
    | C:\JaamSim-master\src\main\java | main |
    | C:\JaamSim-master\src\main\resources | resources |
    | C:\JaamSim-master\src\test\java | test |

    6. Select the "Libraries" tab and click "Add Library" > "User Library" > "User Libraries" > "New" and enter "JOGL2". Select the new library and click "Add External JARs". Browse to the JaamSim folder .\jar\ and select "gluegen-rt.jar" and "jogl-all.jar". Apply and close. Now check the JOGL2 library to add it to the class path and select "Finish". 
    7. Select "Add Library" > "JUnit" > "JUnit4" to add the JUnit library to the build path.
    8. Finally, select "Finish" to create the new Java Project.
6. Set the Run Configuration
    1. Select "Run" > "Run Configurations"
    2. In the "Main" tab, under "Project" enter the project name, and under "Main Class" select "Search" and find "GUIFrame - com.jaamsim.ui" and select it.

## Next Steps

You are now ready to begin working on the hccm plugin. For an overview of Git and GitHub as they pertain to this project consult the Git and GitHub Guide in the hccm repository. For an overview of the working procedures for this project, consult the Working Procedures in the hccm repository.
