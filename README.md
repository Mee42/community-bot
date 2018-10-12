# CommunityBot
This is a bot built on ideas from people who can't code - and maintained by [Mee42](https://github.com/Mee42)


Contributing is very easy - install a git front-end, make some edits, and make a pull request. Contact me for any questions regarding how to use git/github

Code contributions can be made in two ways - Java code and Pseudo code

For java code, follow these guildlines:

- Create a file in com/carson/commands
- call it SomethingCommands.java, or something similar. If you have a collection of command you want to add,
you could base the file around those, or just create it around your name
- Write some boilerplate code:
```java
package com.carson.commands;

import com.carson.core.Command;
import com.carson.core.CommandCollection;
import com.carson.core.Handler;

public class TestCommands extends CommandCollection {

}
```
change the word `TestCommands` to your file name. This is important
- add this to you code, between the class brackets:
```java
public TestCommands(){
    super("My - name");
}
```
Again, replacing `TestCommands` with the name of your file, and `My - Name` with your actually name (or username, whatever)

You need one more piece of code before you can start writing commands, so add this method to your class:
```java
@Override
    public void genCommands(List<Command> commands, Handler handle) {
        
    }
```
Every command you write should be written inside that method. 

Your code should look something like this - and if it does, you're ready to begin!
```java
package com.carson.commands;

import com.carson.core.Command;
import com.carson.core.CommandCollection;
import com.carson.core.Handler;

import java.util.List;

public class TestCommands extends CommandCollection {
    public TestCommands(){
        super("My - name");
    }

    @Override
    public void genCommands(List<Command> commands, Handler handle) {

    }
}

```

*not done yet - more info coming soon*