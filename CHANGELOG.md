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