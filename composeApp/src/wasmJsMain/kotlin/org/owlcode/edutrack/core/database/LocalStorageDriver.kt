package org.owlcode.edutrack.core.database

import kotlinx.serialization.json.Json

// Interop con localStorage del navegador usando @JsFun (Kotlin/WASM JS).
// Cada "store" se almacena como un objeto JSON bajo la clave "edu_<storeName>".

@JsFun("(ns, key, val) => { var s=JSON.parse(localStorage.getItem('edu_'+ns)||'{}'); s[key]=val; localStorage.setItem('edu_'+ns,JSON.stringify(s)); }")
private external fun lsPut(ns: String, key: String, value: String)

@JsFun("(ns, key) => { var s=JSON.parse(localStorage.getItem('edu_'+ns)||'{}'); return s[key]??null; }")
private external fun lsGet(ns: String, key: String): String?

@JsFun("(ns, key) => { var s=JSON.parse(localStorage.getItem('edu_'+ns)||'{}'); delete s[key]; localStorage.setItem('edu_'+ns,JSON.stringify(s)); }")
private external fun lsDelete(ns: String, key: String)

@JsFun("(ns) => JSON.stringify(Object.values(JSON.parse(localStorage.getItem('edu_'+ns)||'{}')))")
private external fun lsGetAllJson(ns: String): String

@JsFun("(ns) => localStorage.removeItem('edu_'+ns)")
private external fun lsClear(ns: String)

class LocalStorageDriver : StorageDriver {
    override suspend fun put(storeName: String, key: String, value: String) =
        lsPut(storeName, key, value)

    override suspend fun get(storeName: String, key: String): String? =
        lsGet(storeName, key)

    override suspend fun delete(storeName: String, key: String) =
        lsDelete(storeName, key)

    override suspend fun getAll(storeName: String): List<String> =
        Json.decodeFromString<List<String>>(lsGetAllJson(storeName))

    override suspend fun clear(storeName: String) =
        lsClear(storeName)
}

