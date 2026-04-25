# Running Event Organizer

This project is plain Java + Swing — no Maven, Gradle, or external installer.

## Prerequisites

- **JDK 11 or newer** on the `PATH` (so `javac --version` and `java --version`
  work from any shell).
- Windows, macOS, or Linux. No GUI on macOS/Linux requires `JAVA_HOME` set the
  same way; Windows just needs `<jdk>\bin` on `PATH`.

Quick check:
```text
java -version
javac -version
```

If either command says "not recognised" or "command not found", install a JDK
(e.g. <https://adoptium.net/>) and re-open the shell.

## Windows — Command Prompt (cmd.exe)

```bat
build.bat
run.bat
```

To run the test suite:
```bat
test.bat
```

## Windows — PowerShell / Windows Terminal

PowerShell can invoke the same `.bat` files directly:
```powershell
.\build.bat
.\run.bat
.\test.bat
```

If PowerShell's execution policy refuses, the `.bat` files still work because
they're run by `cmd.exe` under the hood — execution policy applies to
`.ps1` scripts, not `.bat`.

## Windows — Git Bash / WSL / macOS / Linux

```bash
bash build.sh
bash run.sh
bash test.sh
```

Or make them executable once:
```bash
chmod +x build.sh run.sh test.sh
./build.sh && ./run.sh
```

## Reduced motion

If the aurora backdrop animation is distracting or hardware is slow, pass
`-Duinimal=true`:

```bat
java -Duinimal=true -cp "out;lib\*" com.eventorganizer.Main
```

```bash
java -Duinimal=true -cp "out:lib/*" com.eventorganizer.Main
```

The mesh freezes to a single static frame and every transition becomes instant.

## Persistence

State is auto-saved on shutdown to `eventorganizer.data` in the working
directory. The next launch loads it instead of running the demo seed. Delete
the file to start fresh.

## Vendored fonts (optional)

For the intended Inter / JetBrains Mono typography, drop these TTFs into
`lib/fonts/`:

- `Inter-Regular.ttf`, `Inter-Medium.ttf`, `Inter-SemiBold.ttf`,
  `Inter-Bold.ttf`, `Inter-ExtraBold.ttf`
- `JetBrainsMono-Regular.ttf`, `JetBrainsMono-Medium.ttf`

Both are SIL OFL — see <https://github.com/rsms/inter> and
<https://github.com/JetBrains/JetBrainsMono>. Without them the app still
runs and falls back to Segoe UI / SF Pro / sans-serif.

## Demo accounts

After the first launch the seed creates three demo users — passwords match the
username with a numeric suffix:

| Username | Password    |
|----------|-------------|
| `alice`  | `alice123`  |
| `bob`    | `bob123`    |
| `carol`  | `carol123`  |
