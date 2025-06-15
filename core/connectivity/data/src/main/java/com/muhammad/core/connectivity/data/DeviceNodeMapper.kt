package com.muhammad.core.connectivity.data

import com.google.android.gms.wearable.*
import com.muhammad.core.connectivity.domain.DeviceNode

fun Node.toDeviceNode() : DeviceNode{
    return DeviceNode(
        id = id,
        displayName = displayName, isNearBy = isNearby
    )
}