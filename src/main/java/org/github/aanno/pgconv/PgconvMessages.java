package org.github.aanno.pgconv;

import com.adobe.epubcheck.util.Messages;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class PgconvMessages {

    protected static final String BUNDLE_NAME = "com.adobe.epubcheck.util.messages";
    protected static final Table<String, Locale, PgconvMessages> messageTable = HashBasedTable.create();

    public static PgconvMessages getInstance() {
        return getInstance(null, null);
    }

    /**
     * Get a Messages instance that has been localized for the given locale, or the
     * default locale if locale is null. Note that passing an unknown locale returns
     * the default messages.
     *
     * @param locale The locale to use for localization of the messages.
     * @return The localized messages or default.
     */
    public static PgconvMessages getInstance(Locale locale) {
        return getInstance(locale, null);
    }

    public static PgconvMessages getInstance(Locale locale, Class<?> cls) {
        PgconvMessages instance = null;
        locale = (locale == null) ? Locale.getDefault() : locale;

        String bundleKey = (cls == null) ? BUNDLE_NAME : getBundleName(cls);
        if (messageTable.contains(bundleKey, locale)) {
            instance = messageTable.get(bundleKey, locale);
        } else {
            synchronized (Messages.class) {
                if (instance == null) {
                    instance = new PgconvMessages(locale, bundleKey);
                    messageTable.put(bundleKey, locale, instance);
                }
            }
        }

        return instance;

    }

    protected static String getBundleName(Class<?> cls) {
        String className = cls.getName();
        int i = className.lastIndexOf('.');
        return ((i > 0) ? className.substring(0, i + 1) : "") + "messages";
    }

    protected ResourceBundle bundle;
    protected Locale locale;

    protected PgconvMessages(Locale locale, String bundleName) {
        this.locale = (locale != null) ? locale : Locale.getDefault();
        this.bundle = ResourceBundle.getBundle(bundleName, this.locale /*, new UTF8Control() */);
    }

    protected PgconvMessages() {
        this(null);
    }

    protected PgconvMessages(Locale locale) {
        this(locale, BUNDLE_NAME);
    }

    public String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public String get(String key, Object... arguments) {
        return MessageFormat.format(get(key), arguments);
    }

}
