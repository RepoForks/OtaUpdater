package io.github.otaupdater.otalibary;

import android.content.Context;
import android.support.annotation.NonNull;

import io.github.otaupdater.otalibary.enums.RomUpdaterError;
import io.github.otaupdater.otalibary.enums.UpdateFrom;
import io.github.otaupdater.otalibary.objects.GitHub;
import io.github.otaupdater.otalibary.objects.Update;


public class RomUpdaterUtils {
    private Context context;
    private UpdateListener updateListener;
    private AppUpdaterListener appUpdaterListener;
    private UpdateFrom updateFrom;
    private GitHub gitHub;
    private String xmlOrJSONUrl;
    private UtilsAsync.LatestAppVersion latestAppVersion;

    public interface UpdateListener {
        /**
         * onSuccess method called after it is successful
         * onFailed method called if it can't retrieve the latest version
         *
         * @param update            object with the latest update information: version and url to download
         * @see com.github.javiersantos.appupdater.objects.Update
         * @param isUpdateAvailable compare installed version with the latest one
         */
        void onSuccess(Update update, Boolean isUpdateAvailable);

        void onFailed(RomUpdaterError error);
    }

    @Deprecated
    public interface AppUpdaterListener {
        /**
         * onSuccess method called after it is successful
         * onFailed method called if it can't retrieve the latest version
         *
         * @param latestVersion     available in the provided source
         * @param isUpdateAvailable compare installed version with the latest one
         */
        void onSuccess(String latestVersion, Boolean isUpdateAvailable);

        void onFailed(RomUpdaterError error);
    }

    public RomUpdaterUtils(Context context) {
        this.context = context;
        this.updateFrom = UpdateFrom.GOOGLE_PLAY;
    }

    /**
     * Set the source where the latest update can be found. Default: GOOGLE_PLAY.
     *
     * @param updateFrom source where the latest update is uploaded. If GITHUB is selected, .setGitHubAndRepo method is required.
     * @return this
     * @see com.github.javiersantos.appupdater.enums.UpdateFrom
     * @see <a href="https://github.com/javiersantos/AppUpdater/wiki">Additional documentation</a>
     */
    public RomUpdaterUtils setUpdateFrom(UpdateFrom updateFrom) {
        this.updateFrom = updateFrom;
        return this;
    }

    /**
     * Set the user and repo where the releases are uploaded. You must upload your updates as a release in order to work properly tagging them as vX.X.X or X.X.X.
     *
     * @param user GitHub user
     * @param repo GitHub repository
     * @return this
     */
    public RomUpdaterUtils setGitHubUserAndRepo(String user, String repo) {
        this.gitHub = new GitHub(user, repo);
        return this;
    }

    /**
     * Set the url to the xml with the latest version info.
     *
     * @param xmlUrl file
     * @return this
     */
    public RomUpdaterUtils setUpdateXML(@NonNull String xmlUrl) {
        this.xmlOrJSONUrl = xmlUrl;
        return this;
    }

    /**
     * Set the url to the xml with the latest version info.
     *
     * @param jsonUrl file
     * @return this
     */
    public RomUpdaterUtils setUpdateJSON(@NonNull String jsonUrl) {
        this.xmlOrJSONUrl = jsonUrl;
        return this;
    }


    /**
     * Method to set the AppUpdaterListener for the RomUpdaterUtils actions
     *
     * @param appUpdaterListener the listener to be notified
     * @return this
     * @see com.github.javiersantos.appupdater.AppUpdaterUtils.AppUpdaterListener
     * @deprecated
     */
    public RomUpdaterUtils withListener(AppUpdaterListener appUpdaterListener) {
        this.appUpdaterListener = appUpdaterListener;
        return this;
    }

    /**
     * Method to set the UpdateListener for the RomUpdaterUtils actions
     *
     * @param updateListener the listener to be notified
     * @return this
     * @see com.github.javiersantos.appupdater.AppUpdaterUtils.UpdateListener
     */
    public RomUpdaterUtils withListener(UpdateListener updateListener) {
        this.updateListener = updateListener;
        return this;
    }

    /**
     * Execute RomUpdaterUtils in background.
     */
    public void start() {
        latestAppVersion = new UtilsAsync.LatestAppVersion(context, true, updateFrom, gitHub, xmlOrJSONUrl, new RomUpdater.LibraryListener() {
            @Override
            public void onSuccess(Update update) {
                if (updateListener != null) {
                    updateListener.onSuccess(update, UtilsLibrary.isUpdateAvailable(UtilsLibrary.getAppInstalledVersion(), update.getLatestVersion()));
                } else if (appUpdaterListener != null) {
                    appUpdaterListener.onSuccess(update.getLatestVersion(), UtilsLibrary.isUpdateAvailable(UtilsLibrary.getAppInstalledVersion(), update.getLatestVersion()));
                } else {
                    throw new RuntimeException("You must provide a listener for the RomUpdaterUtils");
                }
            }

            @Override
            public void onFailed(RomUpdaterError error) {
                if (updateListener != null) {
                    updateListener.onFailed(error);
                } else if (appUpdaterListener != null) {
                    appUpdaterListener.onFailed(error);
                } else {
                    throw new RuntimeException("You must provide a listener for the RomUpdaterUtils");
                }
            }
        });

        latestAppVersion.execute();
    }

    /**
     * Stops the execution of RomUpdater.
     */
    public void stop() {
        if (latestAppVersion != null && !latestAppVersion.isCancelled()) {
            latestAppVersion.cancel(true);
        }
    }
}