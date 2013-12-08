/**********************************************
 * Please DO NOT MODIFY the format of this file
 **********************************************/

/*************************
 * Team Info & Time spent
 *************************/

	Name1: Sam Rang
	NetId1: spr20
	Time spent: 

	Name2: Brad Levergood
	NetId2: brl8
	Time spent: 

	Name3: Zach Michaelov
	NetId3: 
	Time spent: 

/******************
 * Files to submit
 ******************/

	lab4.jar // An executable jar including all the source files and test cases.
	README	// This file filled with the lab implementation details
   DeFiler.log   // (optional) auto-generated log on execution of jar file

/************************
 * Implementation details
 *************************/
 
 **General Implementation:
 
 **General Assumptions:
 Files can only increase in size after creation
 Files are either pinned or held after creation until they are explicitly released
 	at which point their DBuffers are evictable from memory

VirtualDisk.java:
The only thing added to this file is the private rebuild() function which is the 
alternative to formatStore(). rebuild() attempts to read the file _volumeName and 
create our object hierarchy from its data. It also checks to see if the data is 
corrupted.


/************************
 * Feedback on the lab
 ************************/

/*
 * Any comments/questions/suggestions/experiences that you would help us to
 * improve the lab.
 * */

/************************
 * References
 ************************/

/*
 * List of collaborators involved including any online references/citations.
 * */
 
