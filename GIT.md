## Git

##### Git is an extremely powerful tool that we will be using to manage this bot. You can use the command line, or a GUI

The instructions here are for the command line, but either one works.

Download and install git [here](https://git-scm.com/downloads). Be sure to download the `Windows` version

To start using git, open git bash.

**NOTE: on Windows, hyphens `-` may be replaced by slashes `/`, 
so `git commit -m 'asdf'` MIGHT BECOME `git commit /m 'asdf'`.** This might not be the case, but if hyphens aren't working, it's worth a try

Because of the many commands needed to use git, this tutorial will be structured like this:

`command *argument*`  :   reasoning

More information about the above command

#### So here we go:
`cd *path to place you want to keep files*`  :  move into a different working directory

`git clone https://github.com/mee42/community-bot.git`  :  Clone the entire git repo down onto your disk

`cd community-bot`  :  move into the cloned directory, all git commands must be run from here

`git checkout -b *branch name*`  :  Create a new branch and move into it. This is the branch you will do all of your work<br>
**Note: the default branch is called `Master`. Never, ever, work on `master`**

Here is where you will start coding. Every time you finish a block of code (like, a command), follow the below steps and commit.
This creates a sort of "checkpoint". For more information on how often you should commit, read 
[this](https://stackoverflow.com/questions/107264/how-often-to-commit-changes-to-source-control). 
<br>Just remember, **Don't commit code that doesn't actually work**.

To commit:<br>
`git add --all`  :   adds all files to git. Prevents missing code<br>
Always run this before running `git commit`<br>
`git commit -m '*informative, short, description of what you did*'`  :  Stores all changes in a commit. 
You need 'single quotes' around the message

Once Development is done, you need to push the branch to Github.<br>
`git push origin *branch name*`  :  Effectively "upload" your code on branch `branch name` to the `branch name` on Github.

After doing that, go to the github website and make a pull request from `branch name` to `master`,
and shoot me a DM so I know about it sooner. 
I will then look over your code to make sure it's good. If it's not, I'll deny your request and show you where you can improve.
Once I approve your pull request, I'll delete your branch from github. You should also delete it from your personal computer with:<br>
`git checkout master`  :  switch back to master<br>
`git branch -D *branch name*`  :  delete branch `*branch name*`

You should make a new branch if you want to do further work.

If you wish to collaborate with someone on something, come *talk to me*. I can help you set stuff up so you and your friends can collaborate on one branch.

If you want to improve anything on this repo, go through the same process. This keeps the `Master` branch clean and operational.