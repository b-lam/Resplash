package com.b_lam.resplash.util.download

const val DOWNLOADER_DEFAULT = "default"
const val DOWNLOADER_SYSTEM = "system"

const val ACTION_DOWNLOAD_COMPLETE = "com.b_lam.resplash.ACTION_DOWNLOAD_COMPLETE"

const val DATA_ACTION = "com.b_lam.resplash.DATA_ACTION"
const val DATA_URI = "com.b_lam.resplash.DATA_URI"

const val DOWNLOAD_STATUS = "com.b_lam.resplash.DOWNLOAD_STATUS"

const val STATUS_SUCCESSFUL = 1
const val STATUS_FAILED = 2
const val STATUS_CANCELLED = 3

enum class DownloadAction { DOWNLOAD, WALLPAPER }