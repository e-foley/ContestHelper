## ContestHelper ##

### Introduction ###
This code generates a suite of webpages that contains image galleries, statistics, and more based on input files that describe user labels, image URLs, and votes.
The code is somewhat specialized for the [Screenshot of the Week](http://www.purezc.net/forums/index.php?showforum=343) competitions held on [PureZC](http://www.purezc.net), but the principles behind it can be extended to other contexts.

### Setting up the project ###
The program depends on access to three directories:
* A **configuration directory** containing webpage templates and other shared web resources that the program incorporates into the complete pages it builds.
  * The top-level **config** directory within this repository is a suitable choice.
* An **input** directory containing raw contest and username data.
  * This directory must contain **data.txt**, which describes contest results, and **associations.txt**, which describes member name changes.
  * The author will provide example files and syntax documentation in a future update.
* An **output** directory in which generated webpages are placed.
  * For the moment, the directory must exist before running the program. It will not be created automatically.

### Running the program ###
Compile the program targeting Main and invoke the resulting JAR executable with three arguments, separated by a space. The call might resemble the following if you choose to have your **input** and **output** directories at the same level as this repository's **config**.

```bat
java -jar "run_archive_generator.jar" "config" "input" "output"
```
