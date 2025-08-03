# NerdClanChat

A Java reimplementation of [CHClanChat](https://github.com/NerdNu/CHClanChat). The basics should be functionally identical to the user, but perform better on the backend. Some UI things have been cleaned up, and new features added.


NerdClanChat is a sort of party-chat plugin where players can make their own groups and invite players. Below are the roles of clanchats and the commands needed to use this plugin.

### Roles

| Role       | What it does                                                                                                            |
|------------|-------------------------------------------------------------------------------------------------------------------------|
| Non-member | Cannot chat or interact with a clanchat apart from checking the member list.                                            |
| Member     | Can send messages to a clanchat and the above.                                                                          |
| Manager    | Can modify clanchat colours, invite/uninvite/remove players, add/remove bulletins, use alerts `/ca`, and all the above. |
| Owner      | Can delete the clanchat, add/remove managers, and all the above.                                                        |

### ClanChat Management (`nerdclanchat.player`)
| Command                                                   | What it does                                                                                                             |
|-----------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| /clanchat                                                 | Displays NerdClanChat help.                                                                                              |
| /clanchat more                                            | Displays the second page of help.                                                                                        |
| /clanchat create \<channelName>                           | Creates a clanchat/group with the specified name.                                                                        |
| /clanchat delete \<channelName>                           | Deletes the clanchat of the specified name, assuming player is the owner.                                                |
| /clanchat confirm                                         | Confirms the deletion of a clanchat after running /clanchat delete \<channelName>.                                       |
| /clanchat color \<channelName> \<color>                   | Sets the clanchat label's colour. Can be a Minecraft colour or hex code (#FFFFFF).                                       |
| /clanchat textcolor \<channelName> \<color>               | Sets the clanchat's message colour. This is what the text players send. Can be a Minecraft colour or hex code (#FFFFFF). |
| /clanchat alertcolor \<channelName> \<color>              | Sets the clanchat's alert message colour (`/ca` command). Can be a Minecraft colour or hex code (#FFFFFF).               |
| /clanchat members \<channelName>                          | Gives the player the list of members of the specified clanchat.                                                          |
| /clanchat invite \<channelName> \<playerName>             | Invites the specified player to the specified clanchat.                                                                  |
| /clanchat uninvite \<channelName> \<playerName>           | Uninvites the specified player from the specified clanchat, assuming the player has been invited.                        |
| /clanchat changeowner \<channelName> \<playerName>        | If run by the owner of the specified clanchat, this makes the specified player the new owner of that clanchat.           |
| /clanchat addmanager  \<channelName> \<playerName>        | Adds the specified player as a manager of the specified clanchat.                                                        |
| /clanchat removemanager  \<channelName> \<playerName>     | Removes the specified player as a manager from the specified clanchat, assuming player is a manager.                     |
| /clanchat listmanagers \<channelName>                     | Lists the managers of the specified clanchat.                                                                            |
| /clanchat remove \<channelName> \<playerName>             | Kicks the specified player from the specified clanchat.                                                                  |
| /clanchat join \<channelName>                             | Used to join a public clanchat or to accept an invite.                                                                   |
| /clanchat leave \<channelName>                            | Used to leave a clanchat the player is a member of.                                                                      |
| /clanchat list                                            | Sends the player the list of clanchats they're a member of.                                                              |
| /clanchat public                                          | Lists all public clanchats.                                                                                              |
| /clanchat addbulletin \<channelName> \<bulletinMessage>   | Adds a message that appears for all members when they join the server.                                                   |
| /clanchat removebulletin \<channelName> \<bulletinNumber> | Removes the specified bulletin.                                                                                          |
| /clanchat subscribe \<channelName>                        | Enables bulletins being displayed from the specified clanchat.                                                           |
| /clanchat unsubscribe \<channelName>                      | Disables bulletins from being displayed from the specified clanchat.                                                     |
| /clanchat subscriptions                                   | Lists the player's current bulletin subscriptions.                                                                       |
| /clanchat flags \<channelName> \<flag> \<boolean>         | Toggles the state of a clanchat's flags (public and secret).                                                             |
|                                                           |                                                                                                                          |

## Chat Commands (`nerdclanchat.player`)
| Command                                  | What it does                                                                                                                                                        |
|------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| /clanchat chat \<channelName> \<message> | Sends a message to the specified clanchat.                                                                                                                          |
| /c [#[channelName]] \<message>           | Sends a message to the specified clanchat. The channelName is not needed if the receiving channel is the player's default. The channelName must have a # before it. |
| /cq [#[channelName]] \<message>          | Sends a message to the specified clanchat without making it the player's default.                                                                                   |
| /ca [#[channelName]] \<message>          | Sends an alert-style message to the specified clanchat.                                                                                                             |
| /cme [#[channelName]] \<message>         | Sends a message to the specified clanchat in the style of the `/me` command.                                                                                        |
| /cs [#[channelName]] \<message>          | Sends a message to the specified clanchat in slanted letters.                                                                                                       |
| /cr \<message>                           | Sends a message to the clanchat that last had a message sent to it by a player who isn't the one running this command.                                              |
| /cb [#[channelName]]                     | Provides the player with the bulletins of the specified channel. If one isn't specified, it provides bulletins for all channels the player is a member of.          |

### Administrative Commands (`nerdclanchat.admin`)
| Command                                            | What it does                                        |
|----------------------------------------------------|-----------------------------------------------------|
| /clanchat channels                                 | Lists all clanchats on the server.                  |
| /clanchat reloadconfig                             | Reloads the clanchat configuration.                 |
| /clanchat changeowner \<channelName> \<playerName> | As an admin, forces an owner change for a clanchat. |

## Build Instructions

 1. Build [BukkitEBean](https://github.com/NerdNu/BukkitEBean) and install in the local Maven repository.
   ```
git clone https://github.com/NerdNu/BukkitEBean
cd BukkitEBean
mvn clean install
   ```
 2. Build NerdClanChat:
   ```
cd ..
git clone https://github.com/NerdNu/NerdClanChat
cd NerdClanChat
mvn clean package
   ```

