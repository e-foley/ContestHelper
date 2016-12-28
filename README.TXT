## ContestHelper ##

### Introduction ###
This code generates a suite of webpages that contains image galleries, statistics, and more based on input files that describe user labels, image URLs, and votes.
The code is somewhat specialized for the [Screenshot of the Week](http://www.purezc.net/forums/index.php?showforum=45) competitions held on [PureZC](http://www.purezc.net), but the principles behind it can be extended to other contexts.

### Creation ###
In August 2012, Ed Foley started this project in Excel for use with the [Map of the Month](http://www.purezc.net/forums/index.php?showforum=196) competition.
He later transferred the project to Java in December 2012 and began adapting it to Screenshot of the Week.

### Setting up the project ###
The following directories and files should exist before attempting to run the program.
* backup
* config
  * config\archives_footer.txt
  * config\archives_header.txt
  * config\leaderboard_footer.txt
  * config\leaderboard_header.txt
  * config\profile_footer.txt
  * config\profile_header.txt
* members
* web
  * web\images
    * web\images\no_image.png
  * web\profiles
  * web\associations.txt
  * web\data.txt
  * web\style.css

Files have their own formatting conventions that warrant future documentation.

The author plans to make this directory structure and sample files available automatically in future updates.
