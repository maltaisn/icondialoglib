# Icon picker dialog
A fully customizable icon picker dialog that provides easy access to quality icons
without having to include them individually in your project. Icons are bundled in
icon packs, which are distributed as separate artifacts. Icon packs are defined by XML,
each icon consisting of an ID, a SVG path, a viewport size, a category and a list of
tags for searching.

<img src="screenshots/demo1.gif" width="40%" alt="Demo 1"/>  <img src="screenshots/demo2.gif" width="40%" alt="Demo 2"/>

### Gradle dependency
`implementation 'com.maltaisn:icondialog:X.Y.Z'`

Replace `X.Y.Z` with lastest version number:  [![Download](https://api.bintray.com/packages/maltaisn/icon-dialog/com.maltaisn%3Aicondialog/images/download.svg)](https://bintray.com/maltaisn/icon-dialog/com.maltaisn%3Aicondialog/_latestVersion)

**Notes**

- Version 2.1.0 and above use AndroidX libraries.
- Version 3.0.0 and above are written in Kotlin.
- Min SDK version is 14.

## Icon packs
The following icon packs are available as of lastest version.

| Name |Dependency<sup>1</sup>|Version|Icons|Size<sup>2</sup>|Languages|
|:----:|:--------:|:-----:|:---:|:----:|:-------:|
|Default|`default`|[![Lastest version](https://api.bintray.com/packages/maltaisn/icon-dialog/com.maltaisn%3Aiconpack-default/images/download.svg)](https://bintray.com/maltaisn/icon-dialog/com.maltaisn%3Aiconpack-default/_latestVersion)|1,045|304 kB|5 languages|
|[Font Awesome][font-awesome]|`font-awesome`|[![Lastest version](https://api.bintray.com/packages/maltaisn/icon-dialog/com.maltaisn%3Aiconpack-font-awesome/images/download.svg)](https://bintray.com/maltaisn/icon-dialog/com.maltaisn%3Aiconpack-font-awesome/_latestVersion)|965|215 kB|English|
|[Community Material][mdi-community]|`community-material`|[![Lastest version](https://api.bintray.com/packages/maltaisn/icon-dialog/com.maltaisn%3Aiconpack-community-material/images/download.svg)](https://bintray.com/maltaisn/icon-dialog/com.maltaisn%3Aiconpack-community-material/_latestVersion)|3,732|525 kB|English|

Note that the process to update some icon packs (MDI, Font Awesome) is automated and icons
might be deleted either on purpose or by mistake. Make sure to always handle `null` icon values
when obtaining an icon by ID!

<sup>1</sup> Dependency uses the following pattern `com.maltaisn:iconpack-<NAME>:X.Y.Z`, where `<NAME>` is the
name indicated in the table and `X.Y.Z` is the version. Icon packs use the same version
as original icons.

<sup>2</sup> As seen in Android Studio's APK analysis, includes icons and tags.

More packs can be added on request. Custom packs and third-party icon packs can easily be created
as well.

## Usage
- [**Example application**](https://github.com/maltaisn/icondialoglib/wiki/Example-application): simple application setup to use the icon dialog.
- [Styling the dialog](https://github.com/maltaisn/icondialoglib/wiki/Styling-the-dialog): styling attributes explained
- [Custom icon pack](https://github.com/maltaisn/icondialoglib/wiki/Custom-icon-packs): tutorial on how to create custom icon packs.

## Translation
Some icon packs are not meant to be translated. These icon packs are auto-generated from
files provided by the people who manage the original icon collection. Tags don't make any
sense in a language other than in English, and thus cannot be translated. The scripts
used for auto-generation are available in the [utils][utils] module.

The default pack tags can be translated. With over 1,000 labels it takes between 3 and 5 hours
to translate (in my experience). A collection of images with the icons for each label can be downloaded
[here][default-pack-label-images] to help with translation.

## Changelog
View [changelog][changelog] for the library release notes. Each icon pack module also have their own changelog.

## Licenses
- Library core is licensed under Apache License 2.0
- For icons, see LICENSE file in each icon pack module.


[font-awesome]: https://fontawesome.com/icons
[mdi-community]: https://materialdesignicons.com/

[changelog]: CHANGELOG.md
[utils]: utils/
[default-pack-label-images]: https://github.com/maltaisn/icondialoglib/files/2957686/label-images.zip
