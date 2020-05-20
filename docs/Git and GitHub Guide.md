# Git and GitHub Guide

<!-- TOC -->

- [Git and GitHub Guide](#git-and-github-guide)
	- [Git Basics](#git-basics)
		- [Remote Repositories](#remote-repositories)
		- [Branching in Git](#branching-in-git)
		- [Basic Branching and Merging](#basic-branching-and-merging)
		- [Basic Merge Conflicts](#basic-merge-conflicts)
		- [Branch Management](#branch-management)
		- [Remote Branches](#remote-branches)
		- [Pushing](#pushing)
		- [Pull Requests](#pull-requests)
	- [Basic Git Commands](#basic-git-commands)
		- [git clone](#git-clone)
		- [git status](#git-status)
		- [git add](#git-add)
		- [git mv](#git-mv)
		- [git log](#git-log)

<!-- /TOC -->

## Git Basics

Git is a distributed version-control system for tracking changes to files, especially in source code during software development. One of Git's main features is strong support for non-linear development. Since each worker has a local copy of the repository, multiple workers can work on a project at the same time with minimal conflicts.

Git stores snapshots of files rather than differences, with links to previous identical files that are unchanged.

![Figure 1-1](https://i.imgur.com/IVEuqNg.png "Figure 1-1")

Git has three main states that files can be in:

- Committed: stored in the local repository
- Modified: changed but not yet committed
- Staged: a modified file marked to be committed

![Figure 1-2](https://i.imgur.com/AzkpR67.png "Figure 1-2")

Git stores the metadata for your project in the .git directory within the repository. The working directory is a single checkout of one version of the project. The staging area is a file, generally in the Git directory, that stores the information about what will go into the next commit.

The basic Git workflow is as follows:

1. Modify files in the working directory
2. Stage the files, adding them to the staging area
3. Commit the files in the staging area to a permanent snapshot in the Git directory

![Figure 1-3](https://i.imgur.com/S15Vvyq.png)

### Remote Repositories

Most version control related work happens in the local repository: staging, committing, viewing the status or the log/history, etc.

Remote repositories are used when collaboration is required. A remote repository is like a file server that you use to exchange data with others.

The actual work on your project happens only in your local repository: all modifications have to be made and committed locally. Then, those changes can be uploaded to a remote repository in order to share them with your team. Remote repositories are only used to share and exchange code between developers - not for actually working on files.

Repositories on GitHub are examples of remote repositories.

![Figure 1-4](https://i.imgur.com/kasgMRl.png "Figure 1-4")

### Branching in Git

Branching is the process of diverging from the main line of development and continuing on work without messing with that main line. Branching is important because in real world projects there are often many development contexts happening in parallel e.g. multiple new features, bug fixes, documentation updates etc.

Branching solves this problem by allowing these contexts to be worked on independently and then combined in a relatively seamless way.

All the changes you make at any time will only apply to the currently active branch; all other branches are left untouched. This gives you the freedom to both work on different things in parallel and to experiment without fear of breaking anything.

Branches are the perfect tool to help you avoid mixing up different lines of development. You should use branches extensively in your development workflows: for new features, bug fixes, experiments, ideas etc.

![Figure 1-5](https://i.imgur.com/4QsaHqM.png "Figure 1-5")

In Git, a branch is simply a pointer to a commit. The default branch is master, but this branch is not special, it is exactly like every other branch. So each branch points to a commit, which has a history of commits behind it.

![Figure 1-6](https://i.imgur.com/Ql0PXar.png "Figure 1-6")

When you create a new branch with ```git branch```, a new pointer is created at the same commit you are currently on, which Git knows from the special pointer HEAD. HEAD is a pointer that always points to the local branch you are currently on.

You can switch branches with ```git checkout```, which moves HEAD to the specified branch, and changes the files in your working directory to match those in the snapshot that that branch points to.

**Note: Switching branches changes your working directory**

You can now make a new commit, which will move the new branch forward, whilst leaving the old branch in the same place. E.g.

```sh
$ git checkout testing
```

![Figure 1-7](https://i.imgur.com/yizQ09z.png "Figure 1-7")

```sh
$ git commit -a -m "made a change"
```

![Figure 1-8](https://i.imgur.com/oO4Hj5f.png "Figure 1-8")

### Basic Branching and Merging

Branches can be merged with ```git merge [branch-name]```, which combines branch-name into the current branch.  If branch-name is a direct descendent of the current branch, Git simply moves the head pointer forward to branch-name.  If the current branch is not a direct descendent of branch-name, Git will find a common ancestor, and perform a three-way merge using the two branch tips and the common ancestor.

![Figure 1-9](https://i.imgur.com/07N3BiS.png)

```sh
# Merge iss53 into master
$ git checkout master
$ git merge iss53
```

![Figure 1-10](https://i.imgur.com/0Ki5A3Z.png)

### Basic Merge Conflicts

Occasionally merging won't go smoothly. This is because the two branch ends have both made changes to the same part of a file or files of the common ancestor, and those changes do not agree. In this case, Git pauses the merge commit while you resolve the problem. You can use ```git status``` to check which files have conflicts. To resolve the conflicts, open the file and find the section(s) that looks like the following:

```sh
<<<<<<< HEAD:index.html
<div id="footer">contact : email.support@github.com</div>
======
<div id="footer">
 please contact us at support@github.com
 </div>
 >>>>>>> iss53:index.html
```

This shows that the HEAD branch and iss53 branch have different content in this section of index.html. The ====== separates the content of the two branches. To resolve this, you should replace the section shown with a single implementation (either one, or the other, or a combination of both) removing the other information. Once this is done, run ```git add``` to mark the file as resolved.

Alternatively, you can resolve merge conflicts with a graphical tool using ```git mergetool```, which will open the default merge tool.

Once all merge conflicts are resolved, you can run ```git commit``` to finalize the merge commit. You should add some details about why the merge was resolved the way it was if you think it would be helpful to others in the future, especially if the why is not obvious.

Note: Ideally merge conflicts should be rare as multiple contributors should not be working on the exact same things at the same time.

### Branch Management

You can run ```git branch``` with no arguments to get a list of the branches, with an asterisk denoting the currently checked out branch (ie the one that HEAD points to). That means that any commits at this point will go on that branch. The -v option shows the hash id and commit messages of the last commit on each branch. The --merged option shows all the branches that already merged into the current branch - these are generally safe to delete (apart from the HEAD).

### Remote Branches

Remote branches are pointers to the state of branches in your remote repositories that you cannot move. They are only moved when you communicate with the remote repository e.g. with ```git fetch```. Remote branches show you where the branches on your remote repositories were the last time you connected to them. E.g.

![Figure 1-11](https://i.imgur.com/hZ62BWS.png "Figure 1-11")

### Pushing

To share a local branch to a remote repository so others can view it, you need to explicitly push it using ```git push```.

### Pull Requests

Rather than pushing directly to master, you can use the Github website to create a pull request, which will allow others to review your changes before they are merged.

## Basic Git Commands

Git commands can be executed in the regular Terminal, the Git Bash shell (which comes with Git), or often indirectly through the IDE you are working with.

### git clone

```sh
$ git clone https://github.com/mosu001/hccm.git
```

This command copies the contents of an existing repository to your local system, allowing you to modify them. This includes the full history of all files in the repository.

### git status

```sh
$ git status
```

This is the main tool used to determine the state of the various files. It will return information such as the current branch, which files if any are currently untracked, and which changes have yet to be committed.

### git add

```sh
$ git add README.md
```

This command adds files to the list of tracked files. By default, new files created are not tracked, so an untracked file is a file that wasn't in the previous snapshot (commit). This command supports file-glob patterns, so commands like ```git add .``` or ```git add *.txt``` can be used to add multiple files at once.

```git add``` is also used to stage modified files using the same syntax. Files that appear in ```git status``` as "Changes not staged for commit" can be staged with ```git add```. For this reason, it may be helpful to think of ```git add``` as "add this content to the next commit".

Note: if you modify a file after running ```git add```, the file will show as both staged (the older version) and unstaged (the new modified one). This means you need to run ```git add``` again to ensure the latest version of the file is the one being committed.

### git diff

```sh
$ git diff [--staged]
```

A more precise version of ```git status```, ```git diff``` shows the exact lines added and removed for each file. ```git diff``` compares the working directory with the staging area, and tells you the changes you've made that you haven't yet staged.

Note: ```git diff``` only shows unstaged changes, not changes made since the last commit. To compare your staged changes to your last commit, use ```git diff --staged```.

### git commit

```sh
$ git commit [-a] -m "commit message"
```

Once the staging area is setup, you can commit your changes with ```git commit```. Anything that is still unstaged won't go into this commit (files that you haven't run ```git add``` on)

Every commit creates a new snapshot of the project that can be reverted to or compared to later.

The -m flag allows you to add a message explaining the changes that you have made since the last commit. Since all the changes are recorded, this needs only to be a couple of lines outlining the major changes.

If you want to add a more detailed comment, you can skip the -m flag, which will take you into the built-in bash text editor. To then write the commit message, hit the INSERT key then type. To finish, hit ESCAPE, then type ```:wq``` and hit ENTER. The first line will appear in yellow and be the title of the commit, with subsequent lines appearing in white and representing additional details.

Note: instead of adding files to the staging area, you can use ```git commit -a -m "commit message"``` to automatically stage every tracked file before committing, allowing you to skip the ```git add``` part.

You can use --amend to undo a commit if you forgot to add a file or want to change the commit message. E.g.

```sh
% Changing commit message
$ git commit -m "wrong message"
$ git commit --amend -m "right message"
```

```sh
% Add forgotten files
git commit -m "..."
git add ...
git commit --amend
```

### git rm

```sh
$ git rm [--cached] example.txt
```

To remove a file from Git, you have to remove it from the tracked files and then commit. If you just delete the file, it will show up as "changed but not update" (ie. unstaged) in ```git status```. ```git rm``` will remove the file from the tracked files and from the working directory, so that it won't show up as untracked in the future (note: these changes as always only take effect when committed).

```sh
$ git rm --cached example.txt
```

If you want to keep a file in the working directory but remove it from the tracked files, you can use the ```--cached``` option. This is useful for files that you forgot to add to your .gitignore.

```git rm``` supports file-glob patterns e.g. ```git rm log/\*.log``` which would remove all files in the log directory that end in .log.

### git mv

```sh
$ git mv
```

Git doesn't explicitly track file movement, but you can use ```git mv``` to do so regardless, and Git will figure it out afterwards. Thus, you can rename files with ```git mv file_from file_to```.

### git log

```sh
$ git log [-p] [--stat]
```

```git log``` allows you to view the commit history of the project, and has many options - see [here](https://git-scm.com/docs/git-log). The git log output shows information like the hash ID of the commit, the author, the date and time, the commit message, and will show commits in reverse chronological order.

```sh
$ git log -p -2
```

You can use -p to show the differences introduced in each commit, and add a -2 to limit the output to the last two entries (or any other number).

```sh
$ git log --stat
```

You can use --stat to show some abbreviated stats for each commit, like a list of the modified files, the number of files changes, and the number of lines added/removed in each.

Git log also has a lot of formatting options that aren't necessary but can be useful -- see [here](https://git-scm.com/book/en/v2/Git-Basics-Viewing-the-Commit-History).

### git reset

```sh
$ git reset HEAD <file>
```

If you want to unstage a file, say if you used ```git add``` incorrectly, you can use git reset to remove that file from the staging area.

### git fetch

```sh
$ git fetch [remote-name]
```

This command is used to fetch data from a remote repository and make it accessible locally. It pulls down all the data from the remote project that you don't have yet. After you run fetch, you have references to all the branches from that remote, which you can merge or inspect. But the new data is not automatically merged with your work and doesn't modify what you are currently working on. You need to merge manually or use ```git pull``` which merges automatically.

### git pull

```sh
$ git pull [remote-name]
```

Like fetch, this command pulls data from a remote repository. Unlike fetch, it automatically tries to merges this data with your local code. Effectively a git fetch followed by a git merge. Generally you should prefer to do the fetch and merge separately.

### git push

```sh
$ git push [remote-name] [branch-name]
```

When you want to share the state of a project, you can use ```git push``` to push it upstream. This will only work if you have write access to the server you cloned from and no one else has pushed in the meantime. If they have, you will have to pull and merge their changes first.

Alternatively, you can use ```git push [remote] [localbranch]:[remotebranch]``` to push the local branch to a remote branch with a different name.

### git branch

```sh
$ git branch [-v] [--merged] [--no-merged] [branch-name]
```

This command creates a new branch that points to the same commit as the current branch, but does not switch to it.

```-v``` shows hash ids and commit messages of the last commit to each branch

```--merged``` shows all the branches already merged into the current branch. These are generally safe to delete (apart from the HEAD).

### git checkout

```sh
$ git checkout [-b] [branch-name]
```

This command switches the branch you are on from the current branch to branch-name, and changes the files in your working directory to match that branch. The optional ```-b``` switch signifies that branch-name does not exist and is being created in place. So ```git checkout -b [branch-name]``` is equivalent to a ```git branch``` followed by a ```git checkout```.

### git merge

```sh
$ git merge [branch-name]
```

This command merges branch-name into the branch you are currently on, assuming there are no merge conflicts.

Note: if you successfully merge a branch, you are probably finished with it, and should delete it with ```git branch -d [branch-name]```.

## Sources

[Git Tower](https://www.git-tower.com/learn/git/ebook/en/desktop-gui/remote-repositories/introduction#start)

[Wikipedia](https://www.wikiwand.com/en/Git)

[Pro Git](https://git-scm.com/book/en/v2)

[Git Tower](https://www.git-tower.com/learn/git/ebook/en/desktop-gui/branching-merging/branching-can-change-your-life#start)
