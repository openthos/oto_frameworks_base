/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.app;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.IContentProvider;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.FileUtils;
import android.os.IBinder;
import android.os.Process;
import android.os.Trace;
import android.os.UserHandle;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructStat;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;

import libcore.io.Memory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteOrder;

class CompatContextImpl extends ContextImpl {
    private final static String TAG = "CompatContextImpl";
    private final static boolean DEBUG = false;

    private static final String XATTR_INODE_CACHE = "user.inode_cache";
    private static final String XATTR_INODE_CODE_CACHE = "user.inode_code_cache";

    final @NonNull LoadedApk mPackageInfo;
    /**
     * Map from package name, to preference name, to cached preferences.
     */
    @GuardedBy("ContextImpl.class")
    private static ArrayMap<String, ArrayMap<File, SharedPreferencesImpl>> sSharedPrefsCache;

    static ContextImpl getImpl(Context context) {
        Context nextContext;
        while ((context instanceof ContextWrapper) &&
                (nextContext=((ContextWrapper)context).getBaseContext()) != null) {
            context = nextContext;
        }
        return (ContextImpl)context;
    }

    /**
     * Try our best to migrate all files from source to target that match
     * requested prefix.
     *
     * @return the number of files moved, or -1 if there was trouble.
     */
    private static int moveFiles(File sourceDir, File targetDir, final String prefix) {
        final File[] sourceFiles = FileUtils.listFilesOrEmpty(sourceDir, new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(prefix);
            }
        });

        int res = 0;
        for (File sourceFile : sourceFiles) {
            final File targetFile = new File(targetDir, sourceFile.getName());
            Log.d(TAG, "Migrating " + sourceFile + " to " + targetFile);
            try {
                FileUtils.copyFileOrThrow(sourceFile, targetFile);
                FileUtils.copyPermissions(sourceFile, targetFile);
                if (!sourceFile.delete()) {
                    throw new IOException("Failed to clean up " + sourceFile);
                }
                if (res != -1) {
                    res++;
                }
            } catch (IOException e) {
                Log.w(TAG, "Failed to migrate " + sourceFile + ": " + e);
                res = -1;
            }
        }
        return res;
    }

    /**
     * Common-path handling of app data dir creation
     */
    private static File ensurePrivateDirExists(File file) {
        return ensurePrivateDirExists(file, 0771, -1, null);
    }

    private static File ensurePrivateCacheDirExists(File file, String xattr) {
        final int gid = UserHandle.getCacheAppGid(Process.myUid());
        return ensurePrivateDirExists(file, 02771, gid, xattr);
    }

    private static File ensurePrivateDirExists(File file, int mode, int gid, String xattr) {
        if (!file.exists()) {
            final String path = file.getAbsolutePath();
            try {
                Os.mkdir(path, mode);
                Os.chmod(path, mode);
                if (gid != -1) {
                    Os.chown(path, -1, gid);
                }
            } catch (ErrnoException e) {
                if (e.errno == OsConstants.EEXIST) {
                    // We must have raced with someone; that's okay
                } else {
                    Log.w(TAG, "Failed to ensure " + file + ": " + e.getMessage());
                }
            }

            if (xattr != null) {
                try {
                    final StructStat stat = Os.stat(file.getAbsolutePath());
                    final byte[] value = new byte[8];
                    Memory.pokeLong(value, 0, stat.st_ino, ByteOrder.nativeOrder());
                    Os.setxattr(file.getParentFile().getAbsolutePath(), xattr, value, 0);
                } catch (ErrnoException e) {
                    Log.w(TAG, "Failed to update " + xattr + ": " + e.getMessage());
                }
            }
        }
        return file;
    }

    private static Resources createResources(IBinder activityToken, LoadedApk pi, String splitName,
            int displayId, Configuration overrideConfig, CompatibilityInfo compatInfo) {
        final String[] splitResDirs;
        final ClassLoader classLoader;
        try {
            splitResDirs = pi.getSplitPaths(splitName);
            classLoader = pi.getSplitClassLoader(splitName);
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        return ResourcesManager.getInstance().getResources(activityToken,
                pi.getResDir(),
                splitResDirs,
                pi.getOverlayDirs(),
                pi.getApplicationInfo().sharedLibraryFiles,
                displayId,
                overrideConfig,
                compatInfo,
                classLoader);
    }

    static ContextImpl createSystemContext(ActivityThread mainThread) {
        LoadedApk packageInfo = new LoadedApk(mainThread);
        ContextImpl context = new CompatContextImpl(null, mainThread, packageInfo, null, null, null, 0,
                null);
        context.setResources(packageInfo.getResources());
        context.mResources.updateConfiguration(context.mResourcesManager.getConfiguration(),
                context.mResourcesManager.getDisplayMetrics());
        return context;
    }

    /**
     * System Context to be used for UI. This Context has resources that can be themed.
     * Make sure that the created system UI context shares the same LoadedApk as the system context.
     */
    static ContextImpl createSystemUiContext(ContextImpl systemContext) {
        final LoadedApk packageInfo = systemContext.mPackageInfo;
        ContextImpl context = new CompatContextImpl(null, systemContext.mMainThread, packageInfo, null,
                null, null, 0, null);
        context.setResources(createResources(null, packageInfo, null, Display.DEFAULT_DISPLAY, null,
                packageInfo.getCompatibilityInfo()));
        return context;
    }

    static ContextImpl createAppContext(ActivityThread mainThread, LoadedApk packageInfo) {
        if (packageInfo == null) throw new IllegalArgumentException("packageInfo");
        ContextImpl context = new CompatContextImpl(null, mainThread, packageInfo, null, null, null, 0,
                null);
        context.setResources(packageInfo.getResources());
        setCompatDisplayMetrics(context.getResources());
        return context;
    }

    static ContextImpl createActivityContext(ActivityThread mainThread,
            LoadedApk packageInfo, ActivityInfo activityInfo, IBinder activityToken, int displayId,
            Configuration overrideConfiguration) {
        if (packageInfo == null) throw new IllegalArgumentException("packageInfo");

        String[] splitDirs = packageInfo.getSplitResDirs();
        ClassLoader classLoader = packageInfo.getClassLoader();

        if (packageInfo.getApplicationInfo().requestsIsolatedSplitLoading()) {
            Trace.traceBegin(Trace.TRACE_TAG_RESOURCES, "SplitDependencies");
            try {
                classLoader = packageInfo.getSplitClassLoader(activityInfo.splitName);
                splitDirs = packageInfo.getSplitPaths(activityInfo.splitName);
            } catch (NameNotFoundException e) {
                // Nothing above us can handle a NameNotFoundException, better crash.
                throw new RuntimeException(e);
            } finally {
                Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
            }
        }

        ContextImpl context = new CompatContextImpl(null, mainThread, packageInfo, activityInfo.splitName,
                activityToken, null, 0, classLoader);

        // Clamp display ID to DEFAULT_DISPLAY if it is INVALID_DISPLAY.
        displayId = (displayId != Display.INVALID_DISPLAY) ? displayId : Display.DEFAULT_DISPLAY;

        final CompatibilityInfo compatInfo = (displayId == Display.DEFAULT_DISPLAY)
                ? packageInfo.getCompatibilityInfo()
                : CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;

        final ResourcesManager resourcesManager = ResourcesManager.getInstance();

        // Create the base resources for which all configuration contexts for this Activity
        // will be rebased upon.
        Resources res = resourcesManager.createBaseActivityResources(activityToken,
                packageInfo.getResDir(),
                splitDirs,
                packageInfo.getOverlayDirs(),
                packageInfo.getApplicationInfo().sharedLibraryFiles,
                displayId,
                overrideConfiguration,
                compatInfo,
                classLoader);
        context.setResources(res);
        context.mDisplay = resourcesManager.getAdjustedDisplay(displayId,
                context.getResources());
        setCompatDisplayMetrics(context.getResources());
        return context;
    }

    @SuppressWarnings("deprecation")
    static void setFilePermissionsFromMode(String name, int mode,
            int extraPermissions) {
        int perms = FileUtils.S_IRUSR|FileUtils.S_IWUSR
            |FileUtils.S_IRGRP|FileUtils.S_IWGRP
            |extraPermissions;
        if ((mode&MODE_WORLD_READABLE) != 0) {
            perms |= FileUtils.S_IROTH;
        }
        if ((mode&MODE_WORLD_WRITEABLE) != 0) {
            perms |= FileUtils.S_IWOTH;
        }
        if (DEBUG) {
            Log.i(TAG, "File " + name + ": mode=0x" + Integer.toHexString(mode)
                  + ", perms=0x" + Integer.toHexString(perms));
        }
        FileUtils.setPermissions(name, perms, -1, -1);
    }

    public CompatContextImpl(@Nullable CompatContextImpl container, @NonNull ActivityThread mainThread,
            @NonNull LoadedApk packageInfo, @Nullable String splitName,
            @Nullable IBinder activityToken, @Nullable UserHandle user, int flags,
            @Nullable ClassLoader classLoader) {
        super(container, mainThread, packageInfo, splitName, activityToken, user, flags, classLoader);
        mPackageInfo = packageInfo;
    }

    @Override
    public Context createApplicationContext(ApplicationInfo application, int flags)
            throws NameNotFoundException {
        LoadedApk pi = mMainThread.getPackageInfo(application, mResources.getCompatibilityInfo(),
                flags | CONTEXT_REGISTER_PACKAGE);
        if (pi != null) {
            ContextImpl c = new CompatContextImpl(this, mMainThread, pi, null, mActivityToken,
                    new UserHandle(UserHandle.getUserId(application.uid)), flags, null);

            final int displayId = mDisplay != null
                    ? mDisplay.getDisplayId() : Display.DEFAULT_DISPLAY;

            Resources res = createResources(mActivityToken, pi, null, displayId, null,
                    getDisplayAdjustments(displayId).getCompatibilityInfo());
            c.setResources(res);
            if (c.mResources != null) {
                c.mResources.getDisplayMetrics().setCompatMetrics();
                return c;
            }
        }

        throw new PackageManager.NameNotFoundException(
                "Application package " + application.packageName + " not found");
    }

    @Override
    public Context createPackageContextAsUser(String packageName, int flags, UserHandle user)
            throws NameNotFoundException {
        if (packageName.equals("system") || packageName.equals("android")) {
            // The system resources are loaded in every application, so we can safely copy
            // the context without reloading Resources.
            return new CompatContextImpl(this, mMainThread, mPackageInfo, null, mActivityToken, user,
                    flags, null);
        }

        LoadedApk pi = mMainThread.getPackageInfo(packageName, mResources.getCompatibilityInfo(),
                flags | CONTEXT_REGISTER_PACKAGE, user.getIdentifier());
        if (pi != null) {
            ContextImpl c = new CompatContextImpl(this, mMainThread, pi, null, mActivityToken, user,
                    flags, null);

            final int displayId = mDisplay != null
                    ? mDisplay.getDisplayId() : Display.DEFAULT_DISPLAY;

            Resources res = createResources(mActivityToken, pi, null, displayId, null,
                    getDisplayAdjustments(displayId).getCompatibilityInfo());
            c.setResources(res);
            if (c.mResources != null) {
                return c;
            }
        }

        // Should be a better exception.
        throw new PackageManager.NameNotFoundException(
                "Application package " + packageName + " not found");
    }

    @Override
    public Context createContextForSplit(String splitName) throws NameNotFoundException {
        if (!mPackageInfo.getApplicationInfo().requestsIsolatedSplitLoading()) {
            // All Splits are always loaded.
            return this;
        }

        final ClassLoader classLoader = mPackageInfo.getSplitClassLoader(splitName);
        final String[] paths = mPackageInfo.getSplitPaths(splitName);

        final ContextImpl context = new CompatContextImpl(this, mMainThread, mPackageInfo, splitName,
                mActivityToken, mUser, mFlags, classLoader);

        final int displayId = mDisplay != null
                ? mDisplay.getDisplayId() : Display.DEFAULT_DISPLAY;

        context.setResources(ResourcesManager.getInstance().getResources(
                mActivityToken,
                mPackageInfo.getResDir(),
                paths,
                mPackageInfo.getOverlayDirs(),
                mPackageInfo.getApplicationInfo().sharedLibraryFiles,
                displayId,
                null,
                mPackageInfo.getCompatibilityInfo(),
                classLoader));
        return context;
    }

    @Override
    public Context createConfigurationContext(Configuration overrideConfiguration) {
        if (overrideConfiguration == null) {
            throw new IllegalArgumentException("overrideConfiguration must not be null");
        }

        ContextImpl context = new CompatContextImpl(this, mMainThread, mPackageInfo, mSplitName,
                mActivityToken, mUser, mFlags, mClassLoader);

        final int displayId = mDisplay != null ? mDisplay.getDisplayId() : Display.DEFAULT_DISPLAY;
        context.setResources(createResources(mActivityToken, mPackageInfo, mSplitName, displayId,
                overrideConfiguration, getDisplayAdjustments(displayId).getCompatibilityInfo()));
        return context;
    }

    @Override
    public Context createDisplayContext(Display display) {
        if (display == null) {
            throw new IllegalArgumentException("display must not be null");
        }

        ContextImpl context = new CompatContextImpl(this, mMainThread, mPackageInfo, mSplitName,
                mActivityToken, mUser, mFlags, mClassLoader);

        final int displayId = display.getDisplayId();
        context.setResources(createResources(mActivityToken, mPackageInfo, mSplitName, displayId,
                null, getDisplayAdjustments(displayId).getCompatibilityInfo()));
        context.mDisplay = display;
        return context;
    }

    @Override
    public Context createDeviceProtectedStorageContext() {
        final int flags = (mFlags & ~Context.CONTEXT_CREDENTIAL_PROTECTED_STORAGE)
                | Context.CONTEXT_DEVICE_PROTECTED_STORAGE;
        return new CompatContextImpl(this, mMainThread, mPackageInfo, mSplitName, mActivityToken, mUser,
                flags, mClassLoader);
    }

    @Override
    public Context createCredentialProtectedStorageContext() {
        final int flags = (mFlags & ~Context.CONTEXT_DEVICE_PROTECTED_STORAGE)
                | Context.CONTEXT_CREDENTIAL_PROTECTED_STORAGE;
        return new CompatContextImpl(this, mMainThread, mPackageInfo, mSplitName, mActivityToken, mUser,
                flags, mClassLoader);
    }

    @Override
    public Resources getResources() {
        setCompatDisplayMetrics(mResources);
        return mResources;
    }

    @Override
    public Display getDisplay() {
        if (mDisplay == null) {
            Display display = mResourcesManager.
                    getAdjustedDisplay(Display.DEFAULT_DISPLAY, mResources);
            display.setCompatDisplayInfo(true);
            return display;
        }
        mDisplay.setCompatDisplayInfo(true);
        return mDisplay;
    }

    @Override
    public boolean isCompatContext() {
        return true;
    }

    static void setCompatDisplayMetrics(Resources res) {
        DisplayMetrics dm = res.getDisplayMetrics();
        dm.setCompatMetrics();
        res.getDisplayMetrics().setTo(dm);
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    private static final class ApplicationContentResolver extends ContentResolver {
        private final ActivityThread mMainThread;
        private final UserHandle mUser;

        public ApplicationContentResolver(
                Context context, ActivityThread mainThread, UserHandle user) {
            super(context);
            mMainThread = Preconditions.checkNotNull(mainThread);
            mUser = Preconditions.checkNotNull(user);
        }

        @Override
        protected IContentProvider acquireProvider(Context context, String auth) {
            return mMainThread.acquireProvider(context,
                    ContentProvider.getAuthorityWithoutUserId(auth),
                    resolveUserIdFromAuthority(auth), true);
        }

        @Override
        protected IContentProvider acquireExistingProvider(Context context, String auth) {
            return mMainThread.acquireExistingProvider(context,
                    ContentProvider.getAuthorityWithoutUserId(auth),
                    resolveUserIdFromAuthority(auth), true);
        }

        @Override
        public boolean releaseProvider(IContentProvider provider) {
            return mMainThread.releaseProvider(provider, true);
        }

        @Override
        protected IContentProvider acquireUnstableProvider(Context c, String auth) {
            return mMainThread.acquireProvider(c,
                    ContentProvider.getAuthorityWithoutUserId(auth),
                    resolveUserIdFromAuthority(auth), false);
        }

        @Override
        public boolean releaseUnstableProvider(IContentProvider icp) {
            return mMainThread.releaseProvider(icp, false);
        }

        @Override
        public void unstableProviderDied(IContentProvider icp) {
            mMainThread.handleUnstableProviderDied(icp.asBinder(), true);
        }

        @Override
        public void appNotRespondingViaProvider(IContentProvider icp) {
            mMainThread.appNotRespondingViaProvider(icp.asBinder());
        }

        /** @hide */
        protected int resolveUserIdFromAuthority(String auth) {
            return ContentProvider.getUserIdFromAuthority(auth, mUser.getIdentifier());
        }
    }
}
