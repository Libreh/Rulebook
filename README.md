# Rulebook
A server-side rules screening mod for Fabric.

Supports [Placeholder API](https://placeholders.pb4.eu/user/general/), [Simplified Text Format](https://placeholders.pb4.eu/user/text-format/), and per player permissions with [LuckPerms](https://github.com/LuckPerms/LuckPerms).

<img src="https://raw.githubusercontent.com/Libreh/Rulebook/refs/heads/main/rulebook_reject.gif" width="400"/>
<img src="https://raw.githubusercontent.com/Libreh/Rulebook/refs/heads/main/rulebook_accept.gif" width="400"/>

## Clarifications
You accept the rules by viewing all pages and closing the rulebook.
You can close by clicking the `Done` or `Take Book` buttons, or pressing Escape on your keyboard.
The `Take Book` button is not supposed to give the player the book (that's just what it says and AFAIK you can't change it server-side).

## Commands (and permissions):
- `/rulebook` and `/rules` - Displays the rules in chat (`rulebook.main`, available by default)
- `/rulebook reload` - Reloads configuration (requires `rulebook.reload`)
- `/rulebook update` - Resets all players' rule status and kicks them, forcing them to accept the rules again (requires `rulebook.update`)
- `/rulebook update <players>` - Same as above but only for targeted players (requires `rulebook.update`)
- `/rulebook update offline` - Same as above but only for offline players (requires `rulebook.update`)
- `/rulebook accept` - Used for accepting the rules (`rulebook.main`, available by default)

## Configuration
```json5
// New lines can be added with `\n`!
{
  // Header at the top of each book page and at the top of the rules command
  "rulesHeader": "Rules Header\n",
  // Rule formatting, supports the variables %rule_number%, %rule_title%, %rule_description%
  "ruleSchema": "%rule_number%. %rule_title%\n%rule_description%\n",
  // Displayed at the last page
  "finalPage": "By closing the rulebook <bold>%player:name%</bold> you hereby agree to <underline>all the rules</underline>",
  // Messages for when a player is kicked
  "kickMessages": {
    // Player hasn't visited all pages
    "didntRead": "<red>You didn't read all the rules!</red>",
    // Player's rules have been updated
    "updatedRules": "<yellow>Rules updated, please reconnect</yellow>"
  },
  // All the different rules, each with a title and description
  "rules": [
    {
      "title": "title",
      "description": "description"
    },
    {
      "title": "more title",
      "description": "more description"
    }
  ],
  // List of players that have accepted
  "acceptedPlayers": []
}
```

## Credits
Parts of this mod are inspired by and uses code by:
- [LilydevMC/Rules](https://github.com/LilydevMC/Rules)
