module NativeFunctions

local function localNative = { native }

local function localNativeWithArgs = |foo,bar| { native }

function publicNative = { native }

function publicNativeWithArgs = |foo,bar| { native }

local function localCompactNativeWithArgs = |foo,bar| -> native

function publicCompactNativeWithArgs = |foo,bar| -> native

function nativeClosureTest = {
    let closure = |a,b| ->
        native(nativeClosure) # 'nativeClosure' is name used for the C function
}

pimp java.lang.String {
        function wrap = |this, left, right| -> native
}