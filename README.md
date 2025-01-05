<img src="https://raw.githubusercontent.com/Libreh/Rulebook/refs/heads/main/rulebook_reject.gif" width="400"/>
<img src="https://raw.githubusercontent.com/Libreh/Rulebook/refs/heads/main/rulebook_accept.gif" width="400"/>

# Rulebook
A server-side rules screening mod for Fabric.

Supports [Placeholder API](https://placeholders.pb4.eu/user/general/), [Simplified Text Format](https://placeholders.pb4.eu/user/text-format/), and per player permissions with [LuckPerms]([https://github.com/LuckPerms/LuckPerms](https://github.com/LuckPerms/LuckPerms)).

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
  "rules_header": "Rules Header\n",
  // Rule formatting, supports the variables %rule_number%, %rule_title%, %rule_description%
  "rule_schema": "%rule_number%. %rule_title%\n%rule_description%\n",
  // At the top of the last page
  "accept_confirmation": "Do you accept the rules?\nClick the checkmark if yes: ",
  // At the bottom of the last page, click it to accept the rules
  "accept_button": "<green>☑</green>",
  // Messages for when players are kicked
  "kick_messages": {
    // When the accept button isn't clicked
    "didnt_accept": "<red>You have to accept the rules to play</red>",
    // When said player's rules are updated
    "updated_rules": "<yellow>The rules have been updated, please reconnect</yellow>"
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
Parts of this mod are inspired and uses code by:
- [LilydevMC/Rules](https://github.com/LilydevMC/Rules)