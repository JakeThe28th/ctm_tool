Build instructions

TODO: ADD LINKS

This project requires /nbt, /frost3d, and /snowui. 

After cloning the project, import it to your IDE of choice. I don't use gradle or maven, so you'll have to add the dependencies yourself.

---

ECLIPSE

You can import the project by slecting the foldor from `File -> Open Projects from File System`

If you've already cloned /nbt, /frost3d, and /snowui, and have imported them into the same workspace, it *might* just work right away. I use Eclipse, and the project file references sources relative to the workspace location... Though, I don't know for sure.

If it doesn't then you'll need to EITHER:

   1) download .jar versions of each library from the releases page, and then in Eclipse's Package Explorer panel, right click the project and select `Build Path -> Configure Build Path`, go to the "Libraries" tab, and add the jar files for each library to the classpath; OR

   2) clone the source code for each library, and in `Configure Build Path`, go to the "Source" tab, and add the source folders. If they're in the same workspace, you can click `Link Source -> Variables -> WORKSPACE_LOC -> Extend`, and then select the "src" folder within each project. After that, edit the 'Folder name' field to be (anything other than ['src'/a name you used for a different folder]).

   **NOTE: Currently, I haven't made any prebuilt releases for those libraries. So, as of writing, option 2 is the only actual option.**

In addition to /nbt, /frost3d, and /snowui, you'll need some libraries from LWJGL. If you've cloned /frost3d, you can just go to "Libraries" in `Configure Build Path`, and select all of the .jar files in /frost3d/lwjgl, and add them to the classpath. Otherwise, you can get them from the LWJGL website. I forgot which libraries you need lol.

After that, you should be able to run the project with the run button. You might need to force all of the classes to rebuild by going into `Project -> Clean`, since the repository doesn't have the 'bin' folder.

---

INTELLIJ

There's probably a better way to set up the project, but here's how I got it to run

1) Download/Clone this repository, /nbt, /frost3d, and /snowui
2) In the top menu, go to `File -> Open` and open this repository's root folder
3) Click the gear in the top right and go to `Project Structure`
4) Go to "Modules", then click the plus in the middle column, and then `New Module`.
5) It might already be filled out, but create the module so that the text below it says
  `Module will be created in ~\...\ctm_tool-main`
   After creating it, you should see the repository's folder structure, where 'src' is highlighted in blue to indicate it's a source folder.
6) AFAIK there are three options for adding the main libraries:
	a) For each library, click "Add Content Root" and add its' root folder. Make sure the 'src' folder in each is marked as a source folder.
   If you click apply, you should be able to go to CTMToolMain.java, and all of the imports aside from the lwjgl stuff won't be red.
	b) Or, you can put the main libraries' folders in the main module's folder, and then in thn the Sources tab in Project Structure, find the 'src' folders, right click them, and click "Sources"
	c) Or, you can download the libraries as .jar files, and add them to the project the same way you add the LWJGL stuff in step 7. But, this option isn't possible right now, because I haven't released any jar versions of the libraries yet. So really, it's just the last two.
7) Next, go to the `Dependencies` tab (still in `Modules` in `Project Structure`), and click the plus, and then `JARs or Directories`. 
8) Go to wherever you put `frost3d/lwjgl/`, and select all of the .jar files, and press 'OK'.
9) I don't know if this is needed since I didn't test without doing it, but if they're not already checked, click the checkmark box on all of the jars.

After clicking Apply/OK, you should be able to run CTMToolMain.java.

---

USAGE

[also TODO]