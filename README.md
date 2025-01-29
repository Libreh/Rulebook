# IMPORTANT LICENSE NOTICE
By using this project in any form, you hereby give your "express assent" for the terms of the license of this project (see [LICENSE](https://raw.githubusercontent.com/Libreh/Rulebook/refs/heads/main/LICENSE)), and acknowledge that I (the author of this project) have fulfilled my obligation under the license to "make a reasonable effort under the circumstances to obtain the express assent of recipients to the terms of this License".

# Rulebook
A server-side rules screening mod for Fabric.

Supports [Placeholder API](https://placeholders.pb4.eu/user/general/), [Simplified Text Format](https://placeholders.pb4.eu/user/text-format/), and per player permissions with [LuckPerms]([https://github.com/LuckPerms/LuckPerms](https://github.com/LuckPerms/LuckPerms)).

<img src="https://raw.githubusercontent.com/Libreh/Rulebook/refs/heads/main/rulebook_reject.gif" width="400"/>
<img src="https://raw.githubusercontent.com/Libreh/Rulebook/refs/heads/main/rulebook_accept.gif" width="400"/>

## Commands (and permissions):
- `/rulebook` and `/rules` - Displays the rules in chat (`rulebook.main`, available by default)
- `/rulebook reload` - Relods configuration (requires `rulebook.reload`)
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
  // Messages for when players are kicked
  "kickMessages": {
    // When the accept button isn't clicked
    "didntAccept": "<red>You didn't accept the rules</red>",
    // When said player's rules are updated
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
  ]
}
```

## Credits
Parts of this mod are inspired by and uses code by:
- [LilydevMC/Rules](https://github.com/LilydevMC/Rules)
