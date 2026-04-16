Build instructions

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

[TODO: i have to finish writing this later, but basically it's the same as Eclipse]

---

USAGE

[also TODO]