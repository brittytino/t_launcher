# How to Submit T_Launcher to F-Droid

## Prerequisites

- ✅ Open source app (MIT License) 
- ✅ Reproducible builds from source
- ✅ No proprietary dependencies
- ✅ No tracking or ads

## Method 1: Request for Packaging (RFP) - Easiest

This is the **recommended method** for beginners.

### Step 1: Create GitLab Account

1. Go to https://gitlab.com/fdroid/rfp
2. Create a GitLab account if you don't have one

### Step 2: Submit RFP Issue

1. Click **New Issue**
2. Fill in the template:

```markdown
### App Name
T_Launcher

### App Description
Minimal, focus-driven Android launcher with dynamic wallpapers and zero distractions. Built with Kotlin and Jetpack Compose.

### App Source Code
https://github.com/brittytino/t_launcher

### App License
MIT

### App Category
System

### App Website (if available)
https://github.com/brittytino/t_launcher

### Additional Information
- Open source, privacy-focused launcher
- No ads, no trackers
- Kotlin + Jetpack Compose
- Supports Android 6.0+
- Release APKs available on GitHub
```

3. Submit the issue
4. Wait for F-Droid maintainers to review (usually 1-4 weeks)

---

## Method 2: Fork fdroiddata Repository (Advanced)

If you want faster approval, you can create the metadata yourself.

### Step 1: Fork the Repository

1. Go to https://gitlab.com/fdroid/fdroiddata
2. Click **Fork**

### Step 2: Create App Metadata File

1. Clone your fork:
```bash
git clone https://gitlab.com/YOUR_USERNAME/fdroiddata.git
cd fdroiddata
```

2. Create metadata file:
```bash
mkdir -p metadata
```

3. Create `metadata/de.brittytino.android.launcher.yml`:

```yaml
Categories:
  - System
License: MIT
AuthorName: Tino Britty J
AuthorEmail: android-launcher@brittytino.de
AuthorWebSite: https://github.com/brittytino
SourceCode: https://github.com/brittytino/t_launcher
IssueTracker: https://github.com/brittytino/t_launcher/issues
Changelog: https://github.com/brittytino/t_launcher/releases

AutoName: T_Launcher
Description: |-
    Minimal, focus-driven Android launcher designed to eliminate distractions.
    
    Features:
    * Dynamic wallpapers with geometric patterns
    * Built-in developer tools (LeetCode integration)
    * Focus mode with app whitelisting
    * Privacy-focused (no ads, no trackers)
    * Comprehensive gesture support
    
    Built with Kotlin and Jetpack Compose.

RepoType: git
Repo: https://github.com/brittytino/t_launcher.git

Builds:
  - versionName: '1.0.3'
    versionCode: 1
    commit: v1.0.3
    gradle:
      - release

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 1.0.3
CurrentVersionCode: 1
```

4. Commit and push:
```bash
git add metadata/de.brittytino.android.launcher.yml
git commit -m "New app: T_Launcher"
git push origin main
```

5. Create Merge Request:
   - Go to your forked repository on GitLab
   - Click **Merge Requests** → **New merge request**
   - Select your branch → fdroid/fdroiddata main
   - Title: `New app: T_Launcher`
   - Description: Brief description of your app
   - Submit

---

## What Happens Next?

1. **F-Droid Review** (1-4 weeks)
   - They check your app for compliance
   - Build it themselves to verify reproducibility
   - May ask for changes

2. **Approval & Publication**
   - Once approved, your app appears in F-Droid
   - Updates are automatic when you push new tags

3. **Maintenance**
   - Push new tags to GitHub
   - F-Droid auto-updates (if AutoUpdateMode is configured)

---

## Tips for Faster Approval

✅ **Do:**
- Provide clear commit history
- Use semantic versioning (v1.0.3, v1.0.4, etc.)
- Include fastlane metadata (already done!)
- Respond quickly to maintainer questions

❌ **Avoid:**
- Binary blobs in repository
- Proprietary libraries
- Network calls without user permission
- Tracking/analytics

---

## Current Status

Your app is **ready for F-Droid submission**:

✅ Open source (MIT)  
✅ No trackers/ads  
✅ Metadata prepared  
✅ Build configuration correct  
✅ GitHub releases available  

**Next Step:** Choose Method 1 (RFP) and submit!

---

## Need Help?

- F-Droid Docs: https://f-droid.org/docs/
- F-Droid Forum: https://forum.f-droid.org/
- Matrix Chat: https://matrix.to/#/#fdroid:f-droid.org
