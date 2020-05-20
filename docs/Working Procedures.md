# Working Procedure

<!-- TOC -->

- [Working Procedure](#working-procedure)
	- [GitHub](#github)
		- [Branch Structure](#branch-structure)
		- [Long-term Branch Guidelines](#long-term-branch-guidelines)
		- [Topic Branch Guidelines](#topic-branch-guidelines)
		- [Commit Message Guidelines](#commit-message-guidelines)
		- [Working Procedure - Step by Step](#working-procedure---step-by-step)
	- [Javadoc](#javadoc)
		- [Format of a Doc Comment](#format-of-a-doc-comment)
		- [Descriptions](#descriptions)
		- [A Style Guide](#a-style-guide)
		- [Tag Conventions](#tag-conventions)
		- [Documenting Default Constructors](#documenting-default-constructors)
		- [Documenting Exceptions with @throws Tag](#documenting-exceptions-with-throws-tag)
		- [Package-Level Comments](#package-level-comments)
		- [Documenting Anonymous Inner Classes](#documenting-anonymous-inner-classes)
		- [Including Images](#including-images)
		- [Full Example](#full-example)

<!-- /TOC -->

## GitHub

### Branch Structure

To keep things simple, the project branching will have one long-term branch: master, which should not be worked on directly. This will be supported by topic branches that are created to fulfil a specific purpose. On these branches code will be developed, tested, and finally pushed to master, with the topic branch then being deleted.

![Figure](https://i.imgur.com/Uu8kSFI.png "Figure 1-1")

### Long-term Branch Guidelines

1. Do not work directly on master (ie. commits to master should be rare: simple file adds etc.).
2. Only push code to master if it has been tested and is working correctly.
3. At any point, master should be fully functional.

### Topic Branch Guidelines

1. Only work on a single topic per branch.
2. Each branch should have a relatively short lifespan where possible.
3. Keep the topic branch up to date with master where possible to minimize merge conflicts when the topic branch is pushed back to master.
4. Keep the local and remote branches in sync by pushing often - this helps others stay up-to-date with the progress of your work.

### Commit Message Guidelines

1. Be concise.
2. Short reasons for changes if they are not obvious are acceptable.
3. Low level explanations of code should go in JavaDoc comments.
4. High level reasoning for code structure and processes should be detailed elsewhere to be later incorporated into a high level documentation document.

### Working Procedure - Step by Step

![Figure 1-2](https://i.imgur.com/2ptZs7m.png "Figure 1-2")

Clone repository:
```sh
$ git clone https://github.com/mosu001/hccm.git
```

Fetch latest changes:
```sh
$ git fetch origin
```

Create new branch:
```sh
$ git branch [branch-name]
$ git checkout [branch-name]
```

Add files
```sh
$ git add .
```

Commit to remote:
```sh
$ git commit [-m "commit message"]
$ git push origin [branch-name]
```

Push to master:
```sh
$ git push origin master
```

Alternatively, create a pull request using the GitHub website.

Delete branch (optional)
```sh
$ git branch -d [branch-name]
```

## Javadoc

### Format of a Doc Comment

- Doc comments are written in HTML and must proceed a class, field, constructor or method declaration.
- They are made up of two parts: a description followed by block tags (e.g. @param, @return).

### Descriptions

 First Sentence:

- The first sentence of each doc comment should be a summary sentence, containing a concise but complete description of the API item.
- This means the first sentence of each member, class, interface or package description.
- The Javadoc tool copies this first sentence to the appropriate member, class/interface or package summary, making it important to write informative initial sentences that can stand on their own.
- In particular, write summary sentences that distinguish overloaded methods from each other.

Implementation Independence:

- Write the description to be implementation-independent, but specifying such dependencies where necessary.
- Define clearly what is required and what is allowed to vary across platforms/implementations.

Reuse of Method Comments:

 You can avoid re-typing doc comments for methods that override or implement other methods. This occurs in three cases:

- When a method in a class overrides a method in a superclass
- When a method in an interface overrides a method in a superinterface
- When a method in a class implements a method in an interface

### A Style Guide

- Use ```<code></code>``` for keywords and names e.g. Java keywords, package names, class names, method names etc.
- Use in-line links economically (ie. not necessary for well know API etc.).
- Omit parentheses for the general form of methods and constructors.
- Acceptable to use phrases instead of complete sentences for brevity.
- Use 3rd person (descriptive) e.g. "gets the label" not "get the label".
- Method descriptions begin with a verb phrase.
- Class/interface/field descriptions can omit the subject and simply state the object.
- Use "this" instead of "the" when referring to an object created from the current class.
- Add description beyond the API name.
- Be clear when using the term "field".
- Avoid Latin.

### Tag Conventions

Order of Tags:

- @author
- @version
- @param
- @return
- @exception
- @see
- @since
- @serial
- @deprecated

Ordering Multiple Tags:

- Multiple @author tags should be listed in chronological order, with the creator of the class listed at the top.
- Multiple @param tags should be listed in argument-declaration order.
- Multiple @throws tags (also known as @exception) should be listed alphabetically by the exception names.
- Multiple @see, see [here](https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html#tag)

Required Tags:

- @param required for every parameter
- @return required for every method that returns something other than void

### Documenting Default Constructors

- Good programming practice dictates that code should never make use of default constructors in public APIs: All constructors should be explicit.
- When it documents such a constructor, Javadoc leaves its description blank, because a default constructor can have no doc comment.
- Note that when creating an explicit constructor, it must match precisely the declaration of the automatically generated constructor; even if the constructor should logically be protected, it must be made public to match the declaration of the automatically generated constructor, for compatibility. An appropriate doc comment should then be provided.

### Documenting Exceptions with @throws Tag

- The purpose of the @throws tag is to indicate which exceptions the programmer must catch (for checked exceptions) or might want to catch (for unchecked exceptions).
- Document the following exceptions with the @throws tag: all checked exceptions, reasonable unchecked exceptions
- It is generally desirable to document the unchecked exceptions that a method can throw: this allows (but does not require) the caller to handle these exceptions.
- Unchecked exceptions are the classes RuntimeException, Error and their subclasses.
- All other exception subclasses are checked exceptions.

### Package-Level Comments

- Each package can have its own package-level doc comment source file that the Javadoc tool will merge into the documentation that it produces.
- This file is named package.html (and is same name for all packages).
- This file is kept in the source directory along with all the *.java files.

Contents:

- The package doc comment should provide (directly or via links) everything necessary to allow programmers to use the package.
- It should contain a short, readable description of the facilities provided by the package (in the introduction, below) followed by pointers to detailed documentation, or the detailed documentation itself, whichever is appropriate.

Sections and Headings:

- Summary sentence
- Describe what the package contains and state its purpose
- Package Specification
  - Description of/links to package wide specifications for packages not included in the Javadoc documentation
  - Links to specifications written outside of doc comments
  - Include specific references
- Related Documentation
- Class and Interface Summary

### Documenting Anonymous Inner Classes

- The Javadoc tool does not directly document anonymous classes -- that is, their declarations and doc comments are ignored.
- If you want to document an anonymous class, the proper way to do so is in a doc comment of its outer class, or another closely associated class.

### Including Images

Images in Source Tree:

- Naming: Name GIF images \<class>-1.gif, incrementing the integer for subsequent images in the same class.
- Location: Put doc images in a directory called "doc-files". This directory should reside in the same package directory where the source files reside.

Images in HTML Destination:

- Naming:  Images would have the same name as they have in the source tree.
- Location:
  - With hierarchical file output, such as Javadoc 1.2, directories would be located in the package directory named "doc-files".
  - With flat file output, such as Javadoc 1.1, directories would be located in the package directory and named "images-\<package>".

### Full Example

```sh
/**
* Graphics is the abstract base class for all graphics contexts
* which allow an application to draw onto components realized on
* various devices or onto off-screen images.
* A Graphics object encapsulates the state information needed
* for the various rendering operations that Java supports.  This
* state information includes:
* <ul>
* <li>The Component to draw on
* <li>A translation origin for rendering and clipping coordinates
* <li>The current clip
* <li>The current color
* <li>The current font
* <li>The current logical pixel operation function (XOR or Paint)
* <li>The current XOR alternation color
*     (see <a href="#setXORMode">setXORMode</a>)
* </ul>
* <p>
* Coordinates are infinitely thin and lie between the pixels of the
* output device.
* Operations which draw the outline of a figure operate by traversing
* along the infinitely thin path with a pixel-sized pen that hangs
* down and to the right of the anchor point on the path.
* Operations which fill a figure operate by filling the interior
* of the infinitely thin path.
* Operations which render horizontal text render the ascending
* portion of the characters entirely above the baseline coordinate.
* <p>
* Some important points to consider are that drawing a figure that
* covers a given rectangle will occupy one extra row of pixels on
* the right and bottom edges compared to filling a figure that is
* bounded by that same rectangle.
* Also, drawing a horizontal line along the same y coordinate as
* the baseline of a line of text will draw the line entirely below
* the text except for any descenders.
* Both of these properties are due to the pen hanging down and to
* the right from the path that it traverses.
* <p>
* All coordinates which appear as arguments to the methods of this
* Graphics object are considered relative to the translation origin
* of this Graphics object prior to the invocation of the method.
* All rendering operations modify only pixels which lie within the
* area bounded by both the current clip of the graphics context
* and the extents of the Component used to create the Graphics object.
*
* @author      Sami Shaio
* @author      Arthur van Hoff
* @version     %I%, %G%
* @since       1.0
*/
public abstract class Graphics {

/**
* Draws as much of the specified image as is currently available
* with its northwest corner at the specified coordinate (x, y).
* This method will return immediately in all cases, even if the
* entire image has not yet been scaled, dithered and converted
* for the current output device.
* <p>
* If the current output representation is not yet complete then
* the method will return false and the indicated
* {@link ImageObserver} object will be notified as the
* conversion process progresses.
*
* @param img       the image to be drawn
* @param x         the x-coordinate of the northwest corner
*                  of the destination rectangle in pixels
* @param y         the y-coordinate of the northwest corner
*                  of the destination rectangle in pixels
* @param observer  the image observer to be notified as more
*                  of the image is converted.  May be
*                  <code>null</code>
* @return          <code>true</code> if the image is completely
*                  loaded and was painted successfully;
*                  <code>false</code> otherwise.
* @see             Image
* @see             ImageObserver
* @since           1.0
*/
public abstract boolean drawImage(Image img, int x, int y,
ImageObserver observer);

/**
* Dispose of the system resources used by this graphics context.
* The Graphics context cannot be used after being disposed of.
* While the finalization process of the garbage collector will
* also dispose of the same system resources, due to the number
* of Graphics objects that can be created in short time frames
* it is preferable to manually free the associated resources
* using this method rather than to rely on a finalization
* process which may not happen for a long period of time.
* <p>
* Graphics objects which are provided as arguments to the paint
* and update methods of Components are automatically disposed
* by the system when those methods return.  Programmers should,
* for efficiency, call the dispose method when finished using
* a Graphics object only if it was created directly from a
* Component or another Graphics object.
*
* @see       #create(int, int, int, int)
* @see       #finalize()
* @see       Component#getGraphics()
* @see       Component#paint(Graphics)
* @see       Component#update(Graphics)
* @since     1.0
*/
public abstract void dispose();

/**
* Disposes of this graphics context once it is no longer 
* referenced.
*
* @see       #dispose()
* @since     1.0
*/
public void finalize() {
dispose();
}
}
```
