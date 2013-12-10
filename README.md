/**********************************************
 * Please DO NOT MODIFY the format of this file
 **********************************************/

/*************************
 * Team Info & Time spent
 *************************/

	Name1: Sam Rang
	NetId1: spr20
	Time spent: 20 hours

	Name2: Brad Levergood
	NetId2: brl8
	Time spent: 20 hours

	Name3: Zach Michaelov
	NetId3: zgm3
	Time spent: 20 hours

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

*Inode.java:
The Inode is an abstraction for part of a file.
We use the first 4 bytes of the on-disk inode to specify the FileID and where the
 inode fits into the file.

*DFileID.java:
We gave DFileID's parameters inodeList, and ArrayList of Inodes associated with a 
	fileID, and a file size integer.  We associate multiple inodes with a file ID because,
	in terms of writing an inode and its associated memory blocks to our disk on a test 
 	file close, we assumed that there was a limit on inode size, as denoted in the constants.
 	java file, while creates a limit to the number of blocks that we can associate with
 	a single inode and still successfully write them all to the disk.


*LocalDBuffer.java:
Our implementation of the DBuffer abstract class gives every buffer that we use
	access to the universal virtual disk, its own blockID, and the byte array of data 
	that it holds.  StartFetch and StartPush methods make calls to the virtual disk 
	startRequest method calling operation types READ and WRITE respectively.  

Buffers also keep track of their own validity and "cleanliness," whether their data 
	needs to be pushed to disk when the sync method is called.  checkValid/Clean and 
	waitValid/Clean methods respectivley return these states, and wait on these states 
	to be true.  

To determine wether a buffer in our cache is held, pinned, or free, we use an enum 
	state that is marked as HELD when getBlock() is called, PINNED when io operations 
	are called, and FREE when a call is made to the releaseBlock() mehtod is called.  
	read() and write() functions in the DBuffer are written to work as specified in 
	the lab handout.  Buffers are marked as valid when they are read from which makes a 
	call to the startFetch function.  buffers are marked as dirty when new data is written 
	to them, which will need to be synced to the disk on a test file close.

The ioComplete() call, which is upcalled from the disk, just sends out a broadcast
	to any waiting threads.


*LocalDBufferCache.java:
Our implementation of DBufferCache keeps a map of DBuffers currently in the map, 
	a queue to keep track of which DBuffers should be removed via the LRU mehtod, and a 
	reference to the disk.

The get block method either returns a DBuffer from the BufferCache if it is in there, 
	and moves it to the back of our LRU queue. Otherwise, it creates a new DBuffer to and 
	fetches any appropriate data to the block from the disk, and marks its data as valid, 
	and removes the LRU DBuffer from the cache if necessary.  releaseBlock marks block as 
	free and sends out a broadcast.  The sync function calls the startPush() function for every 
	DBuffer in the cache that has been marked as "dirty," and calls waitclean until the disk 
	upcalls iocomplete.   

*LocalDFS.java:
Our DFS keeps track off all DfileIDs created, the fileIDs that are free for use, and the 
	fileIDs that are currently being used, as well as the iNodes available for use, 
	those being used, and the the memory blocks that are currently free, and those that have 
	been allocated.

In init() we initialize all of our available fileIDs, our inodes, and all blocks of memory 
	all based on our Constants values. This also calls rebuild() which attempts to rebuild the 
	old files from disk.

There is a bug in rebuild() because we are reading back inodes, and you are not guaranteed that 
	the inodes are in order, ideally I would call ArrayList.add(index i, Inode element) when adding
	inodes to a file, however ArrayList doesn't allow you to add something at an index greater than 
	the current size. Also, we don't know what the size of the file is until we read in all of the 
	inodes. We could use a map where we map an inode to an index but that would make iterating much
	more difficult. Currently we just add inodes regardless of order even though we have a way retrieving
	an inode index from disk.

The createDFile method creates a new DFileID object with the first available fileID and 
	associates the first available iNode for use with that fileID.  The destroyDFile function 
	frees all memory blocks associated with all iNodes associated with the DFile, frees 
	all iNodes associated with the DFile, sets the fileID as available for use, and removes the
	DFileID object from the list of existing files.

The read and write functions call the DBuffer read and write functions for all DBuffers associated 
	with the DFileID object used, cycling through all DBuffers in all the iNodes associated with 
	that object, adjusting the "offset" and "count" values as necessary using a modulus of the 
	block size Constants value.

The sizeDFile and listAllDFiles are both simple and self-explanatory, and the DFS-level sync 
	function simply calls the DBufferCache sync function.

The updateFileDes() function writes all of the inodes associated with a DFileID to the disk
	using the cache. This involves splitting up the integers across the byte arrays, getting the
	memory blocks associated with the inodes from the cache and syncing them back to disk.

*LocalVirtualDisk.java:
The disk is split into Inode-Region and Main-Disk-Region. The Inode-Region is large 
	that we can reference enough memory blocks for the max file size for the max number
	of files. 
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
 
