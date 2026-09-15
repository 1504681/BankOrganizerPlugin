<!-- managed by ~/homelab/bin/claude-md; hand-written notes go below the line -->
# BankOrganizer

A RuneLite plugin that helps you organize your bank by categorizing items, highlighting misplaced ones, and guiding you through reordering items within tabs.

## Run
- `./gradlew run` launches a RuneLite client with the plugin loaded; `./gradlew build` must pass before any hub PR (the hub builds your exact commit).

## Test
- `./gradlew test`

## Where things live
- Runtime data (git-ignored, never commit): `local.gradle`, `src/test/java/com/bankorganizer/tools`, `tools`.
- Git: https://github.com/1504681/BankOrganizerPlugin.git (PUBLIC).

## Rules for this repo
- RuneLite plugin. Process, hub submission and the auto-merge update path: ~/homelab/runbooks/runelite-plugin.md.
- Keep README and Plugin Hub PR bodies short: what it does, settings, changelog. No gradle/run boilerplate in the README.
- Keep `runelite-plugin.properties` and `runelite_plugin.json` in sync; bump the version constant + changelog with every hub update.
- Verify RuneLite API symbols offline with `javap -cp ~/.gradle/caches/modules-2/files-2.1/net.runelite/<jar>` before coding against them.
- PRIVATE tooling: `tools/`, `ItemDump`, `local.gradle` and `./gradlew dumpItems` output are the user's own. Never commit them, never mention them in commits, PRs or issue replies.
- This is the reference plugin for layout and conventions; copy from here when starting a new one.

## Conventions
Box-wide conventions and the port map: ~/homelab (BOX.md is imported into every session). Registry: add/edit services.toml then bin/apply. No Claude attribution or session links in anything pushed.

<!-- hand-written notes below this line are preserved by bin/claude-md -->
---
