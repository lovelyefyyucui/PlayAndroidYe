package com.yfy.core.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.AnimRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import com.yfy.core.view.base.ActivityCollector;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Activity工具
 * 日期： 2024年01月03日 10:23
 * 签名： 天行健，君子以自强不息；地势坤，君子以厚德载物。
 * _              _           _     _   ____  _             _ _
 * / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 * / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 * / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 * /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/  -- 志威 yfy
 * <p>
 * You never know what you can do until you try !
 * ----------------------------------------------------------------
 */
public final class ActivityUtil {

    private ActivityUtil() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * Return the activity by context.
     *
     * @param context The context.
     * @return the activity by context.
     */
    @Nullable
    public static Activity getActivityByContext(@Nullable Context context) {
        if (context == null) return null;
        Activity activity = getActivityByContextInner(context);
        if (!isActivityAlive(activity)) return null;
        return activity;
    }

    @Nullable
    private static Activity getActivityByContextInner(@Nullable Context context) {
        if (context == null) return null;
        List<Context> list = new ArrayList<>();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            Activity activity = getActivityFromDecorContext(context);
            if (activity != null) return activity;
            list.add(context);
            context = ((ContextWrapper) context).getBaseContext();
            if (context == null) {
                return null;
            }
            if (list.contains(context)) {
                // loop context
                return null;
            }
        }
        return null;
    }

    @Nullable
    private static Activity getActivityFromDecorContext(@Nullable Context context) {
        if (context == null) return null;
        if (context.getClass().getName().equals("com.android.internal.policy.DecorContext")) {
            try {
                Field mActivityContextField = context.getClass().getDeclaredField("mActivityContext");
                mActivityContextField.setAccessible(true);
                //noinspection ConstantConditions,unchecked
                return ((WeakReference<Activity>) mActivityContextField.get(context)).get();
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    /**
     * Return whether the activity exists.
     *
     * @param pkg The name of the package.
     * @param cls The name of the class.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isActivityExists(@NonNull final String pkg,
                                           @NonNull final String cls) {
        Intent intent = new Intent();
        intent.setClassName(pkg, cls);
        PackageManager pm = Util.getApp().getPackageManager();
        return !(pm.resolveActivity(intent, 0) == null ||
                intent.resolveActivity(pm) == null ||
                pm.queryIntentActivities(intent, 0).size() == 0);
    }

    /**
     * Start the activity.
     *
     * @param clz The activity class.
     */
    public static void startActivity(@NonNull final Class<? extends Activity> clz) {
        Context context = getTopActivityOrApp();
        startActivity(context, null, context.getPackageName(), clz.getName(), null);
    }

    /**
     * Start the activity.
     *
     * @param clz     The activity class.
     * @param options Additional options for how the Activity should be started.
     */
    public static void startActivity(@NonNull final Class<? extends Activity> clz,
                                     @Nullable final Bundle options) {
        Context context = getTopActivityOrApp();
        startActivity(context, null, context.getPackageName(), clz.getName(), options);
    }

    /**
     * Start the activity.
     *
     * @param clz       The activity class.
     * @param enterAnim A resource ID of the animation resource to use for the
     *                  incoming activity.
     * @param exitAnim  A resource ID of the animation resource to use for the
     *                  outgoing activity.
     */
    public static void startActivity(@NonNull final Class<? extends Activity> clz,
                                     @AnimRes final int enterAnim,
                                     @AnimRes final int exitAnim) {
        Context context = getTopActivityOrApp();
        startActivity(context, null, context.getPackageName(), clz.getName(),
                getOptionsBundle(context, enterAnim, exitAnim));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && context instanceof Activity) {
            ((Activity) context).overridePendingTransition(enterAnim, exitAnim);
        }
    }

    /**
     * Start the activity.
     *
     * @param activity The activity.
     * @param clz      The activity class.
     */
    public static void startActivity(@NonNull final Activity activity,
                                     @NonNull final Class<? extends Activity> clz) {
        startActivity(activity, null, activity.getPackageName(), clz.getName(), null);
    }

    /**
     * Start the activity.
     *
     * @param activity The activity.
     * @param clz      The activity class.
     * @param options  Additional options for how the Activity should be started.
     */
    public static void startActivity(@NonNull final Activity activity,
                                     @NonNull final Class<? extends Activity> clz,
                                     @Nullable final Bundle options) {
        startActivity(activity, null, activity.getPackageName(), clz.getName(), options);
    }

    /**
     * Start the activity.
     *
     * @param activity       The activity.
     * @param clz            The activity class.
     * @param sharedElements The names of the shared elements to transfer to the called
     *                       Activity and their associated Views.
     */
    public static void startActivity(@NonNull final Activity activity,
                                     @NonNull final Class<? extends Activity> clz,
                                     final View... sharedElements) {
        startActivity(activity, null, activity.getPackageName(), clz.getName(),
                getOptionsBundle(activity, sharedElements));
    }

    /**
     * Start the activity.
     *
     * @param activity  The activity.
     * @param clz       The activity class.
     * @param enterAnim A resource ID of the animation resource to use for the
     *                  incoming activity.
     * @param exitAnim  A resource ID of the animation resource to use for the
     *                  outgoing activity.
     */
    public static void startActivity(@NonNull final Activity activity,
                                     @NonNull final Class<? extends Activity> clz,
                                     @AnimRes final int enterAnim,
                                     @AnimRes final int exitAnim) {
        startActivity(activity, null, activity.getPackageName(), clz.getName(),
                getOptionsBundle(activity, enterAnim, exitAnim));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            activity.overridePendingTransition(enterAnim, exitAnim);
        }
    }

    /**
     * Start the activity.
     *
     * @param extras The Bundle of extras to add to this intent.
     * @param clz    The activity class.
     */
    public static void startActivity(@NonNull final Bundle extras,
                                     @NonNull final Class<? extends Activity> clz) {
        Context context = getTopActivityOrApp();
        startActivity(context, extras, context.getPackageName(), clz.getName(), null);
    }

    /**
     * Start the activity.
     *
     * @param extras  The Bundle of extras to add to this intent.
     * @param clz     The activity class.
     * @param options Additional options for how the Activity should be started.
     */
    public static void startActivity(@NonNull final Bundle extras,
                                     @NonNull final Class<? extends Activity> clz,
                                     @Nullable final Bundle options) {
        Context context = getTopActivityOrApp();
        startActivity(context, extras, context.getPackageName(), clz.getName(), options);
    }

    /**
     * Start the activity.
     *
     * @param extras    The Bundle of extras to add to this intent.
     * @param clz       The activity class.
     * @param enterAnim A resource ID of the animation resource to use for the
     *                  incoming activity.
     * @param exitAnim  A resource ID of the animation resource to use for the
     *                  outgoing activity.
     */
    public static void startActivity(@NonNull final Bundle extras,
                                     @NonNull final Class<? extends Activity> clz,
                                     @AnimRes final int enterAnim,
                                     @AnimRes final int exitAnim) {
        Context context = getTopActivityOrApp();
        startActivity(context, extras, context.getPackageName(), clz.getName(),
                getOptionsBundle(context, enterAnim, exitAnim));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && context instanceof Activity) {
            ((Activity) context).overridePendingTransition(enterAnim, exitAnim);
        }
    }

    /**
     * Start the activity.
     *
     * @param extras   The Bundle of extras to add to this intent.
     * @param activity The activity.
     * @param clz      The activity class.
     */
    public static void startActivity(@NonNull final Bundle extras,
                                     @NonNull final Activity activity,
                                     @NonNull final Class<? extends Activity> clz) {
        startActivity(activity, extras, activity.getPackageName(), clz.getName(), null);
    }

    /**
     * Start the activity.
     *
     * @param extras   The Bundle of extras to add to this intent.
     * @param activity The activity.
     * @param clz      The activity class.
     * @param options  Additional options for how the Activity should be started.
     */
    public static void startActivity(@NonNull final Bundle extras,
                                     @NonNull final Activity activity,
                                     @NonNull final Class<? extends Activity> clz,
                                     @Nullable final Bundle options) {
        startActivity(activity, extras, activity.getPackageName(), clz.getName(), options);
    }

    /**
     * Start the activity.
     *
     * @param extras         The Bundle of extras to add to this intent.
     * @param activity       The activity.
     * @param clz            The activity class.
     * @param sharedElements The names of the shared elements to transfer to the called
     *                       Activity and their associated Views.
     */
    public static void startActivity(@NonNull final Bundle extras,
                                     @NonNull final Activity activity,
                                     @NonNull final Class<? extends Activity> clz,
                                     final View... sharedElements) {
        startActivity(activity, extras, activity.getPackageName(), clz.getName(),
                getOptionsBundle(activity, sharedElements));
    }

    /**
     * Start the activity.
     *
     * @param extras    The Bundle of extras to add to this intent.
     * @param activity  The activity.
     * @param clz       The activity class.
     * @param enterAnim A resource ID of the animation resource to use for the
     *                  incoming activity.
     * @param exitAnim  A resource ID of the animation resource to use for the
     *                  outgoing activity.
     */
    public static void startActivity(@NonNull final Bundle extras,
                                     @NonNull final Activity activity,
                                     @NonNull final Class<? extends Activity> clz,
                                     @AnimRes final int enterAnim,
                                     @AnimRes final int exitAnim) {
        startActivity(activity, extras, activity.getPackageName(), clz.getName(),
                getOptionsBundle(activity, enterAnim, exitAnim));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            activity.overridePendingTransition(enterAnim, exitAnim);
        }
    }

    /**
     * Start the activity.
     *
     * @param pkg The name of the package.
     * @param cls The name of the class.
     */
    public static void startActivity(@NonNull final String pkg,
                                     @NonNull final String cls) {
        startActivity(getTopActivityOrApp(), null, pkg, cls, null);
    }

    /**
     * Start the activity.
     *
     * @param pkg     The name of the package.
     * @param cls     The name of the class.
     * @param options Additional options for how the Activity should be started.
     */
    public static void startActivity(@NonNull final String pkg,
                                     @NonNull final String cls,
                                     @Nullable final Bundle options) {
        startActivity(getTopActivityOrApp(), null, pkg, cls, options);
    }

    /**
     * Start the activity.
     *
     * @param pkg       The name of the package.
     * @param cls       The name of the class.
     * @param enterAnim A resource ID of the animation resource to use for the
     *                  incoming activity.
     * @param exitAnim  A resource ID of the animation resource to use for the
     *                  outgoing activity.
     */
    public static void startActivity(@NonNull final String pkg,
                                     @NonNull final String cls,
                                     @AnimRes final int enterAnim,
                                     @AnimRes final int exitAnim) {
        Context context = getTopActivityOrApp();
        startActivity(context, null, pkg, cls, getOptionsBundle(context, enterAnim, exitAnim));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && context instanceof Activity) {
            ((Activity) context).overridePendingTransition(enterAnim, exitAnim);
        }
    }

    /**
     * Start the activity.
     *
     * @param activity The activity.
     * @param pkg      The name of the package.
     * @param cls      The name of the class.
     */
    public static void startActivity(@NonNull final Activity activity,
                                     @NonNull final String pkg,
                                     @NonNull final String cls) {
        startActivity(activity, null, pkg, cls, null);
    }

    /**
     * Start the activity.
     *
     * @param activity The activity.
     * @param pkg      The name of the package.
     * @param cls      The name of the class.
     * @param options  Additional options for how the Activity should be started.
     */
    public static void startActivity(@NonNull final Activity activity,
                                     @NonNull final String pkg,
                                     @NonNull final String cls,
                                     @Nullable final Bundle options) {
        startActivity(activity, null, pkg, cls, options);
    }

    /**
     * Start the activity.
     *
     * @param activity       The activity.
     * @param pkg            The name of the package.
     * @param cls            The name of the class.
     * @param sharedElements The names of the shared elements to transfer to the called
     *                       Activity and their associated Views.
     */
    public static void startActivity(@NonNull final Activity activity,
                                     @NonNull final String pkg,
                                     @NonNull final String cls,
                                     final View... sharedElements) {
        startActivity(activity, null, pkg, cls, getOptionsBundle(activity, sharedElements));
    }

    /**
     * Start the activity.
     *
     * @param activity  The activity.
     * @param pkg       The name of the package.
     * @param cls       The name of the class.
     * @param enterAnim A resource ID of the animation resource to use for the
     *                  incoming activity.
     * @param exitAnim  A resource ID of the animation resource to use for the
     *                  outgoing activity.
     */
    public static void startActivity(@NonNull final Activity activity,
                                     @NonNull final String pkg,
                                     @NonNull final String cls,
                                     @AnimRes final int enterAnim,
                                     @AnimRes final int exitAnim) {
        startActivity(activity, null, pkg, cls, getOptionsBundle(activity, enterAnim, exitAnim));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            activity.overridePendingTransition(enterAnim, exitAnim);
        }
    }

    /**
     * Start the activity.
     *
     * @param extras The Bundle of extras to add to this intent.
     * @param pkg    The name of the package.
     * @param cls    The name of the class.
     */
    public static void startActivity(@NonNull final Bundle extras,
                                     @NonNull final String pkg,
                                     @NonNull final String cls) {
        startActivity(getTopActivityOrApp(), extras, pkg, cls, null);
    }

    /**
     * Start the activity.
     *
     * @param extras  The Bundle of extras to add to this intent.
     * @param pkg     The name of the package.
     * @param cls     The name of the class.
     * @param options Additional options for how the Activity should be started.
     */
    public static void startActivity(@NonNull final Bundle extras,
                                     @NonNull final String pkg,
                                     @NonNull final String cls,
                                     @Nullable final Bundle options) {
        startActivity(getTopActivityOrApp(), extras, pkg, cls, options);
    }

    /**
     * Start the activity.
     *
     * @param extras    The Bundle of extras to add to this intent.
     * @param pkg       The name of the package.
     * @param cls       The name of the class.
     * @param enterAnim A resource ID of the animation resource to use for the
     *                  incoming activity.
     * @param exitAnim  A resource ID of the animation resource to use for the
     *                  outgoing activity.
     */
    public static void startActivity(@NonNull final Bundle extras,
                                     @NonNull final String pkg,
                                     @NonNull final String cls,
                                     @AnimRes final int enterAnim,
                                     @AnimRes final int exitAnim) {
        Context context = getTopActivityOrApp();
        startActivity(context, extras, pkg, cls, getOptionsBundle(context, enterAnim, exitAnim));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && context instanceof Activity) {
            ((Activity) context).overridePendingTransition(enterAnim, exitAnim);
        }
    }

    /**
     * Start the activity.
     *
     * @param activity The activity.
     * @param extras   The Bundle of extras to add to this intent.
     * @param pkg      The name of the package.
     * @param cls      The name of the class.
     */
    public static void startActivity(@NonNull final Bundle extras,
                                     @NonNull final Activity activity,
                                     @NonNull final String pkg,
                                     @NonNull final String cls) {
        startActivity(activity, extras, pkg, cls, null);
    }

    /**
     * Start the activity.
     *
     * @param extras   The Bundle of extras to add to this intent.
     * @param activity The activity.
     * @param pkg      The name of the package.
     * @param cls      The name of the class.
     * @param options  Additional options for how the Activity should be started.
     */
    public static void startActivity(@NonNull final Bundle extras,
                                     @NonNull final Activity activity,
                                     @NonNull final String pkg,
                                     @NonNull final String cls,
                                     @Nullable final Bundle options) {
        startActivity(activity, extras, pkg, cls, options);
    }

    /**
     * Start the activity.
     *
     * @param extras         The Bundle of extras to add to this intent.
     * @param activity       The activity.
     * @param pkg            The name of the package.
     * @param cls            The name of the class.
     * @param sharedElements The names of the shared elements to transfer to the called
     *                       Activity and their associated Views.
     */
    public static void startActivity(@NonNull final Bundle extras,
                                     @NonNull final Activity activity,
                                     @NonNull final String pkg,
                                     @NonNull final String cls,
                                     final View... sharedElements) {
        startActivity(activity, extras, pkg, cls, getOptionsBundle(activity, sharedElements));
    }

    /**
     * Start the activity.
     *
     * @param extras    The Bundle of extras to add to this intent.
     * @param pkg       The name of the package.
     * @param cls       The name of the class.
     * @param enterAnim A resource ID of the animation resource to use for the
     *                  incoming activity.
     * @param exitAnim  A resource ID of the animation resource to use for the
     *                  outgoing activity.
     */
    public static void startActivity(@NonNull final Bundle extras,
                                     @NonNull final Activity activity,
                                     @NonNull final String pkg,
                                     @NonNull final String cls,
                                     @AnimRes final int enterAnim,
                                     @AnimRes final int exitAnim) {
        startActivity(activity, extras, pkg, cls, getOptionsBundle(activity, enterAnim, exitAnim));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            activity.overridePendingTransition(enterAnim, exitAnim);
        }
    }

    /**
     * Start the activity.
     *
     * @param intent The description of the activity to start.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean startActivity(@NonNull final Intent intent) {
        return startActivity(intent, getTopActivityOrApp(), null);
    }

    /**
     * Start the activity.
     *
     * @param intent  The description of the activity to start.
     * @param options Additional options for how the Activity should be started.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean startActivity(@NonNull final Intent intent,
                                        @Nullable final Bundle options) {
        return startActivity(intent, getTopActivityOrApp(), options);
    }

    /**
     * Start the activity.
     *
     * @param intent    The description of the activity to start.
     * @param enterAnim A resource ID of the animation resource to use for the
     *                  incoming activity.
     * @param exitAnim  A resource ID of the animation resource to use for the
     *                  outgoing activity.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean startActivity(@NonNull final Intent intent,
                                        @AnimRes final int enterAnim,
                                        @AnimRes final int exitAnim) {
        Context context = getTopActivityOrApp();
        boolean isSuccess = startActivity(intent, context, getOptionsBundle(context, enterAnim, exitAnim));
        if (isSuccess) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && context instanceof Activity) {
                ((Activity) context).overridePendingTransition(enterAnim, exitAnim);
            }
        }
        return isSuccess;
    }

    /**
     * Start the activity.
     *
     * @param activity The activity.
     * @param intent   The description of the activity to start.
     */
    public static void startActivity(@NonNull final Activity activity,
                                     @NonNull final Intent intent) {
        startActivity(intent, activity, null);
    }

    /**
     * Start the activity.
     *
     * @param activity The activity.
     * @param intent   The description of the activity to start.
     * @param options  Additional options for how the Activity should be started.
     */
    public static void startActivity(@NonNull final Activity activity,
                                     @NonNull final Intent intent,
                                     @Nullable final Bundle options) {
        startActivity(intent, activity, options);
    }

    /**
     * Start the activity.
     *
     * @param activity       The activity.
     * @param intent         The description of the activity to start.
     * @param sharedElements The names of the shared elements to transfer to the called
     *                       Activity and their associated Views.
     */
    public static void startActivity(@NonNull final Activity activity,
                                     @NonNull final Intent intent,
                                     final View... sharedElements) {
        startActivity(intent, activity, getOptionsBundle(activity, sharedElements));
    }

    /**
     * Start the activity.
     *
     * @param activity  The activity.
     * @param intent    The description of the activity to start.
     * @param enterAnim A resource ID of the animation resource to use for the
     *                  incoming activity.
     * @param exitAnim  A resource ID of the animation resource to use for the
     *                  outgoing activity.
     */
    public static void startActivity(@NonNull final Activity activity,
                                     @NonNull final Intent intent,
                                     @AnimRes final int enterAnim,
                                     @AnimRes final int exitAnim) {
        startActivity(intent, activity, getOptionsBundle(activity, enterAnim, exitAnim));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            activity.overridePendingTransition(enterAnim, exitAnim);
        }
    }

    /**
     * Start the activity.
     *
     * @param activity    The activity.
     * @param clz         The activity class.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     */
    public static void startActivityForResult(@NonNull final Activity activity,
                                              @NonNull final Class<? extends Activity> clz,
                                              final int requestCode) {
        startActivityForResult(activity, null, activity.getPackageName(), clz.getName(),
                requestCode, null);
    }

    /**
     * Start the activity.
     *
     * @param activity    The activity.
     * @param clz         The activity class.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     * @param options     Additional options for how the Activity should be started.
     */
    public static void startActivityForResult(@NonNull final Activity activity,
                                              @NonNull final Class<? extends Activity> clz,
                                              final int requestCode,
                                              @Nullable final Bundle options) {
        startActivityForResult(activity, null, activity.getPackageName(), clz.getName(),
                requestCode, options);
    }

    /**
     * Start the activity.
     *
     * @param activity       The activity.
     * @param clz            The activity class.
     * @param requestCode    if &gt;= 0, this code will be returned in
     *                       onActivityResult() when the activity exits.
     * @param sharedElements The names of the shared elements to transfer to the called
     *                       Activity and their associated Views.
     */
    public static void startActivityForResult(@NonNull final Activity activity,
                                              @NonNull final Class<? extends Activity> clz,
                                              final int requestCode,
                                              final View... sharedElements) {
        startActivityForResult(activity, null, activity.getPackageName(), clz.getName(),
                requestCode, getOptionsBundle(activity, sharedElements));
    }

    /**
     * Start the activity.
     *
     * @param activity    The activity.
     * @param clz         The activity class.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     * @param enterAnim   A resource ID of the animation resource to use for the
     *                    incoming activity.
     * @param exitAnim    A resource ID of the animation resource to use for the
     *                    outgoing activity.
     */
    public static void startActivityForResult(@NonNull final Activity activity,
                                              @NonNull final Class<? extends Activity> clz,
                                              final int requestCode,
                                              @AnimRes final int enterAnim,
                                              @AnimRes final int exitAnim) {
        startActivityForResult(activity, null, activity.getPackageName(), clz.getName(),
                requestCode, getOptionsBundle(activity, enterAnim, exitAnim));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            activity.overridePendingTransition(enterAnim, exitAnim);
        }
    }

    /**
     * Start the activity.
     *
     * @param extras      The Bundle of extras to add to this intent.
     * @param activity    The activity.
     * @param clz         The activity class.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     */
    public static void startActivityForResult(@NonNull final Bundle extras,
                                              @NonNull final Activity activity,
                                              @NonNull final Class<? extends Activity> clz,
                                              final int requestCode) {
        startActivityForResult(activity, extras, activity.getPackageName(), clz.getName(),
                requestCode, null);
    }

    /**
     * Start the activity.
     *
     * @param extras      The Bundle of extras to add to this intent.
     * @param activity    The activity.
     * @param clz         The activity class.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     * @param options     Additional options for how the Activity should be started.
     */
    public static void startActivityForResult(@NonNull final Bundle extras,
                                              @NonNull final Activity activity,
                                              @NonNull final Class<? extends Activity> clz,
                                              final int requestCode,
                                              @Nullable final Bundle options) {
        startActivityForResult(activity, extras, activity.getPackageName(), clz.getName(),
                requestCode, options);
    }

    /**
     * Start the activity.
     *
     * @param extras         The Bundle of extras to add to this intent.
     * @param activity       The activity.
     * @param clz            The activity class.
     * @param requestCode    if &gt;= 0, this code will be returned in
     *                       onActivityResult() when the activity exits.
     * @param sharedElements The names of the shared elements to transfer to the called
     *                       Activity and their associated Views.
     */
    public static void startActivityForResult(@NonNull final Bundle extras,
                                              @NonNull final Activity activity,
                                              @NonNull final Class<? extends Activity> clz,
                                              final int requestCode,
                                              final View... sharedElements) {
        startActivityForResult(activity, extras, activity.getPackageName(), clz.getName(),
                requestCode, getOptionsBundle(activity, sharedElements));
    }

    /**
     * Start the activity.
     *
     * @param extras      The Bundle of extras to add to this intent.
     * @param activity    The activity.
     * @param clz         The activity class.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     * @param enterAnim   A resource ID of the animation resource to use for the
     *                    incoming activity.
     * @param exitAnim    A resource ID of the animation resource to use for the
     *                    outgoing activity.
     */
    public static void startActivityForResult(@NonNull final Bundle extras,
                                              @NonNull final Activity activity,
                                              @NonNull final Class<? extends Activity> clz,
                                              final int requestCode,
                                              @AnimRes final int enterAnim,
                                              @AnimRes final int exitAnim) {
        startActivityForResult(activity, extras, activity.getPackageName(), clz.getName(),
                requestCode, getOptionsBundle(activity, enterAnim, exitAnim));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            activity.overridePendingTransition(enterAnim, exitAnim);
        }
    }

    /**
     * Start the activity for result.
     *
     * @param activity    The activity.
     * @param extras      The Bundle of extras to add to this intent.
     * @param pkg         The name of the package.
     * @param cls         The name of the class.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     */
    public static void startActivityForResult(@NonNull final Bundle extras,
                                              @NonNull final Activity activity,
                                              @NonNull final String pkg,
                                              @NonNull final String cls,
                                              final int requestCode) {
        startActivityForResult(activity, extras, pkg, cls, requestCode, null);
    }

    /**
     * Start the activity for result.
     *
     * @param extras      The Bundle of extras to add to this intent.
     * @param activity    The activity.
     * @param pkg         The name of the package.
     * @param cls         The name of the class.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     * @param options     Additional options for how the Activity should be started.
     */
    public static void startActivityForResult(@NonNull final Bundle extras,
                                              @NonNull final Activity activity,
                                              @NonNull final String pkg,
                                              @NonNull final String cls,
                                              final int requestCode,
                                              @Nullable final Bundle options) {
        startActivityForResult(activity, extras, pkg, cls, requestCode, options);
    }

    /**
     * Start the activity for result.
     *
     * @param extras         The Bundle of extras to add to this intent.
     * @param activity       The activity.
     * @param pkg            The name of the package.
     * @param cls            The name of the class.
     * @param requestCode    if &gt;= 0, this code will be returned in
     *                       onActivityResult() when the activity exits.
     * @param sharedElements The names of the shared elements to transfer to the called
     *                       Activity and their associated Views.
     */
    public static void startActivityForResult(@NonNull final Bundle extras,
                                              @NonNull final Activity activity,
                                              @NonNull final String pkg,
                                              @NonNull final String cls,
                                              final int requestCode,
                                              final View... sharedElements) {
        startActivityForResult(activity, extras, pkg, cls,
                requestCode, getOptionsBundle(activity, sharedElements));
    }

    /**
     * Start the activity for result.
     *
     * @param extras      The Bundle of extras to add to this intent.
     * @param pkg         The name of the package.
     * @param cls         The name of the class.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     * @param enterAnim   A resource ID of the animation resource to use for the
     *                    incoming activity.
     * @param exitAnim    A resource ID of the animation resource to use for the
     *                    outgoing activity.
     */
    public static void startActivityForResult(@NonNull final Bundle extras,
                                              @NonNull final Activity activity,
                                              @NonNull final String pkg,
                                              @NonNull final String cls,
                                              final int requestCode,
                                              @AnimRes final int enterAnim,
                                              @AnimRes final int exitAnim) {
        startActivityForResult(activity, extras, pkg, cls,
                requestCode, getOptionsBundle(activity, enterAnim, exitAnim));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            activity.overridePendingTransition(enterAnim, exitAnim);
        }
    }

    /**
     * Start the activity for result.
     *
     * @param activity    The activity.
     * @param intent      The description of the activity to start.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     */
    public static void startActivityForResult(@NonNull final Activity activity,
                                              @NonNull final Intent intent,
                                              final int requestCode) {
        startActivityForResult(intent, activity, requestCode, null);
    }

    /**
     * Start the activity for result.
     *
     * @param activity    The activity.
     * @param intent      The description of the activity to start.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     * @param options     Additional options for how the Activity should be started.
     */
    public static void startActivityForResult(@NonNull final Activity activity,
                                              @NonNull final Intent intent,
                                              final int requestCode,
                                              @Nullable final Bundle options) {
        startActivityForResult(intent, activity, requestCode, options);
    }

    /**
     * Start the activity for result.
     *
     * @param activity       The activity.
     * @param intent         The description of the activity to start.
     * @param requestCode    if &gt;= 0, this code will be returned in
     *                       onActivityResult() when the activity exits.
     * @param sharedElements The names of the shared elements to transfer to the called
     *                       Activity and their associated Views.
     */
    public static void startActivityForResult(@NonNull final Activity activity,
                                              @NonNull final Intent intent,
                                              final int requestCode,
                                              final View... sharedElements) {
        startActivityForResult(intent, activity,
                requestCode, getOptionsBundle(activity, sharedElements));
    }

    /**
     * Start the activity for result.
     *
     * @param activity    The activity.
     * @param intent      The description of the activity to start.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     * @param enterAnim   A resource ID of the animation resource to use for the
     *                    incoming activity.
     * @param exitAnim    A resource ID of the animation resource to use for the
     *                    outgoing activity.
     */
    public static void startActivityForResult(@NonNull final Activity activity,
                                              @NonNull final Intent intent,
                                              final int requestCode,
                                              @AnimRes final int enterAnim,
                                              @AnimRes final int exitAnim) {
        startActivityForResult(intent, activity,
                requestCode, getOptionsBundle(activity, enterAnim, exitAnim));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            activity.overridePendingTransition(enterAnim, exitAnim);
        }
    }

    /**
     * Start the activity.
     *
     * @param fragment    The fragment.
     * @param clz         The activity class.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     */
    public static void startActivityForResult(@NonNull final Fragment fragment,
                                              @NonNull final Class<? extends Activity> clz,
                                              final int requestCode) {
        startActivityForResult(fragment, null, Util.getApp().getPackageName(), clz.getName(),
                requestCode, null);
    }

    /**
     * Start the activity.
     *
     * @param fragment    The fragment.
     * @param clz         The activity class.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     * @param options     Additional options for how the Activity should be started.
     */
    public static void startActivityForResult(@NonNull final Fragment fragment,
                                              @NonNull final Class<? extends Activity> clz,
                                              final int requestCode,
                                              @Nullable final Bundle options) {
        startActivityForResult(fragment, null, Util.getApp().getPackageName(), clz.getName(),
                requestCode, options);
    }

    /**
     * Start the activity.
     *
     * @param fragment       The fragment.
     * @param clz            The activity class.
     * @param requestCode    if &gt;= 0, this code will be returned in
     *                       onActivityResult() when the activity exits.
     * @param sharedElements The names of the shared elements to transfer to the called
     *                       Activity and their associated Views.
     */
    public static void startActivityForResult(@NonNull final Fragment fragment,
                                              @NonNull final Class<? extends Activity> clz,
                                              final int requestCode,
                                              final View... sharedElements) {
        startActivityForResult(fragment, null, Util.getApp().getPackageName(), clz.getName(),
                requestCode, getOptionsBundle(fragment, sharedElements));
    }

    /**
     * Start the activity.
     *
     * @param fragment    The fragment.
     * @param clz         The activity class.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     * @param enterAnim   A resource ID of the animation resource to use for the
     *                    incoming activity.
     * @param exitAnim    A resource ID of the animation resource to use for the
     *                    outgoing activity.
     */
    public static void startActivityForResult(@NonNull final Fragment fragment,
                                              @NonNull final Class<? extends Activity> clz,
                                              final int requestCode,
                                              @AnimRes final int enterAnim,
                                              @AnimRes final int exitAnim) {
        startActivityForResult(fragment, null, Util.getApp().getPackageName(), clz.getName(),
                requestCode, getOptionsBundle(fragment, enterAnim, exitAnim));
    }

    /**
     * Start the activity.
     *
     * @param extras      The Bundle of extras to add to this intent.
     * @param fragment    The fragment.
     * @param clz         The activity class.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     */
    public static void startActivityForResult(@NonNull final Bundle extras,
                                              @NonNull final Fragment fragment,
                                              @NonNull final Class<? extends Activity> clz,
                                              final int requestCode) {
        startActivityForResult(fragment, extras, Util.getApp().getPackageName(), clz.getName(),
                requestCode, null);
    }

    /**
     * Start the activity.
     *
     * @param extras      The Bundle of extras to add to this intent.
     * @param fragment    The fragment.
     * @param clz         The activity class.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     * @param options     Additional options for how the Activity should be started.
     */
    public static void startActivityForResult(@NonNull final Bundle extras,
                                              @NonNull final Fragment fragment,
                                              @NonNull final Class<? extends Activity> clz,
                                              final int requestCode,
                                              @Nullable final Bundle options) {
        startActivityForResult(fragment, extras, Util.getApp().getPackageName(), clz.getName(),
                requestCode, options);
    }

    /**
     * Start the activity.
     *
     * @param extras         The Bundle of extras to add to this intent.
     * @param fragment       The fragment.
     * @param clz            The activity class.
     * @param requestCode    if &gt;= 0, this code will be returned in
     *                       onActivityResult() when the activity exits.
     * @param sharedElements The names of the shared elements to transfer to the called
     *                       Activity and their associated Views.
     */
    public static void startActivityForResult(@NonNull final Bundle extras,
                                              @NonNull final Fragment fragment,
                                              @NonNull final Class<? extends Activity> clz,
                                              final int requestCode,
                                              final View... sharedElements) {
        startActivityForResult(fragment, extras, Util.getApp().getPackageName(), clz.getName(),
                requestCode, getOptionsBundle(fragment, sharedElements));
    }

    /**
     * Start the activity.
     *
     * @param extras      The Bundle of extras to add to this intent.
     * @param fragment    The fragment.
     * @param clz         The activity class.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     * @param enterAnim   A resource ID of the animation resource to use for the
     *                    incoming activity.
     * @param exitAnim    A resource ID of the animation resource to use for the
     *                    outgoing activity.
     */
    public static void startActivityForResult(@NonNull final Bundle extras,
                                              @NonNull final Fragment fragment,
                                              @NonNull final Class<? extends Activity> clz,
                                              final int requestCode,
                                              @AnimRes final int enterAnim,
                                              @AnimRes final int exitAnim) {
        startActivityForResult(fragment, extras, Util.getApp().getPackageName(), clz.getName(),
                requestCode, getOptionsBundle(fragment, enterAnim, exitAnim));
    }

    /**
     * Start the activity for result.
     *
     * @param fragment    The fragment.
     * @param extras      The Bundle of extras to add to this intent.
     * @param pkg         The name of the package.
     * @param cls         The name of the class.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     */
    public static void startActivityForResult(@NonNull final Bundle extras,
                                              @NonNull final Fragment fragment,
                                              @NonNull final String pkg,
                                              @NonNull final String cls,
                                              final int requestCode) {
        startActivityForResult(fragment, extras, pkg, cls, requestCode, null);
    }

    /**
     * Start the activity for result.
     *
     * @param extras      The Bundle of extras to add to this intent.
     * @param fragment    The fragment.
     * @param pkg         The name of the package.
     * @param cls         The name of the class.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     * @param options     Additional options for how the Activity should be started.
     */
    public static void startActivityForResult(@NonNull final Bundle extras,
                                              @NonNull final Fragment fragment,
                                              @NonNull final String pkg,
                                              @NonNull final String cls,
                                              final int requestCode,
                                              @Nullable final Bundle options) {
        startActivityForResult(fragment, extras, pkg, cls, requestCode, options);
    }

    /**
     * Start the activity for result.
     *
     * @param extras         The Bundle of extras to add to this intent.
     * @param fragment       The fragment.
     * @param pkg            The name of the package.
     * @param cls            The name of the class.
     * @param requestCode    if &gt;= 0, this code will be returned in
     *                       onActivityResult() when the activity exits.
     * @param sharedElements The names of the shared elements to transfer to the called
     *                       Activity and their associated Views.
     */
    public static void startActivityForResult(@NonNull final Bundle extras,
                                              @NonNull final Fragment fragment,
                                              @NonNull final String pkg,
                                              @NonNull final String cls,
                                              final int requestCode,
                                              final View... sharedElements) {
        startActivityForResult(fragment, extras, pkg, cls,
                requestCode, getOptionsBundle(fragment, sharedElements));
    }

    /**
     * Start the activity for result.
     *
     * @param extras      The Bundle of extras to add to this intent.
     * @param pkg         The name of the package.
     * @param cls         The name of the class.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     * @param enterAnim   A resource ID of the animation resource to use for the
     *                    incoming activity.
     * @param exitAnim    A resource ID of the animation resource to use for the
     *                    outgoing activity.
     */
    public static void startActivityForResult(@NonNull final Bundle extras,
                                              @NonNull final Fragment fragment,
                                              @NonNull final String pkg,
                                              @NonNull final String cls,
                                              final int requestCode,
                                              @AnimRes final int enterAnim,
                                              @AnimRes final int exitAnim) {
        startActivityForResult(fragment, extras, pkg, cls,
                requestCode, getOptionsBundle(fragment, enterAnim, exitAnim));
    }

    /**
     * Start the activity for result.
     *
     * @param fragment    The fragment.
     * @param intent      The description of the activity to start.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     */
    public static void startActivityForResult(@NonNull final Fragment fragment,
                                              @NonNull final Intent intent,
                                              final int requestCode) {
        startActivityForResult(intent, fragment, requestCode, null);
    }

    /**
     * Start the activity for result.
     *
     * @param fragment    The fragment.
     * @param intent      The description of the activity to start.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     * @param options     Additional options for how the Activity should be started.
     */
    public static void startActivityForResult(@NonNull final Fragment fragment,
                                              @NonNull final Intent intent,
                                              final int requestCode,
                                              @Nullable final Bundle options) {
        startActivityForResult(intent, fragment, requestCode, options);
    }

    /**
     * Start the activity for result.
     *
     * @param fragment       The fragment.
     * @param intent         The description of the activity to start.
     * @param requestCode    if &gt;= 0, this code will be returned in
     *                       onActivityResult() when the activity exits.
     * @param sharedElements The names of the shared elements to transfer to the called
     *                       Activity and their associated Views.
     */
    public static void startActivityForResult(@NonNull final Fragment fragment,
                                              @NonNull final Intent intent,
                                              final int requestCode,
                                              final View... sharedElements) {
        startActivityForResult(intent, fragment,
                requestCode, getOptionsBundle(fragment, sharedElements));
    }

    /**
     * Start the activity for result.
     *
     * @param fragment    The fragment.
     * @param intent      The description of the activity to start.
     * @param requestCode if &gt;= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     * @param enterAnim   A resource ID of the animation resource to use for the
     *                    incoming activity.
     * @param exitAnim    A resource ID of the animation resource to use for the
     *                    outgoing activity.
     */
    public static void startActivityForResult(@NonNull final Fragment fragment,
                                              @NonNull final Intent intent,
                                              final int requestCode,
                                              @AnimRes final int enterAnim,
                                              @AnimRes final int exitAnim) {
        startActivityForResult(intent, fragment,
                requestCode, getOptionsBundle(fragment, enterAnim, exitAnim));
    }

    /**
     * Start activities.
     *
     * @param intents The descriptions of the activities to start.
     */
    public static void startActivities(@NonNull final Intent[] intents) {
        startActivities(intents, getTopActivityOrApp(), null);
    }

    /**
     * Start activities.
     *
     * @param intents The descriptions of the activities to start.
     * @param options Additional options for how the Activity should be started.
     */
    public static void startActivities(@NonNull final Intent[] intents,
                                       @Nullable final Bundle options) {
        startActivities(intents, getTopActivityOrApp(), options);
    }

    /**
     * Start activities.
     *
     * @param intents   The descriptions of the activities to start.
     * @param enterAnim A resource ID of the animation resource to use for the
     *                  incoming activity.
     * @param exitAnim  A resource ID of the animation resource to use for the
     *                  outgoing activity.
     */
    public static void startActivities(@NonNull final Intent[] intents,
                                       @AnimRes final int enterAnim,
                                       @AnimRes final int exitAnim) {
        Context context = getTopActivityOrApp();
        startActivities(intents, context, getOptionsBundle(context, enterAnim, exitAnim));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && context instanceof Activity) {
            ((Activity) context).overridePendingTransition(enterAnim, exitAnim);
        }
    }

    /**
     * Start activities.
     *
     * @param activity The activity.
     * @param intents  The descriptions of the activities to start.
     */
    public static void startActivities(@NonNull final Activity activity,
                                       @NonNull final Intent[] intents) {
        startActivities(intents, activity, null);
    }

    /**
     * Start activities.
     *
     * @param activity The activity.
     * @param intents  The descriptions of the activities to start.
     * @param options  Additional options for how the Activity should be started.
     */
    public static void startActivities(@NonNull final Activity activity,
                                       @NonNull final Intent[] intents,
                                       @Nullable final Bundle options) {
        startActivities(intents, activity, options);
    }

    /**
     * Start activities.
     *
     * @param activity  The activity.
     * @param intents   The descriptions of the activities to start.
     * @param enterAnim A resource ID of the animation resource to use for the
     *                  incoming activity.
     * @param exitAnim  A resource ID of the animation resource to use for the
     *                  outgoing activity.
     */
    public static void startActivities(@NonNull final Activity activity,
                                       @NonNull final Intent[] intents,
                                       @AnimRes final int enterAnim,
                                       @AnimRes final int exitAnim) {
        startActivities(intents, activity, getOptionsBundle(activity, enterAnim, exitAnim));
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
//            activity.overridePendingTransition(enterAnim, exitAnim);
//        }
    }

    /**
     * Start home activity.
     */
    public static void startHomeActivity() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }

    /**
     * Start the launcher activity.
     */
    public static void startLauncherActivity() {
        startLauncherActivity(Util.getApp().getPackageName());
    }

    /**
     * Start the launcher activity.
     *
     * @param pkg The name of the package.
     */
    public static void startLauncherActivity(@NonNull final String pkg) {
        String launcherActivity = getLauncherActivity(pkg);
        if (TextUtils.isEmpty(launcherActivity)) return;
        startActivity(pkg, launcherActivity);
    }


    /**
     * Return the name of launcher activity.
     *
     * @return the name of launcher activity
     */
    public static String getLauncherActivity() {
        return getLauncherActivity(Util.getApp().getPackageName());
    }

    /**
     * Return the name of launcher activity.
     *
     * @param pkg The name of the package.
     * @return the name of launcher activity
     */
    public static String getLauncherActivity(@NonNull final String pkg) {
        if (StringUtil.isSpace(pkg)) return "";
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(pkg);
        PackageManager pm = Util.getApp().getPackageManager();
        @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo> info = pm.queryIntentActivities(intent, 0);
        if (info == null || info.size() == 0) {
            return "";
        }
        return info.get(0).activityInfo.name;
    }

    /**
     * Return the list of main activities.
     *
     * @return the list of main activities
     */
    public static List<String> getMainActivities() {
        return getMainActivities(Util.getApp().getPackageName());
    }

    /**
     * Return the list of main activities.
     *
     * @param pkg The name of the package.
     * @return the list of main activities
     */
    public static List<String> getMainActivities(@NonNull final String pkg) {
        List<String> ret = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.setPackage(pkg);
        PackageManager pm = Util.getApp().getPackageManager();
        @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo> info = pm.queryIntentActivities(intent, 0);
        int size = info.size();
        if (size == 0) return ret;
        for (int i = 0; i < size; i++) {
            ResolveInfo ri = info.get(i);
            if (ri.activityInfo.processName.equals(pkg)) {
                ret.add(ri.activityInfo.name);
            }
        }
        return ret;
    }

    static LinkedList<WeakReference<Activity>> getActivityList() {
        return ActivityCollector.INSTANCE.getActivityList();
    }

    /**
     * Return the top activity in activity's stack.
     *
     * @return the top activity in activity's stack
     */
    public static Activity getTopActivity() {
        return UtilsActivityLifecycleImpl.INSTANCE.getTopActivity();
    }

    /**
     * Return whether the activity is alive.
     *
     * @param context The context.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isActivityAlive(final Context context) {
        return isActivityAlive(getActivityByContext(context));
    }

    /**
     * Return whether the activity is alive.
     *
     * @param activity The activity.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isActivityAlive(final Activity activity) {
        return activity != null && !activity.isFinishing()
                && !activity.isDestroyed(); //Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 ||
    }

    /**
     * Return whether the activity exists in activity's stack.
     *
     * @param activity The activity.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isActivityExistsInStack(@NonNull final Activity activity) {
        if (ActivityCollector.INSTANCE.size() == 0) return false;
        for (final WeakReference<Activity> activityWeakReference : getActivityList()) {
            if (activityWeakReference.get() != null) {
                Activity mActivity = activityWeakReference.get();
                if (mActivity.equals(activity)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return whether the activity exists in activity's stack.
     *
     * @param clz The activity class.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isActivityExistsInStack(@NonNull final Class<? extends Activity> clz) {
        if (ActivityCollector.INSTANCE.size() == 0) return false;
        for (final WeakReference<Activity> activityWeakReference : getActivityList()) {
            if (activityWeakReference.get() != null) {
                Activity mActivity = activityWeakReference.get();
                if (mActivity.getClass().equals(clz)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Finish the activity.
     *
     * @param activity The activity.
     */
    public static void finishActivity(@NonNull final Activity activity) {
        finishActivity(activity, false);
    }

    /**
     * Finish the activity.
     *
     * @param activity   The activity.
     * @param isLoadAnim True to use animation for the outgoing activity, false otherwise.
     */
    public static void finishActivity(@NonNull final Activity activity, final boolean isLoadAnim) {
        activity.finish();
        if (!isLoadAnim) {
            activity.overridePendingTransition(0, 0);
        } else {
            ScreenUtils.showSliderActivityExitAnim(activity);
        }
//        finishActivity(activity.getClass(), isLoadAnim);
    }

    /**
     * Finish the activity.
     *
     * @param activity  The activity.
     * @param enterAnim A resource ID of the animation resource to use for the
     *                  incoming activity.
     * @param exitAnim  A resource ID of the animation resource to use for the
     *                  outgoing activity.
     */
    public static void finishActivity(@NonNull final Activity activity,
                                      @AnimRes final int enterAnim,
                                      @AnimRes final int exitAnim) {
        activity.finish();
        activity.overridePendingTransition(enterAnim, exitAnim);

//        finishActivity(activity.getClass(), enterAnim, exitAnim);
    }

    /**
     * Finish the activity.
     *
     * @param clz The activity class.
     */
    public static void finishActivity(@NonNull final Class<? extends Activity> clz) {
        finishActivity(clz, false);
    }

    /**
     * Finish the activity.
     *
     * @param clz        The activity class.
     * @param isLoadAnim True to use animation for the outgoing activity, false otherwise.
     */
    public static void finishActivity(@NonNull final Class<? extends Activity> clz,
                                      final boolean isLoadAnim) {
        if (ActivityCollector.INSTANCE.size() == 0) return;
        Iterator<WeakReference<Activity>> iterator = getActivityList().iterator();
        do {
            WeakReference<Activity> item = iterator.next();
            if (item.get() != null) {
                Activity mActivity = item.get();
                if (mActivity.getClass().equals(clz)) {
                    mActivity.finish();
                    if (!isLoadAnim) {
                        mActivity.overridePendingTransition(0, 0);
                    } else {
                        ScreenUtils.showSliderActivityExitAnim(mActivity);
                    }
                    iterator.remove();
                }
            }
        } while (iterator.hasNext());
    }

    /**
     * Finish the activity.
     *
     * @param clz       The activity class.
     * @param enterAnim A resource ID of the animation resource to use for the
     *                  incoming activity.
     * @param exitAnim  A resource ID of the animation resource to use for the
     *                  outgoing activity.
     */
    public static void finishActivity(@NonNull final Class<? extends Activity> clz,
                                      @AnimRes final int enterAnim,
                                      @AnimRes final int exitAnim) {
        if (ActivityCollector.INSTANCE.size() == 0) return;
        Iterator<WeakReference<Activity>> iterator = getActivityList().iterator();
        do {
            WeakReference<Activity> item = iterator.next();
            if (item.get() != null) {
                Activity mActivity = item.get();
                if (mActivity.getClass().equals(clz)) {
                    mActivity.finish();
                    mActivity.overridePendingTransition(enterAnim, exitAnim);
                    iterator.remove();
                }
            }
        } while (iterator.hasNext());
    }


    /**
     * Finish to the activity.
     *
     * @param activity      The activity.
     * @param isIncludeSelf True to include the activity, false otherwise.
     */
    public static boolean finishToActivity(@NonNull final Activity activity,
                                           final boolean isIncludeSelf) {
        return finishToActivity(activity, isIncludeSelf, false);
    }

    /**
     * Finish to the activity. 结束活动栈里当前页activity所在位置上面的所有页面， 以返回到当前页activity（isIncludeSelf真时也结束当前页）
     *
     * @param activity      The activity.
     * @param isIncludeSelf True to include the activity, false otherwise.
     * @param isLoadAnim    True to use animation for the outgoing activity, false otherwise.
     */
    public static boolean finishToActivity(@NonNull final Activity activity,
                                           final boolean isIncludeSelf,
                                           final boolean isLoadAnim) {
        if (ActivityCollector.INSTANCE.size() == 0) return false;
        // 获取该列表的逆向迭代器
        ListIterator<WeakReference<Activity>> iterator = getActivityList().listIterator(ActivityCollector.INSTANCE.size());
        // 使用 do...while 循环进行倒序遍历
        do {
            // 判断是否存在前一个元素
            if (iterator.hasPrevious()) {
                // 获取并移动到前一个元素
                WeakReference<Activity> item = iterator.previous();
                if (item.get() != null) {
                    Activity mActivity = item.get();
                    if (mActivity.equals(activity)) {
                        if (isIncludeSelf) {
                            finishActivity(mActivity, isLoadAnim);
                            iterator.remove();
                        }
                        return true; //到当前页时结束循环
                    } else {
                        finishActivity(mActivity, isLoadAnim);
                        iterator.remove();
                    }
                }
            }
        } while (iterator.hasPrevious());
        return false;
    }

    /**
     * Finish to the activity.
     *
     * @param activity      The activity.
     * @param isIncludeSelf True to include the activity, false otherwise.
     * @param enterAnim     A resource ID of the animation resource to use for the
     *                      incoming activity.
     * @param exitAnim      A resource ID of the animation resource to use for the
     *                      outgoing activity.
     */
    public static boolean finishToActivity(@NonNull final Activity activity,
                                           final boolean isIncludeSelf,
                                           @AnimRes final int enterAnim,
                                           @AnimRes final int exitAnim) {
        if (ActivityCollector.INSTANCE.size() == 0) return false;
        // 获取该列表的逆向迭代器
        ListIterator<WeakReference<Activity>> iterator = getActivityList().listIterator(ActivityCollector.INSTANCE.size());
        // 使用 do...while 循环进行倒序遍历
        do {
            // 判断是否存在前一个元素
            if (iterator.hasPrevious()) {
                // 获取并移动到前一个元素
                WeakReference<Activity> item = iterator.previous();
                if (item.get() != null) {
                    Activity mActivity = item.get();
                    if (mActivity.equals(activity)) {
                        if (isIncludeSelf) {
                            finishActivity(mActivity, enterAnim, exitAnim);
                            iterator.remove();
                        }
                        return true; //到当前页时结束循环
                    } else {
                        finishActivity(mActivity, enterAnim, exitAnim);
                        iterator.remove();
                    }
                }
            }
        } while (iterator.hasPrevious());
        return false;
    }

    /**
     * Finish to the activity.
     *
     * @param clz           The activity class.
     * @param isIncludeSelf True to include the activity, false otherwise.
     */
    public static boolean finishToActivity(@NonNull final Class<? extends Activity> clz,
                                           final boolean isIncludeSelf) {
        return finishToActivity(clz, isIncludeSelf, false);
    }

    /**
     * Finish to the activity.
     *
     * @param clz           The activity class.
     * @param isIncludeSelf True to include the activity, false otherwise.
     * @param isLoadAnim    True to use animation for the outgoing activity, false otherwise.
     */
    public static boolean finishToActivity(@NonNull final Class<? extends Activity> clz,
                                           final boolean isIncludeSelf,
                                           final boolean isLoadAnim) {
        if (ActivityCollector.INSTANCE.size() == 0) return false;
        // 获取该列表的逆向迭代器
        ListIterator<WeakReference<Activity>> iterator = getActivityList().listIterator(ActivityCollector.INSTANCE.size());
        // 使用 do...while 循环进行倒序遍历
        do {
            // 判断是否存在前一个元素
            if (iterator.hasPrevious()) {
                // 获取并移动到前一个元素
                WeakReference<Activity> item = iterator.previous();
                if (item.get() != null) {
                    Activity mActivity = item.get();
                    if (mActivity.getClass().equals(clz)) {
                        if (isIncludeSelf) {
                            finishActivity(mActivity, isLoadAnim);
                            iterator.remove();
                        }
                        return true; //到当前页时结束循环
                    } else {
                        finishActivity(mActivity, isLoadAnim);
                        iterator.remove();
                    }
                }
            }
        } while (iterator.hasPrevious());
        return false;
    }

    /**
     * Finish to the activity.
     *
     * @param clz           The activity class.
     * @param isIncludeSelf True to include the activity, false otherwise.
     * @param enterAnim     A resource ID of the animation resource to use for the
     *                      incoming activity.
     * @param exitAnim      A resource ID of the animation resource to use for the
     *                      outgoing activity.
     */
    public static boolean finishToActivity(@NonNull final Class<? extends Activity> clz,
                                           final boolean isIncludeSelf,
                                           @AnimRes final int enterAnim,
                                           @AnimRes final int exitAnim) {
        if (ActivityCollector.INSTANCE.size() == 0) return false;
        // 获取该列表的逆向迭代器
        ListIterator<WeakReference<Activity>> iterator = getActivityList().listIterator(ActivityCollector.INSTANCE.size());
        // 使用 do...while 循环进行倒序遍历
        do {
            // 判断是否存在前一个元素
            if (iterator.hasPrevious()) {
                // 获取并移动到前一个元素
                WeakReference<Activity> item = iterator.previous();
                if (item.get() != null) {
                    Activity mActivity = item.get();
                    if (mActivity.getClass().equals(clz)) {
                        if (isIncludeSelf) {
                            finishActivity(mActivity, enterAnim, exitAnim);
                            iterator.remove();
                        }
                        return true; //到当前页时结束循环
                    } else {
                        finishActivity(mActivity, enterAnim, exitAnim);
                        iterator.remove();
                    }
                }
            }
        } while (iterator.hasPrevious());
        return false;
    }

    /**
     * Finish the activities whose type not equals the activity class.
     *
     * @param clz The activity class.
     */
    public static void finishOtherActivities(@NonNull final Class<? extends Activity> clz) {
        finishOtherActivities(clz, false);
    }


    /**
     * 结束当前页除外的所有页面
     * Finish the activities whose type not equals the activity class.
     *
     * @param clz        The activity class.
     * @param isLoadAnim True to use animation for the outgoing activity, false otherwise.
     */
    public static void finishOtherActivities(@NonNull final Class<? extends Activity> clz,
                                             final boolean isLoadAnim) {
        if (ActivityCollector.INSTANCE.size() == 0) return;
        Iterator<WeakReference<Activity>> iterator = getActivityList().iterator();
        do {
            WeakReference<Activity> item = iterator.next();
            if (item.get() != null) {
                Activity mActivity = item.get();
                if (!mActivity.getClass().equals(clz)) {
                    finishActivity(mActivity, isLoadAnim);
                    iterator.remove();
                }
            }
        } while (iterator.hasNext());
    }

    /**
     * Finish the activities whose type not equals the activity class.
     *
     * @param clz       The activity class.
     * @param enterAnim A resource ID of the animation resource to use for the
     *                  incoming activity.
     * @param exitAnim  A resource ID of the animation resource to use for the
     *                  outgoing activity.
     */
    public static void finishOtherActivities(@NonNull final Class<? extends Activity> clz,
                                             @AnimRes final int enterAnim,
                                             @AnimRes final int exitAnim) {
        if (ActivityCollector.INSTANCE.size() == 0) return;
        Iterator<WeakReference<Activity>> iterator = getActivityList().iterator();
        do {
            WeakReference<Activity> item = iterator.next();
            if (item.get() != null) {
                Activity mActivity = item.get();
                if (!mActivity.getClass().equals(clz)) {
                    finishActivity(mActivity, enterAnim, exitAnim);
                    iterator.remove();
                }
            }
        } while (iterator.hasNext());
    }

    /**
     * Finish all of activities.
     */
    public static void finishAllActivities() {
        ActivityCollector.INSTANCE.finishAll();
//        finishAllActivities(false);
    }

    /**
     * Finish all of activities.
     *
     * @param isLoadAnim True to use animation for the outgoing activity, false otherwise.
     */
    public static void finishAllActivities(final boolean isLoadAnim) {
        if (ActivityCollector.INSTANCE.size() == 0) return;
        for (final WeakReference<Activity> activityWeakReference : getActivityList()) {
            if (activityWeakReference.get() != null) {
                Activity mActivity = activityWeakReference.get();
                // sActivityList remove the index activity at onActivityDestroyed
                mActivity.finish();
                if (!isLoadAnim) {
                    mActivity.overridePendingTransition(0, 0);
                } else {
                    ScreenUtils.showSliderActivityExitAnim(mActivity);
                }
            }
        }
        getActivityList().clear();
    }

    /**
     * Finish all of activities.
     *
     * @param enterAnim A resource ID of the animation resource to use for the
     *                  incoming activity.
     * @param exitAnim  A resource ID of the animation resource to use for the
     *                  outgoing activity.
     */
    public static void finishAllActivities(@AnimRes final int enterAnim,
                                           @AnimRes final int exitAnim) {
        if (ActivityCollector.INSTANCE.size() == 0) return;
        for (final WeakReference<Activity> activityWeakReference : getActivityList()) {
            if (activityWeakReference.get() != null) {
                Activity mActivity = activityWeakReference.get();
                // sActivityList remove the index activity at onActivityDestroyed
                mActivity.finish();
                mActivity.overridePendingTransition(enterAnim, exitAnim);
            }
        }
        getActivityList().clear();
    }

    /**
     * Finish all of activities except the newest activity.
     */
    public static void finishAllActivitiesExceptNewest() {
        finishAllActivitiesExceptNewest(false);
    }

    /**
     * 保留栈顶最新的活动页
     * Finish all of activities except the newest activity.
     *
     * @param isLoadAnim True to use animation for the outgoing activity, false otherwise.
     */
    public static void finishAllActivitiesExceptNewest(final boolean isLoadAnim) {
        if (ActivityCollector.INSTANCE.size() == 0) return;
        // 获取该列表的逆向迭代器
        ListIterator<WeakReference<Activity>> iterator = getActivityList().listIterator(ActivityCollector.INSTANCE.size() - 1); //保留最后一个
        // 使用 do...while 循环进行倒序遍历
        do {
            // 判断是否存在前一个元素
            if (iterator.hasPrevious()) {
                // 获取并移动到前一个元素
                WeakReference<Activity> item = iterator.previous();
                if (item.get() != null) {
                    Activity mActivity = item.get();
                    finishActivity(mActivity, isLoadAnim);
                    iterator.remove();
                }
            }
        } while (iterator.hasPrevious());


//        Activity topActivity = getTopActivity();
//        if (topActivity != null)
//        finishOtherActivities(topActivity.getClass(), false);
    }

    /**
     * Finish all of activities except the newest activity.
     *
     * @param enterAnim A resource ID of the animation resource to use for the
     *                  incoming activity.
     * @param exitAnim  A resource ID of the animation resource to use for the
     *                  outgoing activity.
     */
    public static void finishAllActivitiesExceptNewest(@AnimRes final int enterAnim,
                                                       @AnimRes final int exitAnim) {
        if (ActivityCollector.INSTANCE.size() == 0) return;
        // 获取该列表的逆向迭代器
        ListIterator<WeakReference<Activity>> iterator = getActivityList().listIterator(ActivityCollector.INSTANCE.size() - 1); //保留最后一个
        // 使用 do...while 循环进行倒序遍历
        do {
            // 判断是否存在前一个元素
            if (iterator.hasPrevious()) {
                // 获取并移动到前一个元素
                WeakReference<Activity> item = iterator.previous();
                if (item.get() != null) {
                    Activity mActivity = item.get();
                    finishActivity(mActivity, enterAnim, exitAnim);
                    iterator.remove();
                }
            }
        } while (iterator.hasPrevious());
    }

    /**
     * Return the icon of activity.
     *
     * @param activity The activity.
     * @return the icon of activity
     */
    @Nullable
    public static Drawable getActivityIcon(@NonNull final Activity activity) {
        return getActivityIcon(activity.getComponentName());
    }

    /**
     * Return the icon of activity.
     *
     * @param clz The activity class.
     * @return the icon of activity
     */
    @Nullable
    public static Drawable getActivityIcon(@NonNull final Class<? extends Activity> clz) {
        return getActivityIcon(new ComponentName(Util.getApp(), clz));
    }

    /**
     * Return the icon of activity.
     *
     * @param activityName The name of activity.
     * @return the icon of activity
     */
    @Nullable
    public static Drawable getActivityIcon(@NonNull final ComponentName activityName) {
        PackageManager pm = Util.getApp().getPackageManager();
        try {
            return pm.getActivityIcon(activityName);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.e("ActivityUtils", "err: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the logo of activity.
     *
     * @param activity The activity.
     * @return the logo of activity
     */
    @Nullable
    public static Drawable getActivityLogo(@NonNull final Activity activity) {
        return getActivityLogo(activity.getComponentName());
    }

    /**
     * Return the logo of activity.
     *
     * @param clz The activity class.
     * @return the logo of activity
     */
    @Nullable
    public static Drawable getActivityLogo(@NonNull final Class<? extends Activity> clz) {
        return getActivityLogo(new ComponentName(Util.getApp(), clz));
    }

    /**
     * Return the logo of activity.
     *
     * @param activityName The name of activity.
     * @return the logo of activity
     */
    @Nullable
    public static Drawable getActivityLogo(@NonNull final ComponentName activityName) {
        PackageManager pm = Util.getApp().getPackageManager();
        try {
            return pm.getActivityLogo(activityName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void startActivity(final Context context,
                                      final Bundle extras,
                                      final String pkg,
                                      final String cls,
                                      @Nullable final Bundle options) {
        Intent intent = new Intent();
        if (extras != null) intent.putExtras(extras);
        intent.setComponent(new ComponentName(pkg, cls));
        startActivity(intent, context, options);
    }

    private static boolean startActivity(final Intent intent,
                                         final Context context,
                                         final Bundle options) {
        if (!isIntentAvailable(intent)) {
            Log.e("ActivityUtils", "intent is unavailable");
            return false;
        }
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (options != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            context.startActivity(intent, options);
        } else {
            context.startActivity(intent);
        }
        return true;
    }

    private static boolean isIntentAvailable(final Intent intent) {
        return Util.getApp()
                .getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                .size() > 0;
    }

    private static boolean startActivityForResult(final Activity activity,
                                                  final Bundle extras,
                                                  final String pkg,
                                                  final String cls,
                                                  final int requestCode,
                                                  @Nullable final Bundle options) {
        Intent intent = new Intent();
        if (extras != null) intent.putExtras(extras);
        intent.setComponent(new ComponentName(pkg, cls));
        return startActivityForResult(intent, activity, requestCode, options);
    }

    private static boolean startActivityForResult(final Intent intent,
                                                  final Activity activity,
                                                  final int requestCode,
                                                  @Nullable final Bundle options) {
        if (!isIntentAvailable(intent)) {
            Log.e("ActivityUtils", "intent is unavailable");
            return false;
        }
        if (options != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            activity.startActivityForResult(intent, requestCode, options);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
        return true;
    }

    private static void startActivities(final Intent[] intents,
                                        final Context context,
                                        @Nullable final Bundle options) {
        if (!(context instanceof Activity)) {
            for (Intent intent : intents) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        }
        if (options != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            context.startActivities(intents, options);
        } else {
            context.startActivities(intents);
        }
    }

    private static boolean startActivityForResult(final Fragment fragment,
                                                  final Bundle extras,
                                                  final String pkg,
                                                  final String cls,
                                                  final int requestCode,
                                                  @Nullable final Bundle options) {
        Intent intent = new Intent();
        if (extras != null) intent.putExtras(extras);
        intent.setComponent(new ComponentName(pkg, cls));
        return startActivityForResult(intent, fragment, requestCode, options);
    }

    private static boolean startActivityForResult(final Intent intent,
                                                  final Fragment fragment,
                                                  final int requestCode,
                                                  @Nullable final Bundle options) {
        if (!isIntentAvailable(intent)) {
            LogUtil.e("ActivityUtils", "intent is unavailable");
            return false;
        }
        if (fragment.getActivity() == null) {
            LogUtil.e("ActivityUtils", "Fragment " + fragment + " not attached to Activity");
            return false;
        }
        if (options != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            fragment.startActivityForResult(intent, requestCode, options);
        } else {
            fragment.startActivityForResult(intent, requestCode);
        }
        return true;
    }

    private static Bundle getOptionsBundle(final Fragment fragment,
                                           final int enterAnim,
                                           final int exitAnim) {
        Activity activity = fragment.getActivity();
        if (activity == null) return null;
        return ActivityOptionsCompat.makeCustomAnimation(activity, enterAnim, exitAnim).toBundle();
    }

    private static Bundle getOptionsBundle(final Context context,
                                           final int enterAnim,
                                           final int exitAnim) {
        return ActivityOptionsCompat.makeCustomAnimation(context, enterAnim, exitAnim).toBundle();
    }

    private static Bundle getOptionsBundle(final Fragment fragment,
                                           final View[] sharedElements) {
        Activity activity = fragment.getActivity();
        if (activity == null) return null;
        return getOptionsBundle(activity, sharedElements);
    }

    private static Bundle getOptionsBundle(final Activity activity,
                                           final View[] sharedElements) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return null;
        if (sharedElements == null) return null;
        int len = sharedElements.length;
        if (len <= 0) return null;
        @SuppressWarnings("unchecked")
        Pair<View, String>[] pairs = new Pair[len];
        for (int i = 0; i < len; i++) {
            pairs[i] = Pair.create(sharedElements[i], sharedElements[i].getTransitionName());
        }
        return ActivityOptionsCompat.makeSceneTransitionAnimation(activity, pairs).toBundle();
    }

    private static Context getTopActivityOrApp() {
        if (Util.isAppForeground()) {
            Activity topActivity = getTopActivity();
            return topActivity == null ? Util.getApp() : topActivity;
        } else {
            return Util.getApp();
        }
    }

    /**
     * 退出应用程序
     */
    public static void exitApp() {
        try {
            finishAllActivities();
            Context context = getTopActivityOrApp();
            ActivityManager activityMgr =
                    (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            activityMgr.killBackgroundProcesses(context.getPackageName());
            //			System.exit(0);
        } catch (Exception e) {
            LogUtil.e("ActivityUtil", "err: " + e.getMessage());
            e.printStackTrace();
        }
    }
}