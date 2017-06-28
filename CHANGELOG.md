# Change Log
All notable changes to this project will be documented in this file. This
project adheres to [Semantic Versioning](http://semver.org/).

## [0.1.0] - 2017-06-27
### Fixed
- Explicitly set the character set to UTF-8 when reading XML

### Changed
- Upgrade underlying VTD-XML dependency to 2.13

## [0.0.9] - 2013-09-05
### Added
- Add ability to return the text of an attribute using `text`
- Add ability to check if the navigator is pointed at an attribute with `attribute?`

## [0.0.8] - 2013-05-02
### Added
- Gracefully handle `nil` across the library to make threading easier

## [0.0.7] - 2013-03-29
### Added
- Enrich navigators so that are now sequential, seqable and countable data structures

## [0.0.6] - 2013-03-29
### Added
- Added `select` for selecting elements by name or wildcard

## [0.0.5] - 2013-03-28
### Added
- Added transient interface via `root!`, `parent!`, `next-sibling!`, `previous-sibling!`, `first-child!` and `last-child!`

## [0.0.4] - 2013-03-25
### Added
- Added `attr?` for testing the existence of attributes
- Added ability to pass optional element names to `first-child`, `last-child`,
  `next-sibling`, `previous-sibling`, `siblings` and `children`

## [0.0.3] - 2013-03-24
### Added
- Added support for XML namespaces when searching

### Changed
- Replaced public `token-type` with `element?` and `document?` functions

## [0.0.2] - 2013-03-24
### Added
- Add ability to fetch previous sibling
- Add ability to fetch token type for an element

### Fixed
- Fix fetching all siblings for a navigator, both previous and next

## [0.0.1] - 2013-03-24
### Added
- First stable version of riveted

[0.1.0]: https://github.com/mudge/riveted/releases/tag/v0.1.0
[0.0.9]: https://github.com/mudge/riveted/releases/tag/v0.0.9
[0.0.8]: https://github.com/mudge/riveted/releases/tag/v0.0.8
[0.0.7]: https://github.com/mudge/riveted/releases/tag/v0.0.7
[0.0.6]: https://github.com/mudge/riveted/releases/tag/v0.0.6
[0.0.5]: https://github.com/mudge/riveted/releases/tag/v0.0.5
[0.0.4]: https://github.com/mudge/riveted/releases/tag/v0.0.4
[0.0.3]: https://github.com/mudge/riveted/releases/tag/v0.0.3
[0.0.2]: https://github.com/mudge/riveted/releases/tag/v0.0.2
[0.0.1]: https://github.com/mudge/riveted/releases/tag/v0.0.1
