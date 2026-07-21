# Environment Setup & Validation

This document explains how to validate your development environment for `vog-demo`,
how the required tools were installed, and — because this machine has several Java
versions — how to manage and switch between them.

## 1. Required tools

| Tool  | Required | Why |
|-------|----------|-----|
| Java (JDK) | **17** (17 or 21) | Spring Boot 4.1.0 requires Java 17 as a minimum. `pom.xml` sets `java.version=17`. |
| Maven | 3.9+ | Boot 4 needs Maven 3.9+. You don't install this — the **Maven Wrapper** (`./mvnw`) downloads 3.9.16 automatically. |
| Node.js | 18+ | For the React (Vite) frontend. |
| npm | 9+ | Ships with Node. |
| git | any recent | Version control. |

## 2. Validate the environment (copy/paste)

Run these to confirm everything is in place:

```bash
java -version          # must show 17.x  (NOT 1.8 / 11)
echo "$JAVA_HOME"      # should point at a .../17...  path
./mvnw -version        # from the vog-demo/ folder: Maven 3.9.16, Java 17.0.x
node -v && npm -v      # Node >= 18, npm >= 9
git --version
```

Then confirm the project actually builds:

```bash
cd vog-demo
./mvnw clean compile   # first run downloads Spring Boot 4.1.0; must end with BUILD SUCCESS
```

**What "good" looks like on this machine (verified):**
- `java -version` → `openjdk version "17.0.19"` (Temurin)
- `./mvnw -version` → `Apache Maven 3.9.16 ... Java version: 17.0.19`
- `./mvnw clean compile` → `BUILD SUCCESS`

## 3. What was installed here, and why

This machine uses **SDKMAN** to manage JDKs. It already had Java 8 (`8.0.392`,
`8.0.482`) and Java 11 (`11.0.21`) — **none of which meet the Java 17 requirement**.
So Temurin 17 was installed:

```bash
# One-time: install Java 17 via SDKMAN
sdk install java 17.0.19-tem
```

It was **not** set as the global default, so your other projects that rely on
Java 8/11 keep working. Instead, Java 17 is pinned to this project (see §5).

> If SDKMAN is not yet loaded in a shell, load it first:
> `source "$HOME/.sdkman/bin/sdkman-init.sh"`

## 4. Managing multiple Java versions with SDKMAN

You have several JDKs installed. Here is how to see and control them.

### List what you have / what's available
```bash
sdk list java              # full catalog; installed ones are marked
sdk current java           # which Java is active right now
```

Currently installed on this machine:
```
8.0.392-tem
8.0.482-tem
11.0.21-tem
17.0.19-tem   <-- required by vog-demo
```

### Switch Java — three scopes

| Command | Scope | Use when |
|---------|-------|----------|
| `sdk use java 17.0.19-tem` | **Current shell only** — reverts when you close the terminal | Quick, temporary switch |
| `sdk default java 17.0.19-tem` | **Global default** for all new shells | You want 17 everywhere (⚠ affects Java 8/11 projects) |
| `sdk env` | **Per-directory**, reads `.sdkmanrc` | Auto-use the right Java per project (recommended) |

> Note: after `sdk use`, the *same* shell may still report the old version because
> bash caches command paths. Run `hash -r` (or open a new command) and it will be
> correct. New shells are unaffected.

### Install / remove versions
```bash
sdk install java 21.0.11-tem      # add another version
sdk uninstall java 8.0.392-tem    # remove one you no longer need
```

## 5. Per-project auto-switching (recommended)

A `.sdkmanrc` file is committed at the project root so the correct Java is selected
for this project without touching your global default:

```
# vog-demo/.sdkmanrc
java=17.0.19-tem
```

Apply it manually anytime with:
```bash
cd vog-demo
sdk env          # switches this shell to Java 17.0.19-tem
sdk env clear    # revert to your default
```

To make this **automatic** whenever you `cd` into the folder, enable auto-env once:
```bash
sdk config
# set:  sdkman_auto_env=true
```
After that, entering `vog-demo/` switches to Java 17 and leaving it reverts — no
manual step, and your Java 8/11 projects are never disturbed.

## 6. Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `java -version` shows 1.8 or 11 | Wrong JDK active | `sdk use java 17.0.19-tem` then `hash -r` |
| `sdk: command not found` | SDKMAN not loaded in this shell | `source "$HOME/.sdkman/bin/sdkman-init.sh"` |
| Build fails with "release version 17 not supported" | Maven running on old Java | Ensure Java 17 active, then `./mvnw -version` should show Java 17 |
| `./mvnw` permission denied | Wrapper not executable | `chmod +x mvnw` |
