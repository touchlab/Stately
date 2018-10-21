package co.touchlab.stately

/**
 * Method to freeze state. Calls the platform implementation of 'freeze' on native, and is a noop on other platforms.
 */
expect fun <T> T.freeze(): T

/**
 * Determine if object is frozen. Will return false on non-native platforms.
 */
expect fun <T> T.isFrozen(): Boolean

/**
 * Determine if object is frozen. Will return true on non-native platforms, which for logic is generally the outcome
 * you want.
 */
expect fun <T> T.isNativeFrozen(): Boolean

/**
 * Are we in on a native platform?
 */
expect val isNative:Boolean