
Create a file in com/carson/commands.
Call it SomethingCommands.java, or something similar. If you have a collection of command you want to add, 
You could base the file around those, or just create it around your name.

Write some boilerplate code:
```java
package com.carson.commands;

import com.carson.core.Command;
import com.carson.core.CommandCollection;
import com.carson.core.Handler;

public class TestCommands extends CommandCollection {

}
```
Change the word `TestCommands` to your file name. This is important

Add this to you code, between the class brackets:
```java
public TestCommands(){
    super("My - Name");
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
        super("My - Name");
    }

    @Override
    public void genCommands(List<Command> commands, Handler handle) {

    }
}

```

To add commands, you need to add a `Command` object to the commands list. In order to build a `Command` object, you need to use this method:
```java
    protected Command toCommand(Test test, CommandLambda lambda)
```
Calling this command will look something like this:
```java
toCommand(event -> {
    //testing code
},event -> {
   //run code 
});
```
The `Testing code` needs to return a boolean value. 

Test code may look something like this:
```java
event -> {
    String content = event.getMessage().getContent();
    if(content.startsWith("ping")){
        return true;
    }else{
        return false;
    }
}
```
Because a majority of commands only need to match commands that start with a specific word or keyphrase, a shortcut method is provided:
```java
Test.startsWith("ping")//Don't put the prefix here, it will automatically be added
```
And this will replace the `event -> {/*testing code*/}` segment, so it will look like this:
```java
toCommand(Test.startsWith("ping"), (MessageReceivedEvent event) -> {
   //run code 
});
```
Now for the run code. You have two objects you can use - `event` and `handler`. `handler` is what you will use to send messages, and
`event` contains all of the message information. Some standard `event` methods are shown here.
```java
toCommand(Test.startsWith("ping"), (MessageReceivedEvent event) -> {
    //run code
    String content = event.getMessage().getContent();
    long message_id = event.getMessageID();
    long author_id = event.getAuthor().getLongID();
    long channel_id = event.getChannel().getLongID();
    long guild_id = event.getChannel().getLongID();
});
```
Guilds are the official term for a server. ID's are 17-18 digit numbers that are unique to each object. To get ID's, turn on developer mode 
and right click messages, servers, channels, and people.

To send messages, use `handler.sendMessage`. You can pass the `event` object to send it to the same channel
```java
handle.sendMessage(event,"pong");
```
You can also send messages to other channels, like this
```java
handle.sendMessage(event.getClient().getChannelByID(123L),"pong");
```
Using the channel ID instead of `123L`. You need to have an `L` at the end of the number in order for it to compile successfully

You can send DM's like this:
```java
handle.sendMessage(event.getClient().getOrCreatePMChannel(event.getAuthor()),"pong");
handle.sendMessage(event.getClient().getOrCreatePMChannel(event.getClient().getUserByID(123L)),"pong");
```
The first sends a DM to the author of the message, the second sends a DM to the user with the ID of `123L`.

Keep in mind you can **not** send DM's to bots

Once you have finished writing the command, you need to add it to the command list. This is done by wrapping your `toCommand` code in `commands.add()`:
```java
commands.add(
    toCommand(Test.startsWith("ping"), (MessageReceivedEvent event) -> {
        handle.sendMessage(event,"pong");
    })
);
```
Now there's only one more line of code you need to write - adding your Command Collection to the global register.

Go to the file [com.carson.core.Main](Main.java) and find this static block:
```java
static{
    collectionList.add(new TestCommands());
}
```
It may have more or less lines, but all you need to do is add your Command Collection. 
write `collectionList.add(new TestCommands());`, but replace `TestCommands` with your file name, like before

And that's it! create a testing bot account, add it to a testing server, and copy the client token into a file called key.txt.
Run the program with your favorite java compiler.  (recommendation - use Eclipse). 