/*
 * Copyright 2019 - 2024 Brain Craft Ltd. - All Rights Reserved
 *
 * This file is a part of Video Crop.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 *
 * @author: shakib@braincraftapps.com
 * @file: SocialMediaType.kt
 * @modified: Aug 14, 2024, 12:15 PM
 */

package com.braincraftapps.cropvideos.fragment.editor.export.share.data

import com.braincraftapps.cropvideos.R

enum class SocialMediaType(val titleRes: Int, val iconRes: Int) {
    INSTAGRAM(R.string.export_share_social_media_instagram, R.drawable.vector_instagram_24dp),
    TELEGRAM(R.string.export_share_social_media_telegram, R.drawable.vector_telegram_24dp),
    FACEBOOK(R.string.export_share_social_media_facebook, R.drawable.vector_facebook_24dp),
    WHATSAPP(R.string.export_share_social_media_whatsapp, R.drawable.vector_whatsapp_24dp),
    MESSENGER(R.string.export_share_social_media_messenger, R.drawable.vector_messenger_24dp),
    YOUTUBE(R.string.export_share_social_media_youtube, R.drawable.vector_youtube_24dp),
    MORE(R.string.export_share_social_media_more, R.drawable.vector_share_more_24dp)
}
