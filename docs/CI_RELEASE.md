# CI & Tagged Releases (GitHub Actions)

Two workflows live in [`.github/workflows/`](../.github/workflows/):

- **`ci.yml`** — on every push / PR to `main`: runs the unit tests, builds the debug
  APK, and uploads it as a build artifact. This is the build-on-every-change check.
- **`release.yml`** — on pushing a **version tag** (`v*`): builds the **signed**
  release `.aab` + `.apk` and publishes a **GitHub Release** with both attached.

## One-time setup: signing secrets

The signing keystore is **never committed**. CI rebuilds it from encrypted GitHub
secrets. Add these under **Settings → Secrets and variables → Actions → New repository
secret**:

| Secret | Value |
| --- | --- |
| `KEYSTORE_BASE64` | base64 of `release.jks` (see below) |
| `KEYSTORE_PASSWORD` | the store password from `keystore.properties` |
| `KEY_ALIAS` | the key alias |
| `KEY_PASSWORD` | the key password |

Generate the base64 of the keystore:

```powershell
# Windows PowerShell (run in the project root)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.jks")) | Set-Content keystore.b64
```
```bash
# macOS / Linux
base64 -w0 release.jks > keystore.b64
```

Paste the contents of `keystore.b64` as the `KEYSTORE_BASE64` secret value, then delete
`keystore.b64`. (`gh secret set KEYSTORE_BASE64 < keystore.b64` also works if you use the
GitHub CLI.)

> This is the secure place to "keep" the keystore for builds — encrypted, write-only,
> and exposed only to the workflow at run time. **Also keep your own offline backup** of
> `release.jks` + `keystore.properties` (password manager / encrypted drive); if both the
> backup and the secret are ever lost, you can no longer sign updates with the same key.

## Cut a release

After the secrets are set:

```bash
git tag v1.0.0
git push origin v1.0.0
```

`release.yml` runs, and the new release appears under the repo's **Releases** with the
`.aab` (for Play) and `.apk` (for direct install) attached. Bump `versionCode` /
`versionName` in [`app/build.gradle.kts`](../app/build.gradle.kts) before each new tag.

> If you push a tag before the secrets exist, the run stops immediately with a message
> telling you what's missing — add the secrets and **Re-run jobs** on that run.

The signing config is environment-aware: locally it reads `keystore.properties`; in CI it
reads `KEYSTORE_FILE` / `KEYSTORE_PASSWORD` / `KEY_ALIAS` / `KEY_PASSWORD`.
