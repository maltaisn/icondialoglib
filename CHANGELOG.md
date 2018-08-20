## v1.1.2
- Fixed crash on API 21-22 when opening dialog
- Fixed search terms not getting normalized
- Search query is now whitespace trimmed

## v1.1.1
- Added 21 icons (1010 icons total): ballot, braille, brain, cassette, currency-php, current-ac, current-dc, desk-lamp, dog, dog-side, tulip, fountain, hand, horseshoe, lighthouse, lighthouse-on, pirate, subtitles, toggle-switch, camera-vintage, vote
- Updated 11 icons: medal (60), tumble dryer (92), silverware (452), angle (666), plus-minus (672), exponent (676), not-equal (678), knife-1 (758), knife-2 (760), arrow-unmerge (864), table-search (975)
- Added 21 labels: accessibility, ballot, braille, brain, cassette, curr_php, current_ac, current_dc, election, form, fountain, horseshoe, lighthouse, park, pirate, subtitles, switch, toggle, tulip, vote, wheelchair
- Added `IconHelper.getIconCount()`

## v1.1.0
- Added Portuguese translation
- Added IconView, wrapper around ImageView to set icons by XML
- Fixed `IconHelper.reloadLabels()` not working

### v1.0.1
- Added 12 icons, updated 4 icons.
- Added convenience method `setSelectedIcon(int iconId)`` for selecting a single icon
- Change `setAllowMultipleSelection` to `setMaxSelection`
- Removed BroadcastReceiver for language change, it was not unregistered
- Search bar now has search action and keyboard is hidden on enter.
- Synchronized some methods

## v1.0.0
- Initial release
- Customizable styles for almost every part of the dialog
- Many available options
  - Single or multiple selection
  - Enable search and search language
  - Show category headers, sticky headers
  - Show clear selection button
  - Disable speicific categories and icons
- Ability to customize search algorithm and icon sorting
- Ability to add extra categories and icons with labes, can overwrite default ones
- Dialog is automatically restored on configuration change