### v3.1.1
- Fixed icon drawable sharing mutable state with all drawables of the same icon. This caused problems where tinting one drawable would tint all of them.
- Updated material components version to stable 1.0.0.

## v3.1.0
- Added progress bar in dialog for when icon pack is not loaded yet.
- Fixed `IconDialogSettings.dialogTitle` having no effect.

# v3.0.0
- Complete rewrite in Kotlin with MVP architecture.
- Now based on Google's Material Components with out of the box support for dark theme.
- Added support for API 14.
- **Icon packs**
    - Icons are no longer part of the library, they are contained in icon packs, which can
    be downloaded as separate artifacts. Available icon packs:
        - Default: contains the icons from previous version.
        - Font Awesome: https://fontawesome.com/icons, solid variant only.
        - Material Design Icons: https://github.com/Templarian/MaterialDesign, minus brand and outline icons.
    - Icon packs loading is delegated to the user of the library. `IconHelper` was replaced
    with `IconPackLoader` which loads icon packs from XML. User must load icon packs
    appropriatedly (for example on application start, asynchronously) and store them.
    - Overriding icons is now done by specifying a parent icon pack and defining
    icons or categories with existing IDs.
- **Icons**
    - XML root element for tags is now `<icons>` instead of `<list>`.
    - Icon viewport dimensions can be defined per icon pack or on individual icons.
- **Tags** ("labels" previously)
    - Tags no longer have a value or aliases, they only have a list of values.
    - Tag references in XML are no longer supported.
    - XML root element for tags is now `<tags>` instead of `<list>`.
- Dialog no longer uses deprecated `setRetainInstance(true)`.
- Added `IconDialogSettings` class to configure the dialog appearance and behavior.
- Removed a few attributes for customization, like dialog button texts.
- Various icon filter changes. Sorting by category is now delegated to icon dialog.
- Replaced all IntDefs with enums.

## v2.5.0
- Added support for API 16.

## v2.4.0
- Added a parameter with the dialog instance to the callback method.
- Spanish translation, thanks to hieudev.

### v2.3.2
- Added a parameter with the instance of the IconHelper in `LoadCallback.onDataLoaded`.

### v2.3.1
- Prevented AAPT bug where category string references weren't changed to an ID.

## v2.3.0
- Added dismiss listener.
- Improved nullity annotations for Kotlin.
- Updated 1 icon: wind-turbine (251).

## v2.2.0
- Support for API 19
- German translation

## v2.1.0
- Migrated to AndroidX
- Selecting an icon with keyboard open after search now closes it.

- Updated 8 icons: emoticon-tongue (15), network-access (187), home-insurance (298), bread-slice (407), bullhorn (577), tortoise (616), turtle (617), link (827).
- Added 18 icons: barn, basketball-hoop, parachute, bulldozer, seatbelt, chat-round, crystal-ball, emoticon-angry, emoticon-cry, emoticon-kiss, emoticon-wink, shoe-formal, shoe-heel, fountain-pen, maple-leaf, sheep, measuring-tape, telescope.
- Added 19 labels.

### v2.0.2
- `dataChanged` field is now updated asynchronously in IconHelper

### v2.0.1
- Fixed library dependencies not being added to the project
- Removed unused dependencies

# v2.0.0
- Breaking change: IconHelper data is now loaded asynchronously (was previously loaded on the UI thread and it took 200 - 600 ms). If trying to get icon, label or category before data is loaded, null will be returned.
When data is loaded (after the initial call to `getInstance`, `addExtraIcons` or `reloadLabels`), all `LoadCallback`s attached with `addLoadCallback` will be called. If a callback is attached when data is already loaded, it is automatically called. A callback is removed after being called once. To avoid NPEs, do not use any `get` method if you are unsure whether data is loaded or not.
- Icon dialog shows ProgressBar if data isn't loaded
- Prefixed all IDs of the library's views with `icd_`

- Added 17 icons: ampersand, balloon, bank-in, bank-out, bank-transfer, dishwasher, doctor, egg, easter-egg, helicopter, label, safe, smog, solar-power, spray, t-shirt, import
- Updated 2 icons: router (218), file-download (980)
- Added 22 labels: ampersand, balloon, bottle, deposit, dishwasher, doctor, easter, egg, import, label, nurse, pollution, power_state, programming_language, renewable_energy, safe, smog, smoke, solar_power, tshirt, wind_power, withdraw
- Removed 1 label: tag

### v1.1.2
- Fixed crash on API 21-22 when opening dialog
- Fixed search terms not getting normalized
- Search query is now whitespace trimmed

### v1.1.1
- Added 21 icons (1010 icons total): ballot, braille, brain, cassette, currency-php, current-ac, current-dc, desk-lamp, dog, dog-side, tulip, fountain, hand, horseshoe, lighthouse, lighthouse-on, pirate, subtitles, toggle-switch, camera-vintage, vote
- Updated 11 icons: medal (60), tumble dryer (92), silverware (452), angle (666), plus-minus (672), exponent (676), not-equal (678), knife-1 (758), knife-2 (760), arrow-unmerge (864), table-search (975)
- Added 21 labels: accessibility, ballot, braille, brain, cassette, curr_php, current_ac, current_dc, election, form, fountain, horseshoe, lighthouse, park, pirate, subtitles, switch, toggle, tulip, vote, wheelchair
- Added `IconHelper.getIconCount()`

## v1.1.0
- Added Portuguese translation
- Added IconView, wrapper around ImageView to set icons by XML
- Fixed `IconHelper.reloadLabels()` not working

### v1.0.1
- Added 12 icons, updated 4 icons.,
- Added convenience method `setSelectedIcon(int iconId)`` for selecting a single icon
- Change `setAllowMultipleSelection` to `setMaxSelection`
- Removed BroadcastReceiver for language change, it was not unregistered
- Search bar now has search action and keyboard is hidden on enter.
- Synchronized some methods

# v1.0.0
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
