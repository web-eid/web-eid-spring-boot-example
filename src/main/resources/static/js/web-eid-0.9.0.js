/*
 * Copyright (c) 2020 The Web eID Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

var config = Object.freeze({
    VERSION: "0.9.0",
    EXTENSION_HANDSHAKE_TIMEOUT: 1000,
    NATIVE_APP_HANDSHAKE_TIMEOUT: 5 * 1000,
    DEFAULT_USER_INTERACTION_TIMEOUT: 2 * 60 * 1000,
    DEFAULT_SERVER_REQUEST_TIMEOUT: 20 * 1000,
});

var ErrorCode;
(function (ErrorCode) {
    // Timeout errors
    ErrorCode["ERR_WEBEID_ACTION_TIMEOUT"] = "ERR_WEBEID_ACTION_TIMEOUT";
    ErrorCode["ERR_WEBEID_USER_TIMEOUT"] = "ERR_WEBEID_USER_TIMEOUT";
    ErrorCode["ERR_WEBEID_SERVER_TIMEOUT"] = "ERR_WEBEID_SERVER_TIMEOUT";
    // Health errors
    ErrorCode["ERR_WEBEID_VERSION_MISMATCH"] = "ERR_WEBEID_VERSION_MISMATCH";
    ErrorCode["ERR_WEBEID_VERSION_INVALID"] = "ERR_WEBEID_VERSION_INVALID";
    ErrorCode["ERR_WEBEID_EXTENSION_UNAVAILABLE"] = "ERR_WEBEID_EXTENSION_UNAVAILABLE";
    ErrorCode["ERR_WEBEID_NATIVE_UNAVAILABLE"] = "ERR_WEBEID_NATIVE_UNAVAILABLE";
    ErrorCode["ERR_WEBEID_UNKNOWN_ERROR"] = "ERR_WEBEID_UNKNOWN_ERROR";
    // Security errors
    ErrorCode["ERR_WEBEID_CONTEXT_INSECURE"] = "ERR_WEBEID_CONTEXT_INSECURE";
    ErrorCode["ERR_WEBEID_PROTOCOL_INSECURE"] = "ERR_WEBEID_PROTOCOL_INSECURE";
    ErrorCode["ERR_WEBEID_TLS_CONNECTION_BROKEN"] = "ERR_WEBEID_TLS_CONNECTION_BROKEN";
    ErrorCode["ERR_WEBEID_TLS_CONNECTION_INSECURE"] = "ERR_WEBEID_TLS_CONNECTION_INSECURE";
    ErrorCode["ERR_WEBEID_TLS_CONNECTION_WEAK"] = "ERR_WEBEID_TLS_CONNECTION_WEAK";
    ErrorCode["ERR_WEBEID_CERTIFICATE_CHANGED"] = "ERR_WEBEID_CERTIFICATE_CHANGED";
    ErrorCode["ERR_WEBEID_ORIGIN_MISMATCH"] = "ERR_WEBEID_ORIGIN_MISMATCH";
    // Third party errors
    ErrorCode["ERR_WEBEID_SERVER_REJECTED"] = "ERR_WEBEID_SERVER_REJECTED";
    ErrorCode["ERR_WEBEID_USER_CANCELLED"] = "ERR_WEBEID_USER_CANCELLED";
    ErrorCode["ERR_WEBEID_NATIVE_FATAL"] = "ERR_WEBEID_NATIVE_FATAL";
    // Developer mistakes
    ErrorCode["ERR_WEBEID_ACTION_PENDING"] = "ERR_WEBEID_ACTION_PENDING";
    ErrorCode["ERR_WEBEID_MISSING_PARAMETER"] = "ERR_WEBEID_MISSING_PARAMETER";
})(ErrorCode || (ErrorCode = {}));
var ErrorCode$1 = ErrorCode;

var Action;
(function (Action) {
    Action["STATUS"] = "web-eid:status";
    Action["STATUS_ACK"] = "web-eid:status-ack";
    Action["STATUS_SUCCESS"] = "web-eid:status-success";
    Action["STATUS_FAILURE"] = "web-eid:status-failure";
    Action["AUTHENTICATE"] = "web-eid:authenticate";
    Action["AUTHENTICATE_ACK"] = "web-eid:authenticate-ack";
    Action["AUTHENTICATE_SUCCESS"] = "web-eid:authenticate-success";
    Action["AUTHENTICATE_FAILURE"] = "web-eid:authenticate-failure";
    Action["SIGN"] = "web-eid:sign";
    Action["SIGN_ACK"] = "web-eid:sign-ack";
    Action["SIGN_SUCCESS"] = "web-eid:sign-success";
    Action["SIGN_FAILURE"] = "web-eid:sign-failure";
})(Action || (Action = {}));
var Action$1 = Action;

class CertificateChangedError extends Error {
    constructor(message = "server certificate changed between requests") {
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_CERTIFICATE_CHANGED;
    }
}

class OriginMismatchError extends Error {
    constructor(message = "URLs for a single operation require the same origin") {
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_ORIGIN_MISMATCH;
    }
}

const SECURE_CONTEXTS_INFO_URL = "https://developer.mozilla.org/en-US/docs/Web/Security/Secure_Contexts";
class ContextInsecureError extends Error {
    constructor(message = "Secure context required, see " + SECURE_CONTEXTS_INFO_URL) {
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_CONTEXT_INSECURE;
    }
}

class ExtensionUnavailableError extends Error {
    constructor(message = "Web-eID extension is not available") {
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_EXTENSION_UNAVAILABLE;
    }
}

class ActionPendingError extends Error {
    constructor(message = "same action for Web-eID browser extension is already pending") {
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_ACTION_PENDING;
    }
}

class NativeFatalError extends Error {
    constructor(message = "native application terminated with a fatal error") {
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_NATIVE_FATAL;
    }
}

class NativeUnavailableError extends Error {
    constructor(message = "Web-eID native application is not available") {
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_NATIVE_UNAVAILABLE;
    }
}

class ServerRejectedError extends Error {
    constructor(message = "server rejected the request") {
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_SERVER_REJECTED;
    }
}

class UserTimeoutError extends Error {
    constructor(message = "user failed to respond in time") {
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_USER_TIMEOUT;
    }
}

class UserCancelledError extends Error {
    constructor(message = "request was cancelled by the user") {
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_USER_CANCELLED;
    }
}

function tmpl(strings, requiresUpdate) {
    return `Update required for Web-eID ${requiresUpdate}`;
}
class VersionMismatchError extends Error {
    constructor(message, versions, requiresUpdate) {
        if (!message) {
            if (!requiresUpdate) {
                message = "requiresUpdate not provided";
            }
            else if (requiresUpdate.extension && requiresUpdate.nativeApp) {
                message = tmpl `${"extension and native app"}`;
            }
            else if (requiresUpdate.extension) {
                message = tmpl `${"extension"}`;
            }
            else if (requiresUpdate.nativeApp) {
                message = tmpl `${"native app"}`;
            }
        }
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_VERSION_MISMATCH;
        this.requiresUpdate = requiresUpdate;
        if (versions) {
            const { library, extension, nativeApp } = versions;
            Object.assign(this, { library, extension, nativeApp });
        }
    }
}

class TlsConnectionBrokenError extends Error {
    constructor(message = "TLS connection was broken") {
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_TLS_CONNECTION_BROKEN;
    }
}

class TlsConnectionInsecureError extends Error {
    constructor(message = "TLS connection was insecure") {
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_TLS_CONNECTION_INSECURE;
    }
}

class TlsConnectionWeakError extends Error {
    constructor(message = "TLS connection was weak") {
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_TLS_CONNECTION_WEAK;
    }
}

class ProtocolInsecureError extends Error {
    constructor(message = "HTTPS required") {
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_PROTOCOL_INSECURE;
    }
}

class ActionTimeoutError extends Error {
    constructor(message = "extension message timeout") {
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_ACTION_TIMEOUT;
    }
}

class VersionInvalidError extends Error {
    constructor(message = "invalid version string") {
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_VERSION_INVALID;
    }
}

class ServerTimeoutError extends Error {
    constructor(message = "server failed to respond in time") {
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_SERVER_TIMEOUT;
    }
}

class UnknownError extends Error {
    constructor(message = "an unknown error occurred") {
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_UNKNOWN_ERROR;
    }
}

const errorCodeToErrorClass = {
    [ErrorCode$1.ERR_WEBEID_ACTION_PENDING]: ActionPendingError,
    [ErrorCode$1.ERR_WEBEID_ACTION_TIMEOUT]: ActionTimeoutError,
    [ErrorCode$1.ERR_WEBEID_CERTIFICATE_CHANGED]: CertificateChangedError,
    [ErrorCode$1.ERR_WEBEID_ORIGIN_MISMATCH]: OriginMismatchError,
    [ErrorCode$1.ERR_WEBEID_CONTEXT_INSECURE]: ContextInsecureError,
    [ErrorCode$1.ERR_WEBEID_EXTENSION_UNAVAILABLE]: ExtensionUnavailableError,
    [ErrorCode$1.ERR_WEBEID_NATIVE_FATAL]: NativeFatalError,
    [ErrorCode$1.ERR_WEBEID_NATIVE_UNAVAILABLE]: NativeUnavailableError,
    [ErrorCode$1.ERR_WEBEID_PROTOCOL_INSECURE]: ProtocolInsecureError,
    [ErrorCode$1.ERR_WEBEID_SERVER_REJECTED]: ServerRejectedError,
    [ErrorCode$1.ERR_WEBEID_SERVER_TIMEOUT]: ServerTimeoutError,
    [ErrorCode$1.ERR_WEBEID_TLS_CONNECTION_BROKEN]: TlsConnectionBrokenError,
    [ErrorCode$1.ERR_WEBEID_TLS_CONNECTION_INSECURE]: TlsConnectionInsecureError,
    [ErrorCode$1.ERR_WEBEID_TLS_CONNECTION_WEAK]: TlsConnectionWeakError,
    [ErrorCode$1.ERR_WEBEID_USER_CANCELLED]: UserCancelledError,
    [ErrorCode$1.ERR_WEBEID_USER_TIMEOUT]: UserTimeoutError,
    [ErrorCode$1.ERR_WEBEID_VERSION_INVALID]: VersionInvalidError,
    [ErrorCode$1.ERR_WEBEID_VERSION_MISMATCH]: VersionMismatchError,
};
function deserializeError(errorObject) {
    let error;
    if (typeof errorObject.code == "string" && errorObject.code in errorCodeToErrorClass) {
        const CustomError = errorCodeToErrorClass[errorObject.code];
        error = new CustomError();
    }
    else {
        error = new UnknownError();
    }
    for (const [key, value] of Object.entries(errorObject)) {
        error[key] = value;
    }
    return error;
}

class WebExtensionService {
    constructor() {
        this.queue = [];
        window.addEventListener("message", (event) => this.receive(event));
    }
    receive(event) {
        var _a, _b, _c, _d;
        const message = event.data;
        const suffix = (_b = (_a = message.action) === null || _a === void 0 ? void 0 : _a.match(/success$|failure$|ack$/)) === null || _b === void 0 ? void 0 : _b[0];
        const initialAction = this.getInitialAction(message.action);
        const pending = this.getPendingMessage(initialAction);
        if (suffix === "ack") {
            console.log("ack message", message);
            console.log("ack pending", pending === null || pending === void 0 ? void 0 : pending.message.action);
            console.log("ack queue", JSON.stringify(this.queue));
        }
        if (pending) {
            switch (suffix) {
                case "ack": {
                    clearTimeout(pending.ackTimer);
                    break;
                }
                case "success": {
                    (_c = pending.resolve) === null || _c === void 0 ? void 0 : _c.call(pending, message);
                    this.removeFromQueue(initialAction);
                    break;
                }
                case "failure": {
                    (_d = pending.reject) === null || _d === void 0 ? void 0 : _d.call(pending, message.error ? deserializeError(message.error) : message);
                    this.removeFromQueue(initialAction);
                    break;
                }
            }
        }
    }
    send(message, timeout) {
        if (this.getPendingMessage(message.action)) {
            return Promise.reject(new ActionPendingError());
        }
        else if (!window.isSecureContext) {
            return Promise.reject(new ContextInsecureError());
        }
        else {
            const pending = { message };
            this.queue.push(pending);
            pending.promise = new Promise((resolve, reject) => {
                pending.resolve = resolve;
                pending.reject = reject;
            });
            pending.ackTimer = setTimeout(() => this.onAckTimeout(pending), config.EXTENSION_HANDSHAKE_TIMEOUT);
            pending.replyTimer = setTimeout(() => this.onReplyTimeout(pending), timeout);
            window.postMessage(message, "*");
            return pending.promise;
        }
    }
    onReplyTimeout(pending) {
        var _a;
        console.log("onReplyTimeout", pending.message.action);
        (_a = pending.reject) === null || _a === void 0 ? void 0 : _a.call(pending, new ActionTimeoutError());
        this.removeFromQueue(pending.message.action);
    }
    onAckTimeout(pending) {
        var _a;
        console.log("onAckTimeout", pending.message.action);
        (_a = pending.reject) === null || _a === void 0 ? void 0 : _a.call(pending, new ExtensionUnavailableError());
        clearTimeout(pending.replyTimer);
    }
    getPendingMessage(action) {
        return this.queue.find((pm) => {
            return pm.message.action === action;
        });
    }
    getSuccessAction(action) {
        return `${action}-success`;
    }
    getFailureAction(action) {
        return `${action}-failure`;
    }
    getInitialAction(action) {
        return action.replace(/-success$|-failure$|-ack$/, "");
    }
    removeFromQueue(action) {
        const pending = this.getPendingMessage(action);
        clearTimeout(pending === null || pending === void 0 ? void 0 : pending.replyTimer);
        this.queue = this.queue.filter((pending) => (pending.message.action !== action));
    }
}

const semverPattern = new RegExp("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)" +
    "(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");
var IdentifierDiff;
(function (IdentifierDiff) {
    IdentifierDiff[IdentifierDiff["NEWER"] = 1] = "NEWER";
    IdentifierDiff[IdentifierDiff["SAME"] = 0] = "SAME";
    IdentifierDiff[IdentifierDiff["OLDER"] = -1] = "OLDER";
})(IdentifierDiff || (IdentifierDiff = {}));
function parseSemver(string = "") {
    const result = string.match(semverPattern);
    const [, majorStr, minorStr, patchStr, rc, build] = result ? result : [];
    const major = parseInt(majorStr, 10);
    const minor = parseInt(minorStr, 10);
    const patch = parseInt(patchStr, 10);
    for (const indentifier of [major, minor, patch]) {
        if (Number.isNaN(indentifier)) {
            throw new VersionInvalidError(`Invalid SemVer string '${string}'`);
        }
    }
    return { major, minor, patch, rc, build, string };
}
/**
 * Compares two Semver objects.
 *
 * @param {Semver} a First SemVer object
 * @param {Semver} b Second Semver object
 *
 * @returns {SemverDiff} Diff for major, minor and patch.
 */
function compareSemver(a, b) {
    return {
        major: Math.sign(a.major - b.major),
        minor: Math.sign(a.minor - b.minor),
        patch: Math.sign(a.patch - b.patch),
    };
}

/**
 * Checks if update is required.
 *
 * @param version Object containing SemVer version strings for library, extension and native app.
 *
 * @returns Object which specifies if the extension or native app should be updated.
 */
function checkCompatibility(version) {
    const library = parseSemver(version.library);
    const extension = parseSemver(version.extension);
    const nativeApp = parseSemver(version.nativeApp);
    const extensionDiff = compareSemver(extension, library);
    const nativeAppDiff = compareSemver(nativeApp, library);
    return {
        extension: extensionDiff.major === IdentifierDiff.OLDER,
        nativeApp: nativeAppDiff.major === IdentifierDiff.OLDER,
    };
}
/**
 * Checks an object if 'library', 'extension' or 'nativeApp' properties are present.
 * Values are not checked for SemVer validity.
 *
 * @param object Object which will be checked for version properties.
 *
 * @returns Were any of the version properties found in the provided object.
 */
function hasVersionProperties(object) {
    if (typeof object === "object") {
        for (const prop of ["library", "extension", "nativeApp"]) {
            if (Object.hasOwnProperty.call(object, prop))
                return true;
        }
    }
    return false;
}

class MissingParameterError extends Error {
    constructor(message) {
        super(message);
        this.name = this.constructor.name;
        this.code = ErrorCode$1.ERR_WEBEID_MISSING_PARAMETER;
    }
}

function defer() {
    return new Promise((resolve) => setTimeout(resolve));
}

const webExtensionService = new WebExtensionService();
async function status() {
    await defer(); // Give chrome a moment to load the extension content script
    let statusResponse;
    const library = config.VERSION;
    const timeout = config.EXTENSION_HANDSHAKE_TIMEOUT + config.NATIVE_APP_HANDSHAKE_TIMEOUT;
    const message = { action: Action$1.STATUS };
    try {
        statusResponse = await webExtensionService.send(message, timeout);
    }
    catch (error) {
        error.library = library;
        throw error;
    }
    const versions = { library, ...statusResponse };
    const requiresUpdate = checkCompatibility(versions);
    if (requiresUpdate.extension || requiresUpdate.nativeApp) {
        throw new VersionMismatchError(undefined, versions, requiresUpdate);
    }
    return versions;
}
async function authenticate(options) {
    await defer(); // Give chrome a moment to load the extension content script
    if (typeof options != "object") {
        throw new MissingParameterError("authenticate function requires an options object as parameter");
    }
    if (!options.getAuthChallengeUrl) {
        throw new MissingParameterError("getAuthChallengeUrl missing from authenticate options");
    }
    if (!options.postAuthTokenUrl) {
        throw new MissingParameterError("postAuthTokenUrl missing from authenticate options");
    }
    const timeout = (config.EXTENSION_HANDSHAKE_TIMEOUT +
        config.NATIVE_APP_HANDSHAKE_TIMEOUT +
        (options.serverRequestTimeout || config.DEFAULT_SERVER_REQUEST_TIMEOUT) * 2 +
        (options.userInteractionTimeout || config.DEFAULT_USER_INTERACTION_TIMEOUT));
    const message = { ...options, action: Action$1.AUTHENTICATE };
    const result = await webExtensionService.send(message, timeout);
    return result.response;
}
async function sign(options) {
    await defer(); // Give chrome a moment to load the extension content script
    if (typeof options != "object") {
        throw new MissingParameterError("sign function requires an options object as parameter");
    }
    if (!options.postPrepareSigningUrl) {
        throw new MissingParameterError("postPrepareSigningUrl missing from sign options");
    }
    if (!options.postFinalizeSigningUrl) {
        throw new MissingParameterError("postFinalizeSigningUrl missing from sign options");
    }
    const timeout = (config.EXTENSION_HANDSHAKE_TIMEOUT +
        config.NATIVE_APP_HANDSHAKE_TIMEOUT +
        (options.serverRequestTimeout || config.DEFAULT_SERVER_REQUEST_TIMEOUT) * 2 +
        (options.userInteractionTimeout || config.DEFAULT_USER_INTERACTION_TIMEOUT) * 2);
    const message = { ...options, action: Action$1.SIGN };
    const result = await webExtensionService.send(message, timeout);
    return result.response;
}

export { Action$1 as Action, ErrorCode$1 as ErrorCode, authenticate, config, hasVersionProperties, sign, status };
