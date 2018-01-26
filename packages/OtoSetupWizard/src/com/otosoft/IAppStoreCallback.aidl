package com.otosoft;

interface IAppStoreCallback {
    void downloadCallback(boolean hasData, String appFileName, String appName, int total, boolean isDone);
}
