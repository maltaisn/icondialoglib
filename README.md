# Icon picker dialog
A fully customizable icon picker dialog that provides easy access to quality icons
without having to include them individually in your probject. Icons are bundled in
icon packs which are distributed as separate artifacts. Icon packs are defined by XML,
each icon consisting of an ID, a SVG path, a viewport size, a category and a list of
tags for searching.

<img src="screenshots/demo1.gif" width="40%" alt="Demo 1"/>  <img src="screenshots/demo2.gif" width="40%" alt="Demo 2"/>

### Gradle dependency
`implementation 'com.maltaisn:icondialog:X.Y.Z'`

Replace `X.Y.Z` with lastest version number: [![Download](https://api.bintray.com/packages/maltaisn/icon-dialog/icon-dialog/images/download.svg)](https://bintray.com/maltaisn/icon-dialog/icon-dialog/_latestVersion)

**Notes**

- Version 2.1.0 and above use AndroidX libraries.
- Version 3.0.0 and above are written in Kotlin.
- Min SDK version is 14.

## Icon packs
The following icon packs are available as of lastest version.

| Name |Dependency<sup>1</sup>|Version|Icons|Size<sup>2</sup>|Languages|
|:----:|:--------:|:-----:|:---:|:----:|:-------:|
|Default|`default`|1.0.0|1,045|304 kB|English, French, German, Portuguese, Spanish|
|[Font Awesome][font-awesome]|`font-awesome`|5.12.0|964|215 kB|Engligh|
|[Community Material][mdi-community]|`community-material`|4.6.95|3,516|496 kB|English|

<sup>1</sup> Dependency uses the following pattern `com.maltais:iconpack-<NAME>:X.Y.Z`, where `<NAME>` is the
name indicated in the table and `X.Y.Z` is the version. Icon packs use the same version
as original icons.
<sup>2</sup> As seen in Android Studio's APK analysis, includes icons and tags.

More packs can be added on request. Custom packs and third-party icon packs can easily be created
as well.

#### Translation
Some icon packs are not meant to be translated. These icon packs are auto-generated from
files provided by the people who manage the original icon collection. Tags don't make any
sense in a language other than in English, and thus cannot be translated. The scripts
used for auto-generation are available in the [utils][utils] module.

The default pack tags can be translated. With over 1,000 labels it takes between 3 and 5 hours
to translate in my experience. A collection of images with the icons for each label can be downloaded
[here](default-pack-label-images) to help with translation.

## Tutorial
Coming soon.

## Changelog
View [changelog](https://github.com/maltaisn/icondialoglib/blob/master/CHANGELOG.md) for release notes.

## Licenses
- Library core is licensed under Apache License 2.0
- For icons, see LICENSE file in each icon pack module.


[font-awesome]: https://fontawesome.com/icons
[mdi-community]: https://materialdesignicons.com/

[utils]: utils/
[default-pack-label-images]: https://github.com/maltaisn/icondialoglib/files/2957686/label-images.zip
