# Vendored fonts (Inter + JetBrains Mono)

Place the following TTF files here for the Obsidian Aurora UI to use the
intended typography. Both families are SIL Open Font License (OFL),
redistribution is permitted.

Required files:

- Inter-Regular.ttf
- Inter-Medium.ttf
- Inter-SemiBold.ttf
- Inter-Bold.ttf
- Inter-ExtraBold.ttf
- JetBrainsMono-Regular.ttf
- JetBrainsMono-Medium.ttf

Sources:
- Inter:        https://github.com/rsms/inter (look in releases for *.ttf)
- JetBrainsMono: https://github.com/JetBrains/JetBrainsMono

If these files are absent the app still runs — `FontLoader` logs a warning
and the OS fallback chain (Segoe UI, SF Pro, sans-serif) is used.
